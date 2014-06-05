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

@Component
public class PatientRepository {
    private static final Logger logger = LoggerFactory.getLogger(PatientRepository.class);

    private static final String CREATE_CQL = "INSERT into patient (" +
            "health_id, " +
            "national_id, " +
            "first_name, " +
            "middle_name, " +
            "last_name, " +
            "date_of_birth, " +
            "gender, " +
            "occupation, " +
            "edu_level, " +
            "primary_contact, " +
            "address_line, " +
            "division_id, " +
            "district_id, " +
            "upazilla_id, " +
            "union_id) " +
            "values ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')";


    private static final String FIND_BY_HEALTH_ID_CQL = "SELECT * FROM patient WHERE health_id='%s'";
    private static final String FIND_BY_NATIONAL_ID_CQL = "SELECT * FROM patient WHERE national_id='%s'";

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
        String cql = String.format(CREATE_CQL,
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
                result.set(healthId);
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
        String cql = String.format(FIND_BY_HEALTH_ID_CQL, healthId);
        logger.debug("Find patient by health id CQL: [" + cql + "]");
        final SettableFuture<Patient> result = SettableFuture.create();

        cqlOperations.queryAsynchronously(cql, new AsynchronousQueryListener() {
            @Override
            public void onQueryComplete(ResultSetFuture rsf) {
                try {
                    setPatientOnResult(rsf.get().one(), result);
                } catch (InterruptedException | ExecutionException e) {
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
        String cql = String.format(FIND_BY_NATIONAL_ID_CQL, nationalId);
        logger.debug("Find patient by national id CQL: [" + cql + "]");
        final SettableFuture<Patient> result = SettableFuture.create();

        cqlOperations.queryAsynchronously(cql, new AsynchronousQueryListener() {
            @Override
            public void onQueryComplete(ResultSetFuture rsf) {
                try {
                    setPatientOnResult(rsf.get().one(), result);
                } catch (InterruptedException | ExecutionException e) {
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
        patient.setHealthId(row.getString("health_id"));
        patient.setNationalId(row.getString("national_id"));
        patient.setFirstName(row.getString("first_name"));
        patient.setMiddleName(row.getString("middle_name"));
        patient.setLastName(row.getString("last_name"));
        patient.setDateOfBirth(row.getString("date_of_birth"));
        patient.setGender(row.getString("gender"));
        patient.setOccupation(row.getString("occupation"));
        patient.setEducationLevel(row.getString("edu_level"));
        patient.setPrimaryContact(row.getString("primary_contact"));

        Address address = new Address();
        address.setAddressLine(row.getString("address_line"));
        address.setDivisionId(row.getString("division_id"));
        address.setDistrictId(row.getString("district_id"));
        address.setUpazillaId(row.getString("upazilla_id"));
        address.setUnionId(row.getString("union_id"));
        patient.setAddress(address);
        result.set(patient);
    }
}
