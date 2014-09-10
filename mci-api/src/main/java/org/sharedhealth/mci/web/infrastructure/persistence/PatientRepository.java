package org.sharedhealth.mci.web.infrastructure.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.utils.UidGenerator;
import org.sharedhealth.mci.web.exception.PatientAlreadyExistException;
import org.sharedhealth.mci.web.exception.PatientNotFoundException;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.sharedhealth.mci.web.mapper.Relation;
import org.sharedhealth.mci.web.model.Patient;
import org.sharedhealth.mci.web.utils.concurrent.SimpleListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.AsynchronousQueryListener;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;

import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.*;

@Component
public class PatientRepository {
    private static final Logger logger = LoggerFactory.getLogger(PatientRepository.class);
    private static long TIMEOUT_IN_MILLIS = 10;
    private static int PER_PAGE_LIMIT = 10;

    private static final UidGenerator uid = new UidGenerator();
    private CassandraOperations cassandraOperations;

    @Autowired
    public PatientRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations) {
        this.cassandraOperations = cassandraOperations;
    }

    public ListenableFuture<String> create(PatientMapper patientMapper) {

        PatientMapper existingPatientMapper;

        final SettableFuture<String> result = SettableFuture.create();

        try {
            existingPatientMapper = getExistingPatient(patientMapper);
        } catch (ExecutionException e) {
            result.setException(e.getCause());
            return getStringListenableFuture(result);
        } catch (Exception e) {
            result.setException(e);
            return getStringListenableFuture(result);
        }

        if (!StringUtils.isBlank(existingPatientMapper.getHealthId())) {
            result.setException(new PatientAlreadyExistException(existingPatientMapper.getHealthId()));
            return getStringListenableFuture(result);
        }

        final String healthId = patientMapper.getHealthId();

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

        p = cassandraOperations.insert(p);

        result.set(p.getHealthId());

        return getStringListenableFuture(result);
    }

    private ListenableFuture<String> getStringListenableFuture(final SettableFuture<String> result) {
        return new SimpleListenableFuture<String, String>(result) {
            @Override
            protected String adapt(String adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    private PatientMapper getExistingPatient(PatientMapper patientMapper) throws InterruptedException, ExecutionException {

        if (!StringUtils.isBlank(patientMapper.getHealthId())) {
            try {
                return findByHealthId(patientMapper.getHealthId()).get();
            } catch (Exception e) {
                DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(patientMapper, "patient");
                bindingResult.addError(new FieldError("patient", "hid", "404"));
                throw new ValidationException(bindingResult);
            }
        }

        if (!StringUtils.isBlank(patientMapper.getNationalId())) {
            try {
                return findByNationalId(patientMapper.getNationalId()).get();
            } catch (Exception e) {
                logger.debug("Can not find patient by [National Id");
            }
        }

        if (!StringUtils.isBlank(patientMapper.getBirthRegistrationNumber())) {

            try {
                return findByBirthRegistrationNumber(patientMapper.getBirthRegistrationNumber()).get();
            } catch (Exception e) {
                logger.debug("Can not find patient by [Birth Registration Number]");
            }
        }

        if (!StringUtils.isBlank(patientMapper.getUid())) {
            try {
                return findByUid(patientMapper.getUid()).get();
            } catch (Exception e) {
                logger.debug("Can not find patient by [UID]");
            }
        }

        return patientMapper;
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
        patientMapper.setDateOfBirth(row.getString(DATE_OF_BIRTH));
        patientMapper.setGender(row.getString(GENDER));
        patientMapper.setOccupation(row.getString(OCCUPATION));
        patientMapper.setEducationLevel(row.getString(EDU_LEVEL));
        patientMapper.setNationality(row.getString(NATIONALITY));
        patientMapper.setDisability(row.getString(DISABILITY));
        patientMapper.setEthnicity(row.getString(ETHNICITY));
        patientMapper.setIsAlive(row.getString(IS_ALIVE));
        patientMapper.setCellNo(row.getString(CELL_NO));
        patientMapper.setPrimaryContact(row.getString(PRIMARY_CONTACT));
        patientMapper.setPrimaryCellNo(row.getString(PRIMARY_CELL_NO));

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

        Address permanetaddress = new Address();
        permanetaddress.setAddressLine(row.getString(PERMANENT_ADDRESS_LINE));
        permanetaddress.setDivisionId(row.getString(PERMANENT_DIVISION_ID));
        permanetaddress.setDistrictId(row.getString(PERMANENT_DISTRICT_ID));
        permanetaddress.setUpazillaId(row.getString(PERMANENT_UPAZILLA_ID));
        permanetaddress.setUnionId(row.getString(PERMANENT_UNION_ID));
        permanetaddress.setHoldingNumber(row.getString(PERMANENT_HOLDING_NUMBER));
        permanetaddress.setStreet(row.getString(PERMANENT_STREET));
        permanetaddress.setAreaMouja(row.getString(PERMANENT_AREA_MOUJA));
        permanetaddress.setVillage(row.getString(PERMANENT_VILLAGE));
        permanetaddress.setPostOffice(row.getString(PERMANENT_POST_OFFICE));
        permanetaddress.setPostCode(row.getString(PERMANENT_POST_CODE));
        permanetaddress.setWardId(row.getString(PERMANENT_WARD));
        permanetaddress.setThanaId(row.getString(PERMANENT_THANA));
        permanetaddress.setCityCorporationId(row.getString(PERMANENT_CITY_CORPORATION));
        permanetaddress.setCountryCode(row.getString(PERMANENT_COUNTRY));
        patientMapper.setPermanentAddress(permanetaddress);
        patientMapper.setCreatedAt(row.getDate("created_at"));

        return patientMapper;
    }

    public ListenableFuture<List<PatientMapper>> findAllByQuery(MultiValueMap<String, String> parameters) {

        Select select = QueryBuilder.select().from("patient");

        if (parameters.get("full_name") != null) {
            select.where(QueryBuilder.eq("full_name", parameters.get("full_name").get(0)));
        }

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

    public ListenableFuture<String> update(PatientMapper patientMapper, final String hid) {
        final SettableFuture<String> result = SettableFuture.create();
        Address address = patientMapper.getAddress();
        Address permanentAddress = patientMapper.getPermanentAddress();

        Relation father = patientMapper.getRelation("father");
        Relation mother = patientMapper.getRelation("mother");

        String relationsJson = "";
        ObjectMapper mapper = new ObjectMapper();

        try {
            List<Relation> relations = patientMapper.getRelations();
            relationsJson = mapper.writeValueAsString(relations);
            father = patientMapper.getRelation("father");
            mother = patientMapper.getRelation("mother");

        } catch (Exception e) {
            logger.debug(" Relations: [" + e.getMessage() + "]");
        }

        if (permanentAddress == null) {
            permanentAddress = new Address();
        }
        String fullName = "";
        if (patientMapper.getGivenName() != null) {
            fullName = patientMapper.getGivenName();
        }
        if (patientMapper.getSurName() != null) {
            fullName = fullName + " " + patientMapper.getSurName();
        }

        String cql = String.format(getUpdateQuery(),
                patientMapper.getNationalId(),
                patientMapper.getBirthRegistrationNumber(),
                patientMapper.getNameBangla(),
                patientMapper.getGivenName(),
                patientMapper.getSurName(),
                patientMapper.getDateOfBirth(),
                patientMapper.getGender(),
                patientMapper.getOccupation(),
                patientMapper.getEducationLevel(),
                father.getNameBangla(),
                father.getGivenName(),
                father.getSurName(),
                father.getBinBrn(),
                father.getNid(),
                father.getUid(),
                mother.getNameBangla(),
                mother.getGivenName(),
                mother.getSurName(),
                mother.getBinBrn(),
                mother.getNid(),
                mother.getUid(),
                patientMapper.getUid(),
                patientMapper.getPlaceOfBirth(),
                patientMapper.getReligion(),
                patientMapper.getBloodGroup(),
                patientMapper.getNationality(),
                patientMapper.getDisability(),
                patientMapper.getEthnicity(),
                patientMapper.getIsAlive(),
                address.getAddressLine(),
                address.getDivisionId(),
                address.getDistrictId(),
                address.getUpazillaId(),
                address.getUnionId(),
                address.getHoldingNumber(),
                address.getStreet(),
                address.getAreaMouja(),
                address.getVillage(),
                address.getPostOffice(),
                address.getPostCode(),
                address.getWardId(),
                address.getThanaId(),
                address.getCityCorporationId(),
                address.getCountryCode(),
                permanentAddress.getAddressLine(),
                permanentAddress.getDivisionId(),
                permanentAddress.getDistrictId(),
                permanentAddress.getUpazillaId(),
                permanentAddress.getUnionId(),
                permanentAddress.getHoldingNumber(),
                permanentAddress.getStreet(),
                permanentAddress.getAreaMouja(),
                permanentAddress.getVillage(),
                permanentAddress.getPostOffice(),
                permanentAddress.getPostCode(),
                permanentAddress.getWardId(),
                permanentAddress.getThanaId(),
                permanentAddress.getCityCorporationId(),
                permanentAddress.getCountryCode(),
                fullName.toLowerCase(),
                relationsJson,
                patientMapper.getPrimaryContact(),
                patientMapper.getCellNo(),
                patientMapper.getPrimaryCellNo(),
                hid
        );

        logger.debug("Update patient CQL: [" + cql + "]");

        cassandraOperations.executeAsynchronously(cql, new AsynchronousQueryListener() {
            @Override
            public void onQueryComplete(ResultSetFuture rsf) {
                try {
                    rsf.get(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS);
                    result.set(hid);
                } catch (Exception e) {
                    logger.error("Error while creating patient.", e);
                    result.setException(e);
                }
            }
        });

        return getStringListenableFuture(result);
    }

    public ListenableFuture<List<PatientMapper>> findAllByLocations(List<String> locations, String start) {

        final SettableFuture<List<PatientMapper>> result = SettableFuture.create();
        List<PatientMapper> patients = new ArrayList<>();

        int limit = PER_PAGE_LIMIT;

        if (locations.size() > 0) {
            String locationPointer = getLocationPointer(locations, start, null);

            for (String catchment : locations) {
                if (patients.size() == 0 && !isLocationBelongsToCatchment(locationPointer, catchment)) {
                    continue;
                }

                ListenableFuture<List<PatientMapper>> res = this.findAllByLocation(catchment, start, limit);
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
            return this.findAllByLocation(null, start, limit);
        }

        result.set(patients);
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
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

    public ListenableFuture<List<PatientMapper>> findAllByLocation(String location, String start, int limit) {

        String cql = "SELECT * FROM patient %s LIMIT %s";
        List<String> wheres = new ArrayList<>();

        if (StringUtils.isNotBlank(location)) {
            int i = location.length() / 2;
            wheres.add("location_level" + i + "='" + location + "'");
        }

        if (StringUtils.isNotBlank(start)) {
            wheres.add("token(health_id) > token('" + start + "')");
        }

        String where = Joiner.on(" AND ").join(wheres);

        if (StringUtils.isNotBlank(where)) {
            where = "WHERE " + where;
        }

        return new SimpleListenableFuture<List<PatientMapper>, ResultSet>(
                cassandraOperations.queryAsynchronously(String.format(cql, where, limit))) {
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

        patient.setHealthId(p.getHealthId());
        patient.setNationalId(p.getNationalId());
        patient.setBirthRegistrationNumber(p.getBirthRegistrationNumber());
        patient.setFullNameBangla(p.getNameBangla());
        patient.setGivenName(p.getGivenName());
        patient.setSurName(p.getSurName());
        patient.setDateOfBirth(p.getDateOfBirth());
        patient.setGender(p.getGender());
        patient.setOccupation(p.getOccupation());
        patient.setEducationLevel(p.getEducationLevel());

        if (father != null) {
            patient.setFathersNameBangla(father.getNameBangla());
            patient.setFathersGivenName(father.getGivenName());
            patient.setFathersSurName(father.getSurName());
            patient.setFathersBrn(father.getBinBrn());
            patient.setFathersNid(father.getNid());
            patient.setFathersUid(father.getUid());
        }

        if (mother != null) {
            patient.setMothersNameBangla(mother.getNameBangla());
            patient.setMothersGivenName(mother.getGivenName());
            patient.setMothersSurName(mother.getSurName());
            patient.setMothersBrn(mother.getBinBrn());
            patient.setMothersNid(mother.getNid());
            patient.setMothersUid(mother.getUid());
        }

        patient.setUid(p.getUid());
        patient.setPlaceOfBirth(p.getPlaceOfBirth());
        patient.setReligion(p.getReligion());
        patient.setBloodGroup(p.getBloodGroup());
        patient.setNationality(p.getNationality());
        patient.setDisability(p.getDisability());
        patient.setEthnicity(p.getEthnicity());
        patient.setIsAlive(p.getIsAlive());

        if (address != null) {
            patient.setAddressLine(address.getAddressLine());
            patient.setDivisionId(address.getDivisionId());
            patient.setDistrictId(address.getDistrictId());
            patient.setUpazillaId(address.getUpazillaId());
            patient.setUnionId(address.getUnionId());
            patient.setHoldingNumber(address.getHoldingNumber());
            patient.setStreet(address.getStreet());
            patient.setAreaMouja(address.getAreaMouja());
            patient.setVillage(address.getVillage());
            patient.setPostOffice(address.getPostOffice());
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
            patient.setPermanentHoldingNumber(permanentAddress.getHoldingNumber());
            patient.setPermanentStreet(permanentAddress.getStreet());
            patient.setPermanentAreaMouja(permanentAddress.getAreaMouja());
            patient.setPermanentVillage(permanentAddress.getVillage());
            patient.setPermanentPostOffice(permanentAddress.getPostOffice());
            patient.setPermanentPostCode(permanentAddress.getPostCode());
            patient.setPermanentWardId(permanentAddress.getWardId());
            patient.setPermanentThanaId(permanentAddress.getThanaId());
            patient.setPermanentCityCorporationId(permanentAddress.getCityCorporationId());
            patient.setPermanentCountryCode(permanentAddress.getCountryCode());
        }

        patient.setRelations(relationsJson);

        patient.setCellNo(p.getCellNo());
        patient.setPrimaryContact(p.getPrimaryContact());
        patient.setPrimaryCellNo(p.getPrimaryCellNo());


        return patient;
    }
}
