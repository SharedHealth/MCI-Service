package org.mci.web.infrastructure.persistence;

import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.google.common.util.concurrent.SettableFuture;
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

    private CqlOperations cqlOperations;

    @Autowired
    public PatientRepository(@Qualifier("MCICassandraTemplate") CqlOperations cqlOperations) {
        this.cqlOperations = cqlOperations;
    }

    public ListenableFuture<String> create(Patient patient) {
        final String healthId = UUID.randomUUID().toString();
        Address address = patient.getAddress();
        String cql = String.format("INSERT into patient (health_id, full_name, gender, " +
                        "address_division_id, " +
                        "address_district_id, " +
                        "address_upazilla_id, " +
                        "address_union_id) " +
                        "values ('%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                healthId, patient.getFullName(), patient.getGender(),
                address.getDivisionId(), address.getDistrictId(), address.getUpazillaId(), address.getUnionId());
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

    public ListenableFuture<Patient> find(final String healthId) {
        String cql = String.format("SELECT * FROM patient WHERE health_id='%s'", healthId);
        logger.debug("Find patient CQL: [" + cql + "]");

        final SettableFuture<Patient> result = SettableFuture.create();

        cqlOperations.queryAsynchronously(cql, new AsynchronousQueryListener() {
            @Override
            public void onQueryComplete(ResultSetFuture rsf) {
                try {
                    Row row = rsf.get().one();
                    if (null != row) {
                        Patient patient = new Patient();
                        patient.setHealthId(row.getString("health_id"));
                        patient.setFullName(row.getString("full_name"));
                        patient.setGender(row.getString("gender"));

                        Address address = new Address();
                        address.setDivisionId(row.getString("address_division_id"));
                        address.setDistrictId(row.getString("address_district_id"));
                        address.setUpazillaId(row.getString("address_upazilla_id"));
                        address.setUnionId(row.getString("address_union_id"));
                        patient.setAddress(address);
                        result.set(patient);
                    } else {
                        result.set(null);
                    }
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
}
