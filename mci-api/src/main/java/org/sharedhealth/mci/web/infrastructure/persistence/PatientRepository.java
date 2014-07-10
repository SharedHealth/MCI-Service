package org.sharedhealth.mci.web.infrastructure.persistence;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.io.*;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.commons.lang3.StringUtils;
import org.sharedhealth.mci.utils.UidGenerator;
import org.sharedhealth.mci.web.exception.PatientNotFoundException;
import org.sharedhealth.mci.web.model.Address;
import org.sharedhealth.mci.web.model.Patient;
import org.sharedhealth.mci.web.utils.concurrent.SimpleListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.AsynchronousQueryListener;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.*;

@Component
public class PatientRepository {
    private static final Logger logger = LoggerFactory.getLogger(PatientRepository.class);
    private static long TIMEOUT_IN_MILLIS = 10;

    private static final UidGenerator uid = new UidGenerator();
    private CqlOperations cqlOperations;

    @Autowired
    public PatientRepository(@Qualifier("MCICassandraTemplate") CqlOperations cqlOperations) {
        this.cqlOperations = cqlOperations;
    }

    public ListenableFuture<String> create(Patient patient) {
        if (StringUtils.isBlank(patient.getHealthId())) {
            patient.setHealthId(uid.getId());
        }
        final String healthId = patient.getHealthId();
        Address address = patient.getAddress();
        Address permanentAddress = patient.getPermanentAddress();

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
                patient.getPrimaryContact(),
                patient.getFathersNameBangla(),
                patient.getFathersFirstName(),
                patient.getFathersMiddleName(),
                patient.getFathersLastName(),
                patient.getFathersBrn(),
                patient.getFathersNid(),
                patient.getFathersUid(),
                patient.getMothersNameBangla(),
                patient.getMothersFirstName(),
                patient.getMothersMiddleName(),
                patient.getMothersLastName(),
                patient.getMothersBrn(),
                patient.getMothersNid(),
                patient.getMothersUid(),
                patient.getUid(),
                patient.getPlaceOfBirth(),
                patient.getMaritalStatus(),
                patient.getMarriageId(),
                patient.getSpouseName(),
                patient.getSpouseNameBangla(),
                patient.getSpouseUidNid(),
                patient.getReligion(),
                patient.getBloodGroup(),
                patient.getNationality(),
                patient.getDisability(),
                patient.getEthnicity(),
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
                fullName.toLowerCase());

        logger.debug("Save patient CQL: [" + cql + "]");

        final SettableFuture<String> result = SettableFuture.create();

        cqlOperations.executeAsynchronously(cql, new AsynchronousQueryListener() {
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

        return new SimpleListenableFuture<String, String>(result) {
            @Override
            protected String adapt(String adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }

    public ListenableFuture<Patient> findByHealthId(final String healthId) {
        String cql = String.format(getFindByHealthIdQuery(), healthId);
        logger.debug("Find patient by health id CQL: [" + cql + "]");
        final SettableFuture<Patient> result = SettableFuture.create();

        cqlOperations.queryAsynchronously(cql, new AsynchronousQueryListener() {
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

        cqlOperations.queryAsynchronously(cql, new AsynchronousQueryListener() {
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

        cqlOperations.queryAsynchronously(cql, new AsynchronousQueryListener() {
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

        cqlOperations.queryAsynchronously(cql, new AsynchronousQueryListener() {
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

        cqlOperations.queryAsynchronously(cql, new AsynchronousQueryListener() {
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
        PatientRow row = new PatientRow(r);
        Patient patient = new Patient();
        patient.setHealthId(row.getString(HEALTH_ID));
        patient.setNationalId(row.getString(NATIONAL_ID));
        patient.setUid(row.getString(UID));
        patient.setFathersNameBangla(row.getString(FATHERS_NAME_BANGLA));
        patient.setFathersFirstName(row.getString(FATHERS_FIRST_NAME));
        patient.setFathersMiddleName(row.getString(FATHERS_MIDDLE_NAME));
        patient.setFathersLastName(row.getString(FATHERS_LAST_NAME));
        patient.setFathersUid(row.getString(FATHERS_UID));
        patient.setFathersNid(row.getString(FATHERS_NID));
        patient.setFathersBrn(row.getString(FATHERS_BRN));
        patient.setMothersNameBangla(row.getString(MOTHERS_NAME_BANGLA));
        patient.setMothersFirstName(row.getString(MOTHERS_FIRST_NAME));
        patient.setMothersMiddleName(row.getString(MOTHERS_MIDDLE_NAME));
        patient.setMothersLastName(row.getString(MOTHERS_LAST_NAME));
        patient.setMothersUid(row.getString(MOTHERS_UID));
        patient.setMothersNid(row.getString(MOTHERS_NID));
        patient.setMothersBrn(row.getString(MOTHERS_BRN));
        patient.setPlaceOfBirth(row.getString(PLACE_OF_BIRTH));
        patient.setMaritalStatus(row.getString(MARITAL_STATUS));
        patient.setMarriageId(row.getString(MARRIAGE_ID));
        patient.setSpouseNameBangla(row.getString(SPOUSE_NAME_BANGLA));
        patient.setSpouseName(row.getString(SPOUSE_NAME));
        patient.setSpouseUidNid(row.getString(SPOUSE_UID_NID));
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
        patient.setPrimaryContact(row.getString(PRIMARY_CONTACT));
        patient.setNationality(row.getString(NATIONALITY));
        patient.setDisability(row.getString(DISABILITY));
        patient.setEthnicity(row.getString(ETHNICITY));


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
        result.set(patient);
    }
}
