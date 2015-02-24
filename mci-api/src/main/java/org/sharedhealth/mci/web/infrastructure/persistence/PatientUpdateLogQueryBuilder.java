package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.model.PatientUpdateLog;
import org.springframework.data.cassandra.convert.CassandraConverter;

import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.CF_PATIENT_UPDATE_LOG;
import static org.sharedhealth.mci.web.utils.JsonMapper.writeValueAsString;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

public class PatientUpdateLogQueryBuilder {

    static void buildCreateUpdateLogStmt(PatientData patientDataToSave, PatientData existingPatientData,
                                         CassandraConverter converter, Batch batch) {
        PatientUpdateLog patientUpdateLog = new PatientUpdateLog();
        String changeSet = getChangeSet(patientDataToSave, existingPatientData);

        if (changeSet != null) {
            patientUpdateLog.setEventId(timeBased());
            patientUpdateLog.setHealthId(existingPatientData.getHealthId());
            patientUpdateLog.setChangeSet(changeSet);
            batch.add(createInsertQuery(CF_PATIENT_UPDATE_LOG, patientUpdateLog, null, converter));
        }
    }

    private static String getChangeSet(PatientData newData, PatientData oldData) {
        PatientData patient = new PatientData();

        patient.setSurName(getChangedValue(newData.getSurName(), oldData.getSurName()));
        patient.setGivenName(getChangedValue(newData.getGivenName(), oldData.getGivenName()));
        patient.setGender(getChangedValue(newData.getGender(), oldData.getGender()));
        patient.setConfidential(getChangedValueIgnoreCase(newData.getConfidential(), oldData.getConfidential()));
        patient.setAddress(getChangedValue(newData.getAddress(), oldData.getAddress()));

        if (someLoggableDataChanged(patient)) {
            return writeValueAsString(patient);
        }

        return null;
    }

    static boolean someLoggableDataChanged(PatientData patient) {
        return patient.getSurName() != null
                || patient.getGivenName() != null
                || patient.getConfidential() != null
                || patient.getGender() != null
                || patient.getAddress() != null;
    }

    private static Address getChangedValue(Address newValue, Address old) {
        return newValue != null && !newValue.equals(old) ? newValue : null;
    }

    private static String getChangedValue(String newValue, String old) {
        return newValue != null && !newValue.equals(old) ? newValue : null;
    }

    private static String getChangedValueIgnoreCase(String newValue, String old) {
        return newValue != null && !newValue.equalsIgnoreCase(old) ? newValue : null;
    }
}
