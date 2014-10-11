package org.sharedhealth.mci.web.infrastructure.persistence;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.utils.UidGenerator;
import org.sharedhealth.mci.web.exception.HealthIDExistException;
import org.sharedhealth.mci.web.exception.PatientAlreadyExistException;
import org.sharedhealth.mci.web.exception.PatientNotFoundException;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.sharedhealth.mci.web.mapper.PhoneNumber;
import org.sharedhealth.mci.web.mapper.Relation;
import org.sharedhealth.mci.web.model.Patient;
import org.sharedhealth.mci.web.utils.concurrent.SimpleListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.AsynchronousQueryListener;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;

import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.*;

@Component
public class PatientRepository extends BaseRepository {

    protected static final Logger logger = LoggerFactory.getLogger(PatientRepository.class);

    private static final UidGenerator uid = new UidGenerator();

    @Autowired
    public PatientRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations) {
        super(cassandraOperations);
    }

    public ListenableFuture<MCIResponse> create(PatientMapper patientMapper) {

        PatientMapper existingPatient;

        final SettableFuture<MCIResponse> result = SettableFuture.create();

        if (!StringUtils.isBlank(patientMapper.getHealthId())) {
            DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(patientMapper, "patient");
            bindingResult.addError(new FieldError("patient", "hid", "3001"));
            throw new HealthIDExistException(bindingResult);
        }

        try {
            existingPatient = getExistingPatient(patientMapper);
        } catch (ExecutionException e) {
            result.setException(e.getCause());
            return getStringListenableFuture(result);
        } catch (Exception e) {
            result.setException(e);
            return getStringListenableFuture(result);
        }

        if (existingPatient == null) {
            patientMapper.setHealthId(uid.getId());
        } else if (StringUtils.isBlank(patientMapper.getHealthId())) {
            logger.debug("Update flow");
            return update(patientMapper, existingPatient.getHealthId());
        } else {
            result.setException(new PatientAlreadyExistException(existingPatient.getHealthId()));
            return getStringListenableFuture(result);
        }

        String fullName = "";
        if (patientMapper.getGivenName() != null) {
            fullName = patientMapper.getGivenName();
        }
        if (patientMapper.getSurName() != null) {
            fullName = fullName + " " + patientMapper.getSurName();
        }

        Patient p = getEntityFromPatientMapper(patientMapper);

        p.setHealthId(uid.getId());
        p.setFullName(fullName);
        p.setCreatedAt(new Date());
        p.setUpdatedAt(new Date());
        p.setSurName(patientMapper.getSurName());

        p = cassandraOperations.insert(p);

        result.set(new MCIResponse(p.getHealthId(), HttpStatus.CREATED));

        return getStringListenableFuture(result);
    }

    private ListenableFuture<MCIResponse> getStringListenableFuture(final SettableFuture<MCIResponse> result) {
        return new SimpleListenableFuture<MCIResponse, MCIResponse>(result) {
            @Override
            protected MCIResponse adapt(MCIResponse adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    private PatientMapper getExistingPatient(PatientMapper patientMapper) throws InterruptedException, ExecutionException {

        PatientMapper existingPatient;

        if (!StringUtils.isBlank(patientMapper.getHealthId())) {
            try {
                return findByHealthId(patientMapper.getHealthId()).get();
            } catch (Exception e) {
                DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(patientMapper, "patient");
                bindingResult.addError(new FieldError("patient", "hid", "2002"));
                throw new ValidationException(bindingResult);
            }
        }

        if (!StringUtils.isBlank(patientMapper.getNationalId())) {
            try {
                existingPatient = findByNationalId(patientMapper.getNationalId()).get();

                if (existingPatient.isSimilarTo(patientMapper)) {
                    return existingPatient;
                }
            } catch (Exception e) {
                logger.debug("something happens finding NationalId");
            }
        }

        if (!StringUtils.isBlank(patientMapper.getBirthRegistrationNumber())) {

            try {
                existingPatient = findByBirthRegistrationNumber(patientMapper.getBirthRegistrationNumber()).get();

                if (existingPatient.isSimilarTo(patientMapper)) {
                    return existingPatient;
                }
            } catch (Exception e) {
                logger.debug("something happens finding BirthRegistration");
            }
        }

        if (!StringUtils.isBlank(patientMapper.getUid())) {
            try {
                existingPatient = findByUid(patientMapper.getUid()).get();

                if (existingPatient.isSimilarTo(patientMapper)) {
                    return existingPatient;
                }
            } catch (Exception e) {
                logger.debug("something happens finding Uid");
            }
        }

        return null;
    }

    public ListenableFuture<PatientMapper> findByHealthId(final String healthId) {
        String cql = String.format(getFindByHealthIdQuery(), healthId);
        logger.debug("Find patient by health id CQL: [" + cql + "]");
        final SettableFuture<PatientMapper> result = SettableFuture.create();

        cassandraOperations.queryAsynchronously(cql, new AsynchronousQueryListener() {
            @Override
            public void onQueryComplete(ResultSetFuture rsf) {
                try {
                    Row row = rsf.get(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS).one();
                    if (row == null) {
                        throw new PatientNotFoundException("No patient found with health id: " + healthId);
                    }
                    setPatientOnResult(row, result);
                } catch (Exception e) {
                    logger.error("Error while finding patient by healthId: " + healthId, e);
                    result.setException(e);
                }
            }
        });

        return new SimpleListenableFuture<PatientMapper, PatientMapper>(result) {
            @Override
            protected PatientMapper adapt(PatientMapper adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    public ListenableFuture<PatientMapper> findByNationalId(final String nationalId) {
        String cql = String.format(getFindByNationalIdQuery(), nationalId);
        logger.debug("Find patient by national id CQL: [" + cql + "]");
        final SettableFuture<PatientMapper> result = SettableFuture.create();

        cassandraOperations.queryAsynchronously(cql, new AsynchronousQueryListener() {
            @Override
            public void onQueryComplete(ResultSetFuture rsf) {
                try {
                    Row row = rsf.get(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS).one();
                    if (row == null) {
                        throw new PatientNotFoundException("No patient found with national id: " + nationalId);
                    }
                    setPatientOnResult(row, result);
                } catch (Exception e) {
                    logger.error("Error while finding patient by nationalId: " + nationalId, e);
                    result.setException(e);
                }
            }
        });

        return new SimpleListenableFuture<PatientMapper, PatientMapper>(result) {
            @Override
            protected PatientMapper adapt(PatientMapper adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    public ListenableFuture<PatientMapper> findByBirthRegistrationNumber(final String birthRegistrationNumber) {
        String cql = String.format(getFindByBirthRegistrationNumberQuery(), birthRegistrationNumber);
        logger.debug("Find patient by birth registration number CQL: [" + cql + "]");
        final SettableFuture<PatientMapper> result = SettableFuture.create();

        cassandraOperations.queryAsynchronously(cql, new AsynchronousQueryListener() {
            @Override
            public void onQueryComplete(ResultSetFuture rsf) {
                try {
                    Row row = rsf.get(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS).one();
                    if (row == null) {
                        throw new PatientNotFoundException("No patient found with birth registration number: " + birthRegistrationNumber);
                    }
                    setPatientOnResult(row, result);
                } catch (Exception e) {
                    logger.error("Error while finding patient by birth registration number: " + birthRegistrationNumber, e);
                    result.setException(e);
                }
            }
        });

        return new SimpleListenableFuture<PatientMapper, PatientMapper>(result) {
            @Override
            protected PatientMapper adapt(PatientMapper adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    public ListenableFuture<PatientMapper> findByName(final String fullName) {
        String cql = String.format(getFindByNameQuery(), fullName);
        logger.debug("Find patient by name  CQL: [" + cql + "]");
        final SettableFuture<PatientMapper> result = SettableFuture.create();

        cassandraOperations.queryAsynchronously(cql, new AsynchronousQueryListener() {
            @Override
            public void onQueryComplete(ResultSetFuture rsf) {
                try {
                    Row row = rsf.get(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS).one();
                    if (row == null) {
                        throw new PatientNotFoundException("No patient found with name: " + fullName);
                    }
                    setPatientOnResult(row, result);
                } catch (Exception e) {
                    logger.error("Error while finding patient by name: " + fullName, e);
                    result.setException(e);
                }
            }
        });

        return new SimpleListenableFuture<PatientMapper, PatientMapper>(result) {
            @Override
            protected PatientMapper adapt(PatientMapper adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    public ListenableFuture<PatientMapper> findByUid(final String uid) {
        String cql = String.format(getFindByUidQuery(), uid);
        logger.debug("Find patient by name  CQL: [" + cql + "]");
        final SettableFuture<PatientMapper> result = SettableFuture.create();

        cassandraOperations.queryAsynchronously(cql, new AsynchronousQueryListener() {
            @Override
            public void onQueryComplete(ResultSetFuture rsf) {
                try {
                    Row row = rsf.get(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS).one();
                    if (row == null) {
                        throw new PatientNotFoundException("No patient found with name: " + uid);
                    }
                    setPatientOnResult(row, result);
                } catch (Exception e) {
                    logger.error("Error while finding patient by name: " + uid, e);
                    result.setException(e);
                }
            }
        });

        return new SimpleListenableFuture<PatientMapper, PatientMapper>(result) {
            @Override
            protected PatientMapper adapt(PatientMapper adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    private void setPatientOnResult(Row r, SettableFuture<PatientMapper> result) throws InterruptedException, ExecutionException {
        PatientMapper patientMapper = getPatientFromRow(r);
        result.set(patientMapper);
    }

    private PatientMapper getPatientFromRow(Row r) {
        DatabaseRow row = new DatabaseRow(r);
        PatientMapper patientMapper = new PatientMapper();
        ObjectMapper mapper = new ObjectMapper();

        try {
            patientMapper.setRelations(mapper.readValue(row.getString(RELATIONS), List.class));
        } catch (Exception e) {
            logger.debug(" Relations: [" + e.getMessage() + "]");
        }

        patientMapper.setHealthId(row.getString(HEALTH_ID));
        patientMapper.setNationalId(row.getString(NATIONAL_ID));
        patientMapper.setUid(row.getString(UID));
        patientMapper.setPlaceOfBirth(row.getString(PLACE_OF_BIRTH));

        patientMapper.setReligion(row.getString(RELIGION));
        patientMapper.setBloodGroup(row.getString(BLOOD_GROUP));
        patientMapper.setNameBangla(row.getString(FULL_NAME_BANGLA));
        patientMapper.setBirthRegistrationNumber(row.getString(BIN_BRN));
        patientMapper.setGivenName(row.getString(GIVEN_NAME));
        patientMapper.setSurName(row.getString(SUR_NAME));
        patientMapper.setDateOfBirth(row.getDateAsString(DATE_OF_BIRTH));
        patientMapper.setGender(row.getString(GENDER));
        patientMapper.setOccupation(row.getString(OCCUPATION));
        patientMapper.setEducationLevel(row.getString(EDU_LEVEL));
        patientMapper.setNationality(row.getString(NATIONALITY));
        patientMapper.setDisability(row.getString(DISABILITY));
        patientMapper.setEthnicity(row.getString(ETHNICITY));
        patientMapper.setIsAlive(row.getString(IS_ALIVE));

        patientMapper.setPrimaryContact(row.getString(PRIMARY_CONTACT));


        Address address = new Address();
        address.setAddressLine(row.getString(ADDRESS_LINE));
        address.setDivisionId(row.getString(DIVISION_ID));
        address.setDistrictId(row.getString(DISTRICT_ID));
        address.setUpazillaId(row.getString(UPAZILLA_ID));
        address.setUnionId(row.getString(UNION_ID));
        address.setHoldingNumber(row.getString(HOLDING_NUMBER));
        address.setStreet(row.getString(STREET));
        address.setAreaMouja(row.getString(AREA_MOUJA));
        address.setVillage(row.getString(VILLAGE));
        address.setPostOffice(row.getString(POST_OFFICE));
        address.setPostCode(row.getString(POST_CODE));
        address.setWardId(row.getString(WARD));
        address.setThanaId(row.getString(THANA));
        address.setCityCorporationId(row.getString(CITY_CORPORATION));
        address.setCountryCode(row.getString(COUNTRY));
        patientMapper.setAddress(address);

        Address permanentaddress = new Address();
        permanentaddress.setAddressLine(row.getString(PERMANENT_ADDRESS_LINE));
        permanentaddress.setDivisionId(row.getString(PERMANENT_DIVISION_ID));
        permanentaddress.setDistrictId(row.getString(PERMANENT_DISTRICT_ID));
        permanentaddress.setUpazillaId(row.getString(PERMANENT_UPAZILLA_ID));
        permanentaddress.setUnionId(row.getString(PERMANENT_UNION_ID));
        permanentaddress.setHoldingNumber(row.getString(PERMANENT_HOLDING_NUMBER));
        permanentaddress.setStreet(row.getString(PERMANENT_STREET));
        permanentaddress.setAreaMouja(row.getString(PERMANENT_AREA_MOUJA));
        permanentaddress.setVillage(row.getString(PERMANENT_VILLAGE));
        permanentaddress.setPostOffice(row.getString(PERMANENT_POST_OFFICE));
        permanentaddress.setPostCode(row.getString(PERMANENT_POST_CODE));
        permanentaddress.setWardId(row.getString(PERMANENT_WARD));
        permanentaddress.setThanaId(row.getString(PERMANENT_THANA));
        permanentaddress.setCityCorporationId(row.getString(PERMANENT_CITY_CORPORATION));
        permanentaddress.setCountryCode(row.getString(PERMANENT_COUNTRY));

        PhoneNumber phoneNumber = new PhoneNumber();
        PhoneNumber primaryContactNumber = new PhoneNumber();

        phoneNumber.setNumber(row.getString(PHONE_NO));
        phoneNumber.setAreaCode(row.getString(PHONE_NUMBER_AREA_CODE));
        phoneNumber.setCountryCode(row.getString(PHONE_NUMBER_COUNTRY_CODE));
        phoneNumber.setExtension(row.getString(PHONE_NUMBER_EXTENSION));

        primaryContactNumber.setNumber(row.getString(PRIMARY_CONTACT_NO));
        primaryContactNumber.setAreaCode(row.getString(PRIMARY_CONTACT_NUMBER_AREA_CODE));
        primaryContactNumber.setCountryCode(row.getString(PRIMARY_CONTACT_NUMBER_COUNTRY_CODE));
        primaryContactNumber.setExtension(row.getString(PRIMARY_CONTACT_NUMBER_EXTENSION));

        if (primaryContactNumber.getNumber() != null) {
            patientMapper.setPhoneNumber(phoneNumber);
        }

        if (phoneNumber.getNumber() != null) {
            patientMapper.setPrimaryContactNumber(primaryContactNumber);
        }

        if (permanentaddress.getCountryCode() != null) {
            if (permanentaddress.getCountryCode() == "050" && permanentaddress.getDistrictId() != null) {
                patientMapper.setPermanentAddress(permanentaddress);
            }

            if (permanentaddress.getCountryCode() != "050") {
                patientMapper.setPermanentAddress(permanentaddress);
            }
        }

        patientMapper.setCreatedAt(row.getDate("created_at"));
        patientMapper.setUpdatedAt(row.getDate("updated_at"));

        return patientMapper;
    }

    public ListenableFuture<List<PatientMapper>> findAllByQuery(MultiValueMap<String, String> parameters) {

        Select select = QueryBuilder.select().from("patient");

        if (parameters.get("full_name") != null) {
            select.where(QueryBuilder.eq("full_name", parameters.get("full_name").get(0)));
        }

        if (parameters.get("nid") != null) {
            select.where(QueryBuilder.eq(PatientQueryBuilder.NATIONAL_ID, parameters.get("nid").get(0)));
        }

        if (parameters.get("bin_brn") != null) {
            select.where(QueryBuilder.eq(PatientQueryBuilder.BIN_BRN, parameters.get("bin_brn").get(0)));
        }

        if (parameters.get("uid") != null) {
            select.where(QueryBuilder.eq(PatientQueryBuilder.UID, parameters.get("uid").get(0)));
        }

        return getPatientListListenableFuture(select);
    }

    public ListenableFuture<MCIResponse> update(PatientMapper patientMapper, final String hid) {
        final SettableFuture<MCIResponse> result = SettableFuture.create();

        String fullName = "";
        if (patientMapper.getGivenName() != null) {
            fullName = patientMapper.getGivenName();
        }
        if (patientMapper.getSurName() != null) {
            fullName = fullName + " " + patientMapper.getSurName();
        }

        Patient p = getEntityFromPatientMapper(patientMapper);
        p.setHealthId(hid);
        p.setFullName(fullName);
        p.setUpdatedAt(new Date());
        p = cassandraOperations.update(p);
        result.set(new MCIResponse(p.getHealthId(), HttpStatus.ACCEPTED));
        return getStringListenableFuture(result);
    }

    public ListenableFuture<List<PatientMapper>> findAllByLocations(List<String> locations, String start, Date since) {

        final SettableFuture<List<PatientMapper>> result = SettableFuture.create();
        List<PatientMapper> patients = new ArrayList<>();

        int limit = PER_PAGE_LIMIT;

        if (locations != null && locations.size() > 0) {
            String locationPointer = getLocationPointer(locations, start, null);

            for (String catchment : locations) {
                if (patients.size() == 0 && !isLocationBelongsToCatchment(locationPointer, catchment)) {
                    continue;
                }

                ListenableFuture<List<PatientMapper>> res = this.findAllByLocation(catchment, start, limit, since);
                try {
                    List<PatientMapper> temp = res.get();
                    patients.addAll(temp);

                    if (patients.size() < PER_PAGE_LIMIT) {
                        start = null;
                        limit = PER_PAGE_LIMIT - patients.size();
                        locationPointer = null;
                    } else {
                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            return this.findAllByLocation(null, start, limit, since);
        }

        result.set(patients);
        return getPatientListListenableFuture(result);
    }

    public ListenableFuture<List<PatientMapper>> findAllByLocation(String location, String start, int limit, Date since) {

        Select select = QueryBuilder.select().from("patient");

        if (StringUtils.isBlank(location)) {
            final SettableFuture<List<PatientMapper>> result = SettableFuture.create();
            return getPatientListListenableFuture(result);
        }

        select.where(QueryBuilder.eq(getAddressHierarchyField(location.length()), location));

        if (StringUtils.isNotBlank(start)) {
            select.where(QueryBuilder.gt(QueryBuilder.token("health_id"), QueryBuilder.raw("token('" + start + "')")));
        }

        if (since != null) {
            select.where(QueryBuilder.gt("updated_at", since));
            select.allowFiltering();
        }

        if (limit > 0) {
            select.limit(limit);
        }

        return getPatientListListenableFuture(select);
    }

    private ListenableFuture<List<PatientMapper>> getPatientListListenableFuture(final Select select) {
        return new SimpleListenableFuture<List<PatientMapper>, ResultSet>(
                cassandraOperations.queryAsynchronously(select)) {
            @Override
            protected List<PatientMapper> adapt(ResultSet resultSet) throws ExecutionException {
                List<PatientMapper> patientMappers = new ArrayList<>();
                for (Row result : resultSet.all()) {
                    PatientMapper patientMapper = getPatientFromRow(result);
                    patientMappers.add(patientMapper);
                }

                return patientMappers;
            }
        };
    }

    private ListenableFuture<List<PatientMapper>> getPatientListListenableFuture(final SettableFuture<List<PatientMapper>> result) {
        return new SimpleListenableFuture<List<PatientMapper>, List<PatientMapper>>(result) {
            @Override
            protected List<PatientMapper> adapt(List<PatientMapper> p) throws ExecutionException {
                return p;
            }
        };
    }

    private String getLocationPointer(List<String> locations, String start, String d) {
        if (locations.size() > 1 && StringUtils.isNotBlank(start)) {
            ListenableFuture<PatientMapper> p = findByHealthId(start);
            try {
                return p.get().getAddress().getGeoCode();
            } catch (InterruptedException | ExecutionException e) {
                logger.debug("Invalid start parameter");
            }
        }

        return d;
    }

    private boolean isLocationBelongsToCatchment(String location, String catchment) {

        if (StringUtils.isBlank(location)) {
            return true;
        }

        return location.startsWith(catchment);
    }

    private String getAddressHierarchyField(int length) {
        return "location_level" + (length / 2);
    }

    public Patient getEntityFromPatientMapper(PatientMapper p) {
        Patient patient = new Patient();

        String relationsJson = "";
        ObjectMapper mapper = new ObjectMapper();

        Relation father = p.getRelation("father");
        Relation mother = p.getRelation("mother");

        try {
            relationsJson = mapper.writeValueAsString(p.getRelations());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Address address = p.getAddress();
        Address permanentAddress = p.getPermanentAddress();

        PhoneNumber phoneNumber = p.getPhoneNumber();
        PhoneNumber primaryContactNumber = p.getPrimaryContactNumber();

        patient.setHealthId(p.getHealthId());
        patient.setNationalId(p.getNationalId());
        patient.setBirthRegistrationNumber(p.getBirthRegistrationNumber());
        patient.setFullNameBangla(StringUtils.trim(p.getNameBangla()));
        patient.setGivenName(StringUtils.trim(p.getGivenName()));
        patient.setSurName(StringUtils.trim(p.getSurName()));
        patient.setDateOfBirth(p.getDateOfBirth());
        patient.setGender(p.getGender());
        patient.setOccupation(p.getOccupation());
        patient.setEducationLevel(p.getEducationLevel());

        if (father != null) {
            patient.setFathersNameBangla(StringUtils.trim(father.getNameBangla()));
            patient.setFathersGivenName(StringUtils.trim(father.getGivenName()));
            patient.setFathersSurName(StringUtils.trim(father.getSurName()));
            patient.setFathersBrn(father.getBirthRegistrationNumber());
            patient.setFathersNid(father.getNationalId());
            patient.setFathersUid(father.getUid());
        }

        if (mother != null) {
            patient.setMothersNameBangla(StringUtils.trim(mother.getNameBangla()));
            patient.setMothersGivenName(StringUtils.trim(mother.getGivenName()));
            patient.setMothersSurName(StringUtils.trim(mother.getSurName()));
            patient.setMothersBrn(mother.getBirthRegistrationNumber());
            patient.setMothersNid(mother.getNationalId());
            patient.setMothersUid(mother.getUid());
        }

        patient.setUid(p.getUid());
        patient.setPlaceOfBirth(StringUtils.trim(p.getPlaceOfBirth()));
        patient.setReligion(p.getReligion());
        patient.setBloodGroup(p.getBloodGroup());
        patient.setNationality(StringUtils.trim(p.getNationality()));
        patient.setDisability(p.getDisability());
        patient.setEthnicity(p.getEthnicity());
        patient.setIsAlive(p.getIsAlive());

        if (address != null) {
            patient.setAddressLine(address.getAddressLine());
            patient.setDivisionId(address.getDivisionId());
            patient.setDistrictId(address.getDistrictId());
            patient.setUpazillaId(address.getUpazillaId());
            patient.setUnionId(address.getUnionId());
            patient.setHoldingNumber(StringUtils.trim(address.getHoldingNumber()));
            patient.setStreet(StringUtils.trim(address.getStreet()));
            patient.setAreaMouja(StringUtils.trim(address.getAreaMouja()));
            patient.setVillage(StringUtils.trim(address.getVillage()));
            patient.setPostOffice(StringUtils.trim(address.getPostOffice()));
            patient.setPostCode(address.getPostCode());
            patient.setWardId(address.getWardId());
            patient.setThanaId(address.getThanaId());
            patient.setCityCorporationId(address.getCityCorporationId());
            patient.setCountryCode(address.getCountryCode());
        }

        if (permanentAddress != null) {
            patient.setPermanentAddressLine(permanentAddress.getAddressLine());
            patient.setPermanentDivisionId(permanentAddress.getDivisionId());
            patient.setPermanentDistrictId(permanentAddress.getDistrictId());
            patient.setPermanentUpazillaId(permanentAddress.getUpazillaId());
            patient.setPermanentUnionId(permanentAddress.getUnionId());
            patient.setPermanentHoldingNumber(StringUtils.trim(permanentAddress.getHoldingNumber()));
            patient.setPermanentStreet(StringUtils.trim(permanentAddress.getStreet()));
            patient.setPermanentAreaMouja(StringUtils.trim(permanentAddress.getAreaMouja()));
            patient.setPermanentVillage(StringUtils.trim(permanentAddress.getVillage()));
            patient.setPermanentPostOffice(StringUtils.trim(permanentAddress.getPostOffice()));
            patient.setPermanentPostCode(permanentAddress.getPostCode());
            patient.setPermanentWardId(permanentAddress.getWardId());
            patient.setPermanentThanaId(permanentAddress.getThanaId());
            patient.setPermanentCityCorporationId(permanentAddress.getCityCorporationId());
            patient.setPermanentCountryCode(permanentAddress.getCountryCode());
        }

        patient.setRelations(relationsJson);

        if (phoneNumber != null) {
            patient.setCellNo(phoneNumber.getNumber());
            patient.setPhoneNumberAreaCode(phoneNumber.getAreaCode());
            patient.setPhoneNumberCountryCode(phoneNumber.getCountryCode());
            patient.setPhoneNumberExtension(phoneNumber.getExtension());
        }

        if (primaryContactNumber != null) {
            patient.setPrimaryCellNo(primaryContactNumber.getNumber());
            patient.setPrimaryContactNumberAreaCode(primaryContactNumber.getAreaCode());
            patient.setPrimaryContactNumberCountryCode(primaryContactNumber.getCountryCode());
            patient.setPrimaryContactNumberExtension(primaryContactNumber.getExtension());
        }

        patient.setPrimaryContact(StringUtils.trim(p.getPrimaryContact()));


        return patient;
    }
}
