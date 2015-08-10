package org.sharedhealth.mci.deduplication.repository;

import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Insert;
import org.apache.commons.lang3.builder.Diff;
import org.sharedhealth.mci.deduplication.model.DuplicatePatient;
import org.sharedhealth.mci.deduplication.model.DuplicatePatientIgnored;
import org.sharedhealth.mci.domain.model.Catchment;
import org.sharedhealth.mci.domain.model.PatientData;
import org.sharedhealth.mci.domain.repository.BaseRepository;
import org.sharedhealth.mci.domain.repository.PatientRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.batch;
import static com.datastax.driver.core.querybuilder.QueryBuilder.timestamp;
import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.sharedhealth.mci.deduplication.repository.DuplicatePatientQueryBuilder.*;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.CF_PATIENT_DUPLICATE;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.DUPLICATE_PATIENT_MARKER;
import static org.sharedhealth.mci.domain.repository.MarkerRepositoryQueryBuilder.buildUpdateMarkerBatch;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertBatchQuery;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

@Component
public class DuplicatePatientRepository extends BaseRepository {

    private static final Logger logger = getLogger(DuplicatePatientRepository.class);
    public static final int BATCH_QUERY_EXEC_DELAY = 100;

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
    public List<DuplicatePatient> findByCatchment(Catchment catchment, UUID after, UUID before, int limit) {
        return cassandraOps.select(buildFindByCatchmentStmt(catchment, after, before, limit), DuplicatePatient.class);
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
            buildRetireBatch(patient1, patient1.getCatchment(), batch);

        } else {
            Set<String> reasons = findReasonsForDuplicates(duplicatePatients);
            buildCreateIgnoreDuplicatesStmt(healthId1, healthId2, reasons, cassandraOps.getConverter(), batch);
            buildDeleteDuplicatesStmt(patient1, patient2, cassandraOps.getConverter(), batch);
        }
        cassandraOps.execute(batch);
    }

    private void buildDeleteDuplicatesStmt(PatientData patient1, PatientData patient2, CassandraConverter converter,
                                           Batch batch) {
        String healthId1 = patient1.getHealthId();
        String healthId2 = patient2.getHealthId();

        buildDeleteDuplicateBatch(patient1.getCatchment(), healthId1, healthId2, batch);
        buildDeleteDuplicateBatch(patient2.getCatchment(), healthId2, healthId1, batch);
    }

    private void buildDeleteDuplicateBatch(Catchment catchment, String healthId1, String healthId2, Batch batch) {
        List<DuplicatePatient> duplicates = findByCatchmentAndHealthIds(catchment, healthId1, healthId2);
        DuplicatePatientQueryBuilder
                .buildDeleteDuplicatesStmt(duplicates, cassandraOps.getConverter(), batch, getCurrentTimeInMicros());
    }

    private void buildRetireBatch(PatientData patient1, Catchment catchment, Batch batch) {
        buildRetireBatch(patient1, catchment, batch, getCurrentTimeInMicros());
    }

    private void buildRetireBatch(PatientData patient, Catchment oldCatchment, Batch batch, long timestamp) {
        String healthId1 = patient.getHealthId();
        List<DuplicatePatient> duplicates = findByCatchmentAndHealthId(oldCatchment, healthId1);
        Set<String> healthId2List = findHealthId2List(duplicates);
        duplicates.addAll(findDuplicatesByHealthIds(healthId2List, healthId1));
        DuplicatePatientQueryBuilder
                .buildDeleteDuplicatesStmt(duplicates, cassandraOps.getConverter(), batch, timestamp);
    }

    public Set<String> findHealthId2List(List<DuplicatePatient> duplicates) {
        Set<String> healthIds = new HashSet<>();
        for (DuplicatePatient duplicate : duplicates) {
            healthIds.add(duplicate.getHealth_id2());
        }
        return healthIds;
    }

    private List<DuplicatePatient> findDuplicatesByHealthIds(Set<String> healthId1List, String healthId2) {
        List<DuplicatePatient> duplicates = new ArrayList<>();
        for (String healthId1 : healthId1List) {
            PatientData patient = patientRepository.findByHealthId(healthId1);
            duplicates.addAll(findByCatchmentAndHealthIds(patient.getCatchment(), healthId1, healthId2));
        }
        return duplicates;
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

    public List<DuplicatePatient> findDuplicatePatients(PatientData patient1, PatientData patient2) {
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
        String cql;
        List<DuplicatePatient> duplicates = new ArrayList<>();
        for (String catchmentId : catchment.getAllIds()) {
            cql = buildFindByCatchmentAndHealthIdsStmt(catchmentId, healthId1, healthId2);
            duplicates.addAll(cassandraOps.select(cql, DuplicatePatient.class));
        }
        return duplicates;
    }

    public Set<String> findReasonsForDuplicates(List<DuplicatePatient> duplicatePatients) {
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
        String cql;
        List<DuplicatePatient> duplicates = new ArrayList<>();
        for (String catchmentId : catchment.getAllIds()) {
            cql = buildFindByCatchmentAndHealthIdStmt(catchmentId, healthId1);
            duplicates.addAll(cassandraOps.select(cql, DuplicatePatient.class));
        }
        return duplicates;
    }

    public void create(List<DuplicatePatient> duplicates, UUID marker) {
        CassandraConverter converter = cassandraOps.getConverter();
        Batch batch = createInsertBatchQuery(CF_PATIENT_DUPLICATE, duplicates, null, converter);
        buildUpdateMarkerBatch(DUPLICATE_PATIENT_MARKER, marker.toString(), converter, batch);
        cassandraOps.execute(batch);
    }

    public void retire(String healthId, UUID marker) {
        PatientData patient = patientRepository.findByHealthId(healthId);
        Batch batch = batch();
        buildRetireBatch(patient, patient.getCatchment(), batch);
        buildUpdateMarkerBatch(DUPLICATE_PATIENT_MARKER, marker.toString(), cassandraOps.getConverter(), batch);
        cassandraOps.execute(batch);
    }

    public void update(String healthId, Catchment oldCatchment, List<DuplicatePatient> newDuplicates, UUID marker) {
        CassandraConverter converter = cassandraOps.getConverter();
        Batch batch = batch();
        long currentTimeMicros = getCurrentTimeInMicros();

        PatientData patient = patientRepository.findByHealthId(healthId);
        if (oldCatchment == null) {
            oldCatchment = patient.getCatchment();
        }
        buildRetireBatch(patient, oldCatchment, batch, currentTimeMicros);

        for (DuplicatePatient duplicate : findDuplicatesWithIgnoredRemoved(newDuplicates)) {
            Insert insertQuery = createInsertQuery(CF_PATIENT_DUPLICATE, duplicate, null, converter);
            insertQuery.using(timestamp(currentTimeMicros + BATCH_QUERY_EXEC_DELAY));
            batch.add(insertQuery);
        }

        buildUpdateMarkerBatch(DUPLICATE_PATIENT_MARKER, marker.toString(), converter, batch);
        cassandraOps.execute(batch);
    }

    public List<DuplicatePatient> findDuplicatesWithIgnoredRemoved(List<DuplicatePatient> duplicates) {
        List<DuplicatePatient> duplicatesWithIgnoredRemoved = new ArrayList<>();
        DuplicatePatientIgnored duplicateIgnored;
        for (DuplicatePatient duplicate : duplicates) {
            duplicateIgnored = findIgnoredDuplicates(duplicate.getHealth_id1(), duplicate.getHealth_id2());
            if (duplicateIgnored == null) {
                duplicatesWithIgnoredRemoved.add(duplicate);
            }
        }
        return duplicatesWithIgnoredRemoved;
    }

    private DuplicatePatientIgnored findIgnoredDuplicates(String healthId1, String healthId2) {
        String cql = buildFindIgnoreDuplicatesStmt(healthId1, healthId2);
        return cassandraOps.selectOne(cql, DuplicatePatientIgnored.class);
    }
}
