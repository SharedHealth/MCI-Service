package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import org.apache.commons.lang3.builder.Diff;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.datastax.driver.core.querybuilder.QueryBuilder.batch;
import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientQueryBuilder.*;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class DuplicatePatientRepository extends BaseRepository {

    private static final Logger logger = getLogger(DuplicatePatientRepository.class);

    private PatientRepository patientRepository;

    @Autowired
    public DuplicatePatientRepository(PatientRepository patientRepository,
                                      @Qualifier("MCICassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
        this.patientRepository = patientRepository;
    }

    /**
     * Finds by exact catchment id.
     */
    public List<DuplicatePatient> findByCatchment(Catchment catchment) {
        return cassandraOps.select(buildFindByCatchmentStmt(catchment), DuplicatePatient.class);
    }

    public void processDuplicates(PatientData patientData1, PatientData patientData2, boolean isMerged) {
        String healthId1 = patientData1.getHealthId();
        String healthId2 = patientData2.getHealthId();

        PatientData patient1 = patientRepository.findByHealthId(healthId1);
        PatientData patient2 = patientRepository.findByHealthId(healthId2);

        List<DuplicatePatient> duplicatePatients = findDuplicatePatients(patient1, patient2);
        if (isEmpty(duplicatePatients)) {
            handleIllegalArgument(format("Duplicates don't exist for health IDs %s & %s in db. Cannot merge.",
                    patient1.getHealthId(), patient2.getHealthId()));
        }

        Batch batch = batch();
        if (isMerged) {
            validateMergedData(patientData2, patient2, patient1);
            patientRepository.buildUpdateProcessBatch(patientData1, healthId1, batch);
            patientRepository.buildUpdateProcessBatch(patientData2, healthId2, batch);
            buildDeleteDuplicatesStmt(patient1, batch);

        } else {
            Set<String> reasons = findReasonsForDuplicates(duplicatePatients);
            buildCreateIgnoreDuplicatesStmt(healthId1, healthId2, reasons, cassandraOps.getConverter(), batch);
        }

        buildDeleteDuplicatesStmt(patient1, patient2, cassandraOps.getConverter(), batch);
        cassandraOps.execute(batch);
    }

    private void validateMergedData(PatientData patientData2, PatientData patient2, PatientData patient1) {
        for (Diff<?> diff : patient2.diff(patientData2)) {
            Object value = patient1.getValue(diff.getFieldName());
            if (!diff.getValue().equals(value)) {
                handleIllegalArgument(format("Patient 2 [hid: %s] not merged from patient 1 [hid: %s]",
                        patient2.getHealthId(), patient1.getHealthId()));
            }
        }
    }

    private void handleIllegalArgument(String message) {
        logger.error(message);
        throw new IllegalArgumentException(message);
    }

    List<DuplicatePatient> findDuplicatePatients(PatientData patient1, PatientData patient2) {
        String healthId1 = patient1.getHealthId();
        String healthId2 = patient2.getHealthId();

        List<DuplicatePatient> duplicatePatients1 = findByCatchmentAndHealthIds(patient1.getCatchment(), healthId1, healthId2);
        if (isNotEmpty(duplicatePatients1)) {
            return duplicatePatients1;
        }

        List<DuplicatePatient> duplicatePatients2 = findByCatchmentAndHealthIds(patient2.getCatchment(), healthId2, healthId1);
        if (isNotEmpty(duplicatePatients2)) {
            return duplicatePatients2;
        }

        return null;
    }

    /**
     * Finds by all possible catchment ids, healthId1 and healthId2.
     * All possible catchment ids for catchment 1020304050 are 1020, 102030, 10203040, 1020304050.
     */
    public List<DuplicatePatient> findByCatchmentAndHealthIds(Catchment catchment, String healthId1, String healthId2) {
        return cassandraOps.select(buildFindByCatchmentAndHealthIdsStmt(catchment, healthId1, healthId2), DuplicatePatient.class);
    }

    Set<String> findReasonsForDuplicates(List<DuplicatePatient> duplicatePatients) {
        if (isNotEmpty(duplicatePatients)) {
            return duplicatePatients.get(0).getReasons();
        }
        return null;
    }

    /**
     * Finds by all possible catchment ids and healthId1.
     * All possible catchment ids for catchment 1020304050 are 1020, 102030, 10203040, 1020304050.
     */
    public List<DuplicatePatient> findByCatchmentAndHealthId(Catchment catchment, String healthId1) {
        return cassandraOps.select(buildFindByCatchmentAndHealthIdStmt(catchment, healthId1), DuplicatePatient.class);
    }
}
