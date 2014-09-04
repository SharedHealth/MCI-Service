package org.sharedhealth.mci.web.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.utils.UidGenerator;
import org.sharedhealth.mci.web.exception.PatientAlreadyExistException;
import org.sharedhealth.mci.web.exception.PatientNotFoundException;
import org.sharedhealth.mci.web.exception.ValidationException;
import org.sharedhealth.mci.web.model.Address;
import org.sharedhealth.mci.web.model.Patient;
import org.sharedhealth.mci.web.model.Relation;
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

    private static final UidGenerator uid = new UidGenerator();
    private CassandraOperations cassandraOperations;

    @Autowired
    public PatientRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations) {
        this.cassandraOperations = cassandraOperations;
    }

    public ListenableFuture<String> create(Patient patient) {

        Patient existingPatient;

        final SettableFuture<String> result = SettableFuture.create();

        try {
            existingPatient = getExistingPatient(patient);
        } catch (ExecutionException e) {
            result.setException(e.getCause());
            return getStringListenableFuture(result);
        } catch (Exception e) {
            result.setException(e);
            return getStringListenableFuture(result);
        }

        if (StringUtils.isBlank(existingPatient.getHealthId())) {
            patient.setHealthId(uid.getId());
        }else{
            result.setException(new PatientAlreadyExistException(existingPatient.getHealthId()));
            return getStringListenableFuture(result);
        }

        final String healthId = patient.getHealthId();
        Address address = patient.getAddress();
        Address permanentAddress = patient.getPermanentAddress();
        Relation father = new Relation();
        Relation mother = new Relation();

        String relationsJson = "";
        ObjectMapper mapper = new ObjectMapper();

        try {
             List<Relation> relations = patient.getRelations();
             relationsJson =  mapper.writeValueAsString(relations);
             father = patient.getRelation("father");
             mother = patient.getRelation("mother");

        }catch(Exception e){
            logger.debug(" Relations: [" +e.getMessage() + "]");
        }
        logger.debug(" Relations: [" + patient.getRelations() + "]");

        if (permanentAddress == null ){
            permanentAddress = new Address();
        }


        String fullName = "";
        if(patient.getFirstName() != null){
             fullName = patient.getFirstName();
        }
        if(patient.getMiddleName() != null){
            fullName = fullName + " " + patient.getMiddleName();
        }
        if(patient.getLastName() != null){
            fullName = fullName + " " +patient.getLastName();
        }

        String cql = String.format(getCreateQuery(),
                healthId,
                patient.getNationalId(),
                patient.getBirthRegistrationNumber(),
                patient.getFullNameBangla(),
                patient.getFirstName(),
                patient.getMiddleName(),
                patient.getLastName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getOccupation(),
                patient.getEducationLevel(),
                father.getNameBangla(),
                father.getFirstName(),
                father.getMiddleName(),
                father.getLastName(),
                father.getBrn(),
                father.getNid(),
                father.getUid(),
                mother.getNameBangla(),
                mother.getFirstName(),
                mother.getMiddleName(),
                mother.getLastName(),
                mother.getBrn(),
                mother.getNid(),
                mother.getUid(),
                patient.getUid(),
                patient.getPlaceOfBirth(),
                patient.getReligion(),
                patient.getBloodGroup(),
                patient.getNationality(),
                patient.getDisability(),
                patient.getEthnicity(),
                patient.getIsAlive(),
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
                address.getWard(),
                address.getThana(),
                address.getCityCorporation(),
                address.getCountry(),
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
                permanentAddress.getWard(),
                permanentAddress.getThana(),
                permanentAddress.getCityCorporation(),
                permanentAddress.getCountry(),
                fullName.toLowerCase(),
                father.getType(),
                mother.getType(),
                relationsJson
          );

        logger.debug("Save patient CQL: [" + cql + "]");

        cassandraOperations.executeAsynchronously(cql, new AsynchronousQueryListener() {
            @Override
            public void onQueryComplete(ResultSetFuture rsf) {
                try {
                    rsf.get(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS);
                    result.set(healthId);
                } catch (Exception e) {
                    logger.error("Error while creating patient.", e);
                    result.setException(e);
                }
            }
        });

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

    private Patient getExistingPatient(Patient patient) throws InterruptedException, ExecutionException {

        if (!StringUtils.isBlank(patient.getHealthId())) {
            try {
                return findByHealthId(patient.getHealthId()).get();
            } catch (Exception e) {
                DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(patient, "patient");
                bindingResult.addError(new FieldError("patient", "hid", "404"));
                throw new ValidationException(bindingResult);
            }
        }

        if(!StringUtils.isBlank(patient.getNationalId())) {
            try {
                return findByNationalId(patient.getNationalId()).get();
            } catch (Exception e) {
                logger.debug("Can not find patient by [National Id");
            }
        }

        if(!StringUtils.isBlank(patient.getBirthRegistrationNumber())) {

            try {
                return findByBirthRegistrationNumber(patient.getBirthRegistrationNumber()).get();
            } catch (Exception e) {
                logger.debug("Can not find patient by [Birth Registration Number]");
            }
        }

        if(!StringUtils.isBlank(patient.getUid())) {
            try {
                return findByUid(patient.getUid()).get();
            } catch (Exception e) {
                logger.debug("Can not find patient by [UID]");
            }
        }

        return patient;
    }

    public ListenableFuture<Patient> findByHealthId(final String healthId) {
        String cql = String.format(getFindByHealthIdQuery(), healthId);
        logger.debug("Find patient by health id CQL: [" + cql + "]");
        final SettableFuture<Patient> result = SettableFuture.create();

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

        return new SimpleListenableFuture<Patient, Patient>(result) {
            @Override
            protected Patient adapt(Patient adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    public ListenableFuture<Patient> findByNationalId(final String nationalId) {
        String cql = String.format(getFindByNationalIdQuery(), nationalId);
        logger.debug("Find patient by national id CQL: [" + cql + "]");
        final SettableFuture<Patient> result = SettableFuture.create();

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

        return new SimpleListenableFuture<Patient, Patient>(result) {
            @Override
            protected Patient adapt(Patient adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    public ListenableFuture<Patient> findByBirthRegistrationNumber(final String birthRegistrationNumber) {
        String cql = String.format(getFindByBirthRegistrationNumberQuery(), birthRegistrationNumber);
        logger.debug("Find patient by birth registration number CQL: [" + cql + "]");
        final SettableFuture<Patient> result = SettableFuture.create();

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

        return new SimpleListenableFuture<Patient, Patient>(result) {
            @Override
            protected Patient adapt(Patient adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    public ListenableFuture<Patient> findByName(final String fullName) {
        String cql = String.format(getFindByNameQuery(), fullName);
        logger.debug("Find patient by name  CQL: [" + cql + "]");
        final SettableFuture<Patient> result = SettableFuture.create();

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

        return new SimpleListenableFuture<Patient, Patient>(result) {
            @Override
            protected Patient adapt(Patient adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    public ListenableFuture<Patient> findByUid(final String uid) {
        String cql = String.format(getFindByUidQuery(), uid);
        logger.debug("Find patient by name  CQL: [" + cql + "]");
        final SettableFuture<Patient> result = SettableFuture.create();

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

        return new SimpleListenableFuture<Patient, Patient>(result) {
            @Override
            protected Patient adapt(Patient adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    private void setPatientOnResult(Row r, SettableFuture<Patient> result) throws InterruptedException, ExecutionException {
        Patient patient = getPatientFromRow(r);
        result.set(patient);
    }

    private Patient getPatientFromRow(Row r) {
        DatabaseRow row = new DatabaseRow(r);
        Patient patient = new Patient();
        ObjectMapper mapper = new ObjectMapper();

        try {
            patient.setRelations(mapper.readValue(row.getString(RELATIONS),  List.class ));
        }catch(Exception e){
            logger.debug(" Relations: [" +e.getMessage() + "]");
        }

        patient.setHealthId(row.getString(HEALTH_ID));
        patient.setNationalId(row.getString(NATIONAL_ID));
        patient.setUid(row.getString(UID));
        patient.setPlaceOfBirth(row.getString(PLACE_OF_BIRTH));

        patient.setReligion(row.getString(RELIGION));
        patient.setBloodGroup(row.getString(BLOOD_GROUP));
        patient.setFullNameBangla(row.getString(FULL_NAME_BANGLA));
        patient.setBirthRegistrationNumber(row.getString(BIN_BRN));
        patient.setFirstName(row.getString(FIRST_NAME));
        patient.setMiddleName(row.getString(MIDDLE_NAME));
        patient.setLastName(row.getString(LAST_NAME));
        patient.setDateOfBirth(row.getString(DATE_OF_BIRTH));
        patient.setGender(row.getString(GENDER));
        patient.setOccupation(row.getString(OCCUPATION));
        patient.setEducationLevel(row.getString(EDU_LEVEL));
        patient.setNationality(row.getString(NATIONALITY));
        patient.setDisability(row.getString(DISABILITY));
        patient.setEthnicity(row.getString(ETHNICITY));
        patient.setIsAlive(row.getString(IS_ALIVE));

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
        address.setWard(row.getString(WARD));
        address.setThana(row.getString(THANA));
        address.setCityCorporation(row.getString(CITY_CORPORATION));
        address.setCountry(row.getString(COUNTRY));
        patient.setAddress(address);

       /* Address permanetaddress = new Address();
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
        permanetaddress.setWard(row.getString(PERMANENT_WARD));
        permanetaddress.setThana(row.getString(PERMANENT_THANA));
        permanetaddress.setCityCorporation(row.getString(PERMANENT_CITY_CORPORATION));
        permanetaddress.setCountry(row.getString(PERMANENT_COUNTRY));
        patient.setPermanentAddress(permanetaddress);*/
        return patient;
    }

    public ListenableFuture<List<Patient>> findAllByQuery(MultiValueMap<String, String> parameters) {

        Select select = QueryBuilder.select().from("patient");

        if(parameters.get("full_name") != null) {
            select.where(QueryBuilder.eq("full_name", parameters.get("full_name").get(0)));
        }

        return new SimpleListenableFuture<List<Patient>, ResultSet>(
                cassandraOperations.queryAsynchronously(select)) {
            @Override
            protected List<Patient> adapt(ResultSet resultSet) throws ExecutionException {
                List<Patient> patients = new ArrayList<>();
                for (Row result : resultSet.all()) {
                    Patient patient = getPatientFromRow(result);
                    patients.add(patient);
                }

                return patients;
            }
        };
    }

    public ListenableFuture<String> update(Patient patient, final String hid){
        final SettableFuture<String> result = SettableFuture.create();
        Address address = patient.getAddress();
        Address permanentAddress = patient.getPermanentAddress();

        Relation father = patient.getRelation("father");
        Relation mother = patient.getRelation("mother");


        if (permanentAddress == null ){
            permanentAddress = new Address();
        }
        String fullName = "";
        if(patient.getFirstName() != null){
            fullName = patient.getFirstName();
        }
        if(patient.getMiddleName() != null){
            fullName = fullName + " " + patient.getMiddleName();
        }
        if(patient.getLastName() != null){
            fullName = fullName + " " +patient.getLastName();
        }

        String cql = String.format(getUpdateQuery(),
                patient.getNationalId(),
                patient.getBirthRegistrationNumber(),
                patient.getFullNameBangla(),
                patient.getFirstName(),
                patient.getMiddleName(),
                patient.getLastName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getOccupation(),
                patient.getEducationLevel(),
                father.getNameBangla(),
                father.getFirstName(),
                father.getMiddleName(),
                father.getLastName(),
                father.getBrn(),
                father.getNid(),
                father.getUid(),
                mother.getNameBangla(),
                mother.getFirstName(),
                mother.getMiddleName(),
                mother.getLastName(),
                mother.getBrn(),
                mother.getNid(),
                mother.getUid(),
                patient.getUid(),
                patient.getPlaceOfBirth(),
                patient.getReligion(),
                patient.getBloodGroup(),
                patient.getNationality(),
                patient.getDisability(),
                patient.getEthnicity(),
                patient.getIsAlive(),
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
                address.getWard(),
                address.getThana(),
                address.getCityCorporation(),
                address.getCountry(),
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
                permanentAddress.getWard(),
                permanentAddress.getThana(),
                permanentAddress.getCityCorporation(),
                permanentAddress.getCountry(),
                fullName.toLowerCase(),
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
}
