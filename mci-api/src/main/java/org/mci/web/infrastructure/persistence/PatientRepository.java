package org.mci.web.infrastructure.persistence;

import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.google.common.util.concurrent.SettableFuture;
import org.apache.commons.lang3.StringUtils;
import org.mci.web.model.Address;
import org.mci.web.model.Patient;
import org.mci.web.utils.concurrent.SimpleListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.AsynchronousQueryListener;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mci.web.infrastructure.persistence.PatientQueryBuilder.*;

@Component
public class PatientRepository {
    private static final Logger logger = LoggerFactory.getLogger(PatientRepository.class);
    private static long TIMEOUT_IN_MILLIS = 10;


    private CqlOperations cqlOperations;

    @Autowired
    public PatientRepository(@Qualifier("MCICassandraTemplate") CqlOperations cqlOperations) {
        this.cqlOperations = cqlOperations;
    }

    public ListenableFuture<String> create(Patient patient) {
        if (StringUtils.isBlank(patient.getHealthId())) {
            patient.setHealthId(UUID.randomUUID().toString());
        }
        final String healthId = patient.getHealthId();
        Address address = patient.getAddress();
        String cql = String.format(getCreateQuery(),
                healthId,
                patient.getNationalId(),
                patient.getFirstName(),
                patient.getMiddleName(),
                patient.getLastName(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getOccupation(),
                patient.getEducationLevel(),
                patient.getPrimaryContact(),
                address.getAddressLine(),
                address.getDivisionId(),
                address.getDistrictId(),
                address.getUpazillaId(),
                address.getUnionId());
        logger.debug("Save patient CQL: [" + cql + "]");

        final SettableFuture<String> result = SettableFuture.create();

        cqlOperations.executeAsynchronously(cql, new AsynchronousQueryListener() {
            @Override
            public void onQueryComplete(ResultSetFuture rsf) {
                try {
                    rsf.get(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS);
                    result.set(healthId);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
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
                    setPatientOnResult(rsf.get(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS).one(), result);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    logger.error("Error while finding patient by healthId: " + healthId, e);
                    result.set(null);
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
                    setPatientOnResult(rsf.get(TIMEOUT_IN_MILLIS, TimeUnit.MILLISECONDS).one(), result);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    logger.error("Error while finding patient by nationalId: " + nationalId, e);
                    result.set(null);
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
        if (r == null) {
            result.set(null);
            return;
        }
        PatientRow row = new PatientRow(r);
        Patient patient = new Patient();
        patient.setHealthId(row.getString(HEALTH_ID));
        patient.setNationalId(row.getString(NATIONAL_ID));
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
