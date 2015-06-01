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

import static com.datastax.driver.core.querybuilder.QueryBuilder.batch;
import static java.lang.String.format;
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

    public List<DuplicatePatient> findAllByCatchment(Catchment catchment) {
        return cassandraOps.select(buildFindByCatchmentStmt(catchment), DuplicatePatient.class);
    }

    public void processDuplicates(PatientData patientData1, PatientData patientData2, boolean isMerged) {
        String healthId1 = patientData1.getHealthId();
        String healthId2 = patientData2.getHealthId();

        PatientData patient1 = patientRepository.findByHealthId(healthId1);
        PatientData patient2 = patientRepository.findByHealthId(healthId2);
        validateExistingPatientsForMerge(patient1, patient2);

        Batch batch = batch();
        if (isMerged) {
            validateMergedData(patientData2, patient2, patient1);
            patientRepository.buildUpdateProcessBatch(patientData1, healthId1, batch);
            patientRepository.buildUpdateProcessBatch(patientData2, healthId2, batch);
            buildDeleteDuplicatesStmt(patient1, batch);
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

    private void validateExistingPatientsForMerge(PatientData patient1, PatientData patient2) {
        if (!duplicatePatientExists(patient1, patient2)) {
            handleIllegalArgument(format("Duplicates don't exist for health IDs %s & %s in db. Cannot merge.",
                    patient1.getHealthId(), patient2.getHealthId()));
        }
    }

    private void handleIllegalArgument(String message) {
        logger.error(message);
        throw new IllegalArgumentException(message);
    }

    boolean duplicatePatientExists(PatientData patient1, PatientData patient2) {
        String healthId1 = patient1.getHealthId();
        String healthId2 = patient2.getHealthId();
        List<DuplicatePatient> duplicatePatients1 = findByCatchmentAndHealthIds(patient1.getCatchment(), healthId1, healthId2);
        List<DuplicatePatient> duplicatePatients2 = findByCatchmentAndHealthIds(patient1.getCatchment(), healthId1, healthId2);
        return isNotEmpty(duplicatePatients1) || isNotEmpty(duplicatePatients2);
    }

    public List<DuplicatePatient> findByCatchmentAndHealthIds(Catchment catchment, String healthId1, String healthId2) {
        return cassandraOps.select(buildFindByCatchmentAndHealthIdsStmt(catchment, healthId1, healthId2), DuplicatePatient.class);
    }
}
