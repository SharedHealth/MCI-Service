package org.sharedhealth.mci.web.infrastructure.persistence;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
                patient.getFatherBrn(),
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
                permanentAddress.getCountry());

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

    private void setPatientOnResult(Row r, SettableFuture<Patient> result) throws InterruptedException, ExecutionException {
        PatientRow row = new PatientRow(r);
        Patient patient = new Patient();
        patient.setHealthId(row.getString(HEALTH_ID));
        patient.setNationalId(row.getString(NATIONAL_ID));
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

        Address address = new Address();
        address.setAddressLine(row.getString(ADDRESS_LINE));
        address.setDivisionId(row.getString(DIVISION_ID));
        address.setDistrictId(row.getString(DISTRICT_ID));
        address.setUpazillaId(row.getString(UPAZILLA_ID));
        address.setUnionId(row.getString(UNION_ID));
        patient.setAddress(address);
        result.set(patient);
    }
}
