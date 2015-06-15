package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Insert;
import org.apache.commons.lang3.builder.Diff;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.model.DuplicatePatient;
import org.sharedhealth.mci.web.model.DuplicatePatientIgnored;
import org.sharedhealth.mci.web.model.Marker;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.batch;
import static com.datastax.driver.core.querybuilder.QueryBuilder.timestamp;
import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.sharedhealth.mci.web.infrastructure.persistence.DuplicatePatientQueryBuilder.*;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.*;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertBatchQuery;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

@Component
public class DuplicatePatientRepository extends BaseRepository {

    private static final Logger logger = getLogger(DuplicatePatientRepository.class);
    public static final int BATCH_QUERY_EXEC_DELAY = 1;

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
            buildRetireBatch(patient1, batch);

        } else {
            Set<String> reasons = findReasonsForDuplicates(duplicatePatients);
            buildCreateIgnoreDuplicatesStmt(healthId1, healthId2, reasons, cassandraOps.getConverter(), batch);
            buildDeleteDuplicatesStmt(patient1, patient2, cassandraOps.getConverter(), batch);
        }
        cassandraOps.execute(batch);
    }

    private void buildRetireBatch(PatientData patient, Batch batch) {
        List<DuplicatePatient> duplicates = findDuplicatesByPatient1(patient);
        Set<String> healthId2List = findHealthId2List(duplicates);
        duplicates.addAll(findDuplicatesByHealthId2(healthId2List));
        buildDeleteDuplicatesStmt(duplicates, cassandraOps.getConverter(), batch);
    }

    private List<DuplicatePatient> findDuplicatesByHealthId1(String healthId) {
        PatientData patient = patientRepository.findByHealthId(healthId);
        return findDuplicatesByPatient1(patient);
    }

    private List<DuplicatePatient> findDuplicatesByPatient1(PatientData patient) {
        return findByCatchmentAndHealthId(patient.getCatchment(), patient.getHealthId());
    }

    Set<String> findHealthId2List(List<DuplicatePatient> duplicates) {
        Set<String> healthIds = new HashSet<>();
        for (DuplicatePatient duplicate : duplicates) {
            healthIds.add(duplicate.getHealth_id2());
        }
        return healthIds;
    }

    private List<DuplicatePatient> findDuplicatesByHealthId2(Set<String> healthId2List) {
        List<DuplicatePatient> duplicates = new ArrayList<>();
        for (String healthId2 : healthId2List) {
            duplicates.addAll(findDuplicatesByHealthId1(healthId2));
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

    public void create(List<DuplicatePatient> duplicates, UUID marker) {
        CassandraConverter converter = cassandraOps.getConverter();
        Batch batch = createInsertBatchQuery(CF_PATIENT_DUPLICATE, duplicates, null, converter);
        batch.add(buildCreateMarkerStmt(marker.toString(), converter));
        cassandraOps.execute(batch);
    }

    public void retire(String healthId, UUID marker) {
        PatientData patient = patientRepository.findByHealthId(healthId);
        Batch batch = batch();
        buildRetireBatch(patient, batch);
        batch.add(buildCreateMarkerStmt(marker.toString(), cassandraOps.getConverter()));
        cassandraOps.execute(batch);
    }

    public void update(String healthId, List<DuplicatePatient> duplicates, UUID marker) {
        CassandraConverter converter = cassandraOps.getConverter();
        Batch batch = batch();
        long batchTimestamp = currentTimeMillis();

        PatientData patient = patientRepository.findByHealthId(healthId);
        buildRetireBatch(patient, batch);

        for (DuplicatePatient duplicate : findDuplicatesWithIgnoredRemoved(duplicates)) {
            Insert insertQuery = createInsertQuery(CF_PATIENT_DUPLICATE, duplicate, null, converter);
            insertQuery.using(timestamp(batchTimestamp + BATCH_QUERY_EXEC_DELAY));
            batch.add(insertQuery);
        }

        batch.add(buildCreateMarkerStmt(marker.toString(), converter));
        cassandraOps.execute(batch);
    }

    List<DuplicatePatient> findDuplicatesWithIgnoredRemoved(List<DuplicatePatient> duplicates) {
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

    private Insert buildCreateMarkerStmt(String value, CassandraConverter converter) {
        Marker marker = new Marker();
        marker.setType(DUPLICATE_PATIENT_MARKER);
        marker.setCreatedAt(timeBased());
        marker.setMarker(value);
        return createInsertQuery(CF_MARKER, marker, null, converter);
    }
}
