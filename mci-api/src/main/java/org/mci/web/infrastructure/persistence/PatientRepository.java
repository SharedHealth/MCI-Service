package org.mci.web.infrastructure.persistence;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.util.concurrent.SettableFuture;
import org.mci.web.model.Patient;
import org.mci.web.utils.concurrent.SimpleListenableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

@Component
public class PatientRepository {

    private CqlOperations cqlOperations;

    @Autowired
    public PatientRepository(@Qualifier("MCICassandraTemplate") CqlOperations cqlOperations) {
        this.cqlOperations = cqlOperations;
    }

    public ListenableFuture<Patient> find(String healthId) {
        return new SimpleListenableFuture<Patient, ResultSet>(cqlOperations.queryAsynchronously("SELECT * FROM patient WHERE health_id='" + healthId + "';")) {
            @Override
            protected Patient adapt(ResultSet resultSet) throws ExecutionException {
                Row result = resultSet.one();
                if (null != result) {
                    Patient patient = new Patient();
                    patient.setHealthId(result.getString("health_id"));
                    return patient;
                } else {
                    return null;
                }
            }
        };
    }

    public ListenableFuture<Boolean> save(Patient patient) {
        final SettableFuture<Boolean> result = SettableFuture.create();
        cqlOperations.executeAsynchronously("INSERT into patient (healthId) values  ('" + patient.getHealthId() + "')", new Runnable() {
            @Override
            public void run() {
                result.set(Boolean.TRUE);
            }
        });
        return new SimpleListenableFuture<Boolean, Boolean>(result) {
            @Override
            protected Boolean adapt(Boolean adapteeResult) throws ExecutionException {
                return adapteeResult;
            }
        };
    }
}
