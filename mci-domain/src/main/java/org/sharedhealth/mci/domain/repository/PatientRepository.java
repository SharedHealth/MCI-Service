package org.sharedhealth.mci.domain.repository;

import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.sharedhealth.mci.domain.exception.Forbidden;
import org.sharedhealth.mci.domain.exception.InvalidRequestException;
import org.sharedhealth.mci.domain.exception.PatientNotFoundException;
import org.sharedhealth.mci.domain.model.*;
import org.sharedhealth.mci.domain.service.PendingApprovalFilter;
import org.sharedhealth.mci.domain.util.TimeUuidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.convert.CassandraConverter;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.domain.constant.MCIConstants.HID_CARD_STATUS_REGISTERED;
import static org.sharedhealth.mci.domain.constant.MCIConstants.PATIENT_STATUS_ALIVE;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;
import static org.sharedhealth.mci.domain.repository.PatientAuditLogQueryBuilder.buildCreateAuditLogStmt;
import static org.sharedhealth.mci.domain.repository.PatientQueryBuilder.*;
import static org.sharedhealth.mci.domain.repository.PatientUpdateLogQueryBuilder.buildCreateUpdateLogStmt;
import static org.sharedhealth.mci.domain.util.TimeUuidUtil.getTimeFromUUID;
import static org.springframework.data.cassandra.core.CassandraTemplate.createDeleteQuery;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;


@Component
public class PatientRepository extends BaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(PatientRepository.class);

    private static final long QUERY_EXEC_DELAY = 1;
    private static final String ALL_FIELDS = "ALL_FIELDS";

    private PendingApprovalFilter pendingApprovalFilter;
    private PatientMapper mapper;

    @Autowired
    public PatientRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations,
                             PatientMapper mapper, PendingApprovalFilter pendingApprovalFilter) {
        super(cassandraOperations);
        this.mapper = mapper;
        this.pendingApprovalFilter = pendingApprovalFilter;
    }

    public MCIResponse create(PatientData patientData) {
        Patient patient = mapper.map(patientData, new PatientData());
        UUID createdAt = TimeUuidUtil.uuidForDate(new Date());
        patient.setCreatedAt(createdAt);
        patient.setUpdatedAt(createdAt);

        Requester requester = patientData.getRequester();
        patient.setCreatedBy(requester);
        patient.setUpdatedBy(requester);

        patient.setActive(true);
        if (isBlank(patient.getStatus())) patient.setStatus(PATIENT_STATUS_ALIVE);
        if (patient.getConfidential() == null) patient.setConfidential(false);
        if (isBlank(patient.getHidCardStatus())) patient.setHidCardStatus(HID_CARD_STATUS_REGISTERED);

        Map<String, Set<Requester>> requestedBy = new HashMap<>();
        buildRequestedBy(requestedBy, ALL_FIELDS, requester);

        CassandraConverter converter = cassandraOps.getConverter();
        Batch batch = batch();
        batch.add(createInsertQuery(CF_PATIENT, patient, null, converter));
        buildCreateCatchmentMappingsStmt(patientData.getCatchment(), createdAt, patientData.getHealthId(), converter, batch);
        buildCreateAuditLogStmt(patientData, requestedBy, converter, batch);
        addToPatientUpdateLogStmt(patient, requestedBy, converter, batch);

        cassandraOps.execute(batch);
        return new MCIResponse(patient.getHealthId(), HttpStatus.CREATED);
    }

    public MCIResponse update(PatientData newPatientData, PatientData existingPatient, Requester requestedBy) {
        Batch batch = buildUpdateProcessBatch(newPatientData, existingPatient, requestedBy);
        if (batch != null) {
            cassandraOps.execute(batch);
        }
        return new MCIResponse(existingPatient.getHealthId(), HttpStatus.ACCEPTED);
    }

    public Batch buildUpdateProcessBatch(PatientData patientDataToBeUpdated, PatientData existingPatientData, Requester requester) {
        Batch batch = batch();
        Patient newPatient = mapper.map(patientDataToBeUpdated, existingPatientData);

        newPatient.setHealthId(existingPatientData.getHealthId());
        newPatient.setUpdatedAt(TimeUuidUtil.uuidForDate(new Date()));
        newPatient.setUpdatedBy(requester);

        clearPendingApprovalsIfRequired(patientDataToBeUpdated, existingPatientData, batch);
        buildUpdatePendingApprovalsBatch(newPatient, existingPatientData, batch);
        buildUpdateBatch(newPatient, existingPatientData, cassandraOps.getConverter(), batch);
        Map<String, Set<Requester>> requestedBy = new HashMap<>();
        buildRequestedBy(requestedBy, ALL_FIELDS, requester);
        buildCreateUpdateLogStmt(patientDataToBeUpdated, existingPatientData, requestedBy, null, cassandraOps.getConverter(), batch);
        PatientAuditLogQueryBuilder.buildUpdateAuditLogStmt(patientDataToBeUpdated, existingPatientData, requestedBy, null, cassandraOps
                .getConverter(), batch);
        return batch;
    }

    public Batch buildUpdateProcessBatch(PatientData updateRequest, String healthId, Batch batch) {
        logger.debug(String.format("Update patient: %s", healthId));
        updateRequest.setHealthId(healthId);
        Requester requester = updateRequest.getRequester();

        PatientData existingPatientData = this.findByHealthId(healthId);

        if (Boolean.FALSE.equals(existingPatientData.isActive())) {
            String mergedWith = existingPatientData.getMergedWith();
            String errorMessage = mergedWith != null ? String.format("Cannot update inactive patient, already merged with %s", mergedWith) : "Cannot update inactive patient";
            throw new InvalidRequestException(errorMessage);
        }

        checkIfTryingToMergeWithNonExistingOrInactiveHid(updateRequest.getMergedWith());
        clearPendingApprovalsIfRequired(updateRequest, existingPatientData, batch);
        PatientData newPatientData = this.pendingApprovalFilter.filter(existingPatientData, updateRequest);

        Patient newPatient = mapper.map(newPatientData, existingPatientData);
        newPatient.setHealthId(healthId);
        newPatient.setUpdatedAt(TimeUuidUtil.uuidForDate(new Date()));
        newPatient.setUpdatedBy(requester);

        buildUpdatePendingApprovalsBatch(newPatient, existingPatientData, batch);
        buildUpdateBatch(newPatient, existingPatientData, cassandraOps.getConverter(), batch);
        Map<String, Set<Requester>> requestedBy = new HashMap<>();
        buildRequestedBy(requestedBy, ALL_FIELDS, requester);
        buildCreateUpdateLogStmt(newPatientData, existingPatientData, requestedBy, null, cassandraOps.getConverter(), batch);
        PatientAuditLogQueryBuilder.buildUpdateAuditLogStmt(newPatientData, existingPatientData, requestedBy, null, cassandraOps
                .getConverter(), batch);
        return batch;
    }

    private void clearPendingApprovalsIfRequired(PatientData updateRequest, PatientData existingPatientData, Batch batch) {
        if (null == updateRequest.isActive() || updateRequest.isActive()) {
            return;
        }
        existingPatientData.setPendingApprovals(new TreeSet<PendingApproval>());
        buildDeletePendingApprovalMappingStmt(existingPatientData.getHealthId(), batch, new Date().getTime());
    }

    private boolean checkIfTryingToMergeWithNonExistingOrInactiveHid(String mergedWith) {
        if (null == mergedWith) {
            return false;
        }
        PatientData targetPatient;
        try {
            targetPatient = this.findByHealthId(mergedWith);
        } catch (PatientNotFoundException e) {
            throw new PatientNotFoundException("Merge_with patient not found with health id: " + mergedWith);
        }
        if (!targetPatient.isActive()) {
            throw new Forbidden("Cannot merge with inactive patient");
        }
        return false;
    }

    private Batch buildUpdatePendingApprovalsBatch(Patient newPatient, PatientData existingPatientData, Batch batch) {
        TreeSet<PendingApproval> newPendingApprovals = newPatient.getPendingApprovals();

        if (isNotEmpty(newPendingApprovals)) {
            TreeSet<PendingApproval> existingPendingApprovals = existingPatientData.getPendingApprovals();
            String healthId = newPatient.getHealthId();

            long timestamp = new Date().getTime();
            if (existingPendingApprovals != null && existingPendingApprovals.size() > 0) {
                buildDeletePendingApprovalMappingStmt(healthId, batch, timestamp);
            }

            UUID uuid = findLatestUuid(newPatient.getPendingApprovals());
            buildCreatePendingApprovalMappingStmt(newPatient.getCatchment(), healthId, uuid, batch, timestamp + QUERY_EXEC_DELAY);
        }
        return batch;
    }

    private void buildDeletePendingApprovalMappingStmt(String healthId, Batch batch, long timestamp) {
        String cql = select().from(CF_PENDING_APPROVAL_MAPPING).where(eq(HEALTH_ID, healthId)).toString();
        List<PendingApprovalMapping> mappings = cassandraOps.select(cql, PendingApprovalMapping.class);
        for (PendingApprovalMapping mapping : mappings) {
            Delete deleteQuery = createDeleteQuery(CF_PENDING_APPROVAL_MAPPING, mapping, null, cassandraOps.getConverter());
            deleteQuery.using(timestamp(timestamp));
            batch.add(deleteQuery);
        }
    }

    private void buildCreatePendingApprovalMappingStmt(Catchment catchment, String healthId, UUID uuid, Batch batch, long timestamp) {
        List<PendingApprovalMapping> mappings = buildPendingApprovalMappings(catchment, healthId, uuid);
        for (PendingApprovalMapping mapping : mappings) {
            Insert insertQuery = createInsertQuery(CF_PENDING_APPROVAL_MAPPING, mapping, null, cassandraOps.getConverter());
            insertQuery.using(timestamp(timestamp));
            batch.add(insertQuery);
        }
    }

    public UUID findLatestUuid(TreeSet<PendingApproval> pendingApprovals) {
        UUID latest = null;
        for (PendingApproval pendingApproval : pendingApprovals) {
            UUID uuid = pendingApproval.getFieldDetails().keySet().iterator().next();
            if (latest == null) {
                latest = uuid;
                continue;
            }
            if (isGraterUUIDThanLatest(uuid, latest)) {
                latest = uuid;
            }
        }
        return latest;
    }

    private boolean isGraterUUIDThanLatest(UUID uuid, UUID latest) {
        Long timeFromUUID = getTimeFromUUID(uuid);
        Long timeFromLatestUUID = getTimeFromUUID(latest);
        int isGreater = timeFromUUID.compareTo(timeFromLatestUUID);
        if (isGreater == 0) {
            return uuid.compareTo(latest) > 0;
        }
        return isGreater > 0;
    }

    private List<PendingApprovalMapping> buildPendingApprovalMappings(Catchment catchment, String healthId, UUID uuid) {
        List<PendingApprovalMapping> mappings = new ArrayList<>();
        for (String catchmentId : catchment.getAllIds()) {
            PendingApprovalMapping mapping = new PendingApprovalMapping();
            mapping.setCatchmentId(catchmentId);
            mapping.setHealthId(healthId);
            mapping.setLastUpdated(uuid);
            mappings.add(mapping);
        }
        return mappings;
    }

    public PatientData findByHealthId(final String healthId) {
        logger.debug(String.format("Find patient by healthId: %s", healthId));
        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        if (patient == null) {
            throw new PatientNotFoundException("No patient found with health id: " + healthId);
        }
        return mapper.map(patient);
    }

    public List<PatientData> findByHealthId(List<String> healthIds) {
        String[] values = healthIds.toArray(new String[healthIds.size()]);
        List<Patient> patients = cassandraOps.select(buildFindByHidStmt(values), Patient.class);
        if (isEmpty(patients)) {
            throw new PatientNotFoundException("No patient found with health ids: " + healthIds);
        }
        return mapper.map(patients);
    }

    public List<PatientData> findAllByQuery(SearchQuery searchQuery) {
        return filterPatients(findProbables(searchQuery), searchQuery);
    }

    public List<PatientSummaryData> findAllSummaryByQuery(SearchQuery searchQuery) {
        return mapper.mapSummary(filterPatients(findProbables(searchQuery), searchQuery));
    }

    private List<PatientData> findProbables(SearchQuery searchQuery) {
        List<PatientData> dataList = new ArrayList<>();
        String query = null;

        if (isNotBlank(searchQuery.getNid())) {
            query = buildFindByNidStmt(searchQuery.getNid());

        } else if (isNotBlank(searchQuery.getBin_brn())) {
            query = buildFindByBrnStmt(searchQuery.getBin_brn());

        } else if (isNotBlank(searchQuery.getUid())) {
            query = buildFindByUidStmt(searchQuery.getUid());

        } else if (isNotBlank(searchQuery.getPhone_no())) {
            query = buildFindByPhoneNumberStmt(searchQuery.getPhone_no());

        } else if (isNotBlank(searchQuery.getHousehold_code())) {
            query = buildFindByHouseholdStmt(searchQuery.getHousehold_code());

        } else if (isNotBlank(searchQuery.getPresent_address()) && isNotBlank(searchQuery.getGiven_name())) {
            query = buildFindByNameStmt(searchQuery.getDivisionId(), searchQuery.getDistrictId(),
                    searchQuery.getUpazilaId(), searchQuery.getGiven_name(), searchQuery.getSur_name());
        }

        if (isNotBlank(query)) {
            List<String> healthIds = cassandraOps.queryForList(query, String.class);
            if (isNotEmpty(healthIds)) {
                dataList.addAll(this.findByHealthId(healthIds));
            }
        }
        return dataList;
    }

    private List<PatientData> filterPatients(List<PatientData> patients, SearchQuery searchQuery) {
        List<PatientData> result = new ArrayList<>();
        for (PatientData patient : patients) {
            if (isMatchingPatient(patient, searchQuery)) {
                result.add(patient);
            }
        }
        return result;
    }

    private boolean isMatchingPatient(PatientData p, SearchQuery searchQuery) {
        if (isNotBlank(searchQuery.getNid()) && !p.getNationalId().equals(searchQuery.getNid())) {
            return false;
        }
        if (isNotBlank(searchQuery.getBin_brn()) && !searchQuery.getBin_brn().equals(p.getBirthRegistrationNumber())) {
            return false;
        }
        if (isNotBlank(searchQuery.getUid()) && !searchQuery.getUid().equals(p.getUid())) {
            return false;
        }
        if (isNotBlank(searchQuery.getArea_code()) && !searchQuery.getArea_code().equals(p.getPhoneNumber().getAreaCode())) {
            return false;
        }
        if (isNotBlank(searchQuery.getPhone_no()) && !searchQuery.getPhone_no().equals(p.getPhoneNumber().getNumber())) {
            return false;
        }
        if (isNotBlank(searchQuery.getHousehold_code()) && !searchQuery.getHousehold_code().equals(p.getHouseholdCode())) {
            return false;
        }
        Address address = p.getAddress();
        if (isNotBlank(searchQuery.getDivisionId()) && !searchQuery.getDivisionId().equals(address.getDivisionId())) {
            return false;
        }
        if (isNotBlank(searchQuery.getDistrictId()) && !searchQuery.getDistrictId().equals(address.getDistrictId())) {
            return false;
        }
        if (isNotBlank(searchQuery.getUpazilaId()) && !searchQuery.getUpazilaId().equals(address.getUpazilaId())) {
            return false;
        }
        if (isNotBlank(searchQuery.getCityCorporationId()) && !searchQuery.getCityCorporationId().equals(address.getCityCorporationId())) {
            return false;
        }
        if (isNotBlank(searchQuery.getUnionOrUrbanWardId()) && !searchQuery.getUnionOrUrbanWardId().equals(address.getUnionOrUrbanWardId
                ())) {
            return false;
        }
        if (isNotBlank(searchQuery.getRuralWardId()) && !searchQuery.getRuralWardId().equals(address.getRuralWardId())) {
            return false;
        }

        if (isNotBlank(searchQuery.getGiven_name()) && !searchQuery.getGiven_name().equalsIgnoreCase(p.getGivenName())) {
            return false;
        }

        if (isNotBlank(searchQuery.getSur_name()) && !searchQuery.getSur_name().equalsIgnoreCase(p.getSurName())) {
            return false;
        }

        return true;
    }

    public List<Map<String, Object>> findAllByCatchment(Catchment catchment, Date since, UUID lastMarker, int limit) {
        List<CatchmentMapping> mappings = cassandraOps.select
                (buildFindByCatchmentStmt(catchment, since, lastMarker, limit), CatchmentMapping.class);
        if (isEmpty(mappings)) {
            return emptyList();
        }

        Set<String> healthIds = new HashSet<>();
        for (CatchmentMapping mapping : mappings) {
            healthIds.add(mapping.getHealthId());
        }

        ArrayList<String> healthIdList = new ArrayList<>();
        healthIdList.addAll(healthIds);
        List<PatientData> patients = findByHealthId(healthIdList);

        List<Map<String, Object>> catchmentEvents = new ArrayList<>();
        for (CatchmentMapping catchmentMapping : mappings) {

            HashMap<String, Object> catchmentPatientMapper = new HashMap<>();
            catchmentPatientMapper.put("eventId", catchmentMapping.getLastUpdated());
            catchmentPatientMapper.put("patientData", fetchPatient(catchmentMapping.getHealthId(), patients));
            catchmentEvents.add(catchmentPatientMapper);
        }

        return catchmentEvents;
    }

    private PatientData fetchPatient(final String healthId, final List<PatientData> patients) {
        return CollectionUtils.find(patients, new Predicate<PatientData>() {
            @Override
            public boolean evaluate(PatientData patient) {
                return patient.getHealthId().equals(healthId);
            }
        });
    }

    public List<PendingApprovalMapping> findPendingApprovalMapping(Catchment catchment, UUID after, UUID before, int limit) {
        List<PendingApprovalMapping> result = cassandraOps.select(buildFindPendingApprovalMappingStmt(catchment, after, before, limit),
                PendingApprovalMapping.class);
        if (isNotEmpty(result)) {
            Collections.sort(result, new Comparator<PendingApprovalMapping>() {
                @Override
                public int compare(PendingApprovalMapping m1, PendingApprovalMapping m2) {

                    UUID uuid1 = m1.getLastUpdated();
                    UUID uuid2 = m2.getLastUpdated();
                    Long t1 = getTimeFromUUID(uuid1);
                    Long t2 = getTimeFromUUID(uuid2);
                    int result = t1.compareTo(t2);
                    if (result == 0) {
                        return uuid1.compareTo(uuid2);
                    }
                    return result;
                }
            });
        }
        return result;
    }

    public String processPendingApprovals(PatientData requestData, PatientData existingPatientData, boolean shouldAccept) {
        Batch batch = batch();
        Patient newPatient;
        Requester approver = requestData.getRequester();
        TreeSet<PendingApproval> existingPendingApprovals = existingPatientData.getPendingApprovals();

        if (shouldAccept) {
            newPatient = mapper.map(requestData, existingPatientData);
            Map<String, Set<Requester>> requestedBy = findRequestedBy(existingPendingApprovals, requestData);
            buildCreateUpdateLogStmt(requestData, existingPatientData, requestedBy, approver, cassandraOps.getConverter(), batch);
            PatientAuditLogQueryBuilder.buildUpdateAuditLogStmt(requestData, existingPatientData, requestedBy, approver, cassandraOps.getConverter(), batch);

        } else {
            newPatient = new Patient();
            newPatient.setHealthId(requestData.getHealthId());
        }

        newPatient.setUpdatedAt(TimeUuidUtil.uuidForDate(new Date()));
        newPatient.setUpdatedBy(approver);
        newPatient.setPendingApprovals(existingPendingApprovals);

        String healthId = requestData.getHealthId();
        UUID lastUpdated = findLatestUuid(newPatient.getPendingApprovals());

        TreeSet<PendingApproval> pendingApprovals = updatePendingApprovals(newPatient.getPendingApprovals(), requestData, shouldAccept);
        newPatient.setPendingApprovals(pendingApprovals);
        buildUpdateBatch(newPatient, existingPatientData, cassandraOps.getConverter(), batch);

        if (isNotEmpty(pendingApprovals)) {
            UUID toBeUpdated = findLatestUuid(pendingApprovals);
            boolean hasLastUpdatedChanged = !toBeUpdated.equals(lastUpdated);
            boolean hasCatchmentChanged = newPatient.getCatchment() != null && !newPatient.getCatchment().equals(existingPatientData
                    .getCatchment());
            if (hasLastUpdatedChanged || hasCatchmentChanged) {
                long timestamp = new Date().getTime();
                buildDeletePendingApprovalMappingStmt(healthId, batch, timestamp);
                Catchment catchment = newPatient.getCatchment() != null ? newPatient.getCatchment() : existingPatientData.getCatchment();
                buildCreatePendingApprovalMappingStmt(catchment, healthId, toBeUpdated, batch, timestamp + QUERY_EXEC_DELAY);
            }
        } else {
            buildDeletePendingApprovalMappingStmt(healthId, batch, new Date().getTime());
        }
        cassandraOps.execute(batch);
        return healthId;
    }

    public TreeSet<PendingApproval> updatePendingApprovals(TreeSet<PendingApproval> pendingApprovals, PatientData patient, boolean
            shouldAccept) {
        for (Iterator<PendingApproval> it = pendingApprovals.iterator(); it.hasNext(); ) {
            PendingApproval pendingApproval = it.next();
            String key = pendingApproval.getName();
            Object value = patient.getValue(key);

            if (value != null) {
                if (shouldAccept) {
                    it.remove();
                } else {
                    removeMatchingPendingApproval(pendingApproval, value);
                    TreeMap<UUID, PendingApprovalFieldDetails> fieldDetails = pendingApproval.getFieldDetails();
                    if (fieldDetails == null || fieldDetails.size() == 0) {
                        it.remove();
                    }
                }
            }
        }
        return pendingApprovals;
    }

    private void removeMatchingPendingApproval(PendingApproval pendingApproval, Object value) {
        for (Iterator<PendingApprovalFieldDetails> it = pendingApproval.getFieldDetails().values().iterator(); it.hasNext(); ) {
            PendingApprovalFieldDetails fieldDetails = it.next();
            if (value.equals(fieldDetails.getValue())) {
                it.remove();
            }
        }
    }

    public Map<String, Set<Requester>> findRequestedBy(TreeSet<PendingApproval> pendingApprovals, PatientData requestData) {
        Map<String, Set<Requester>> requestedBy = new HashMap<>();
        for (PendingApproval pendingApproval : pendingApprovals) {
            String fieldName = pendingApproval.getName();
            Object value = requestData.getValue(fieldName);

            if (value != null) {
                for (PendingApprovalFieldDetails fieldDetails : pendingApproval.getFieldDetails().values()) {
                    if (value.equals(fieldDetails.getValue())) {
                        buildRequestedBy(requestedBy, fieldName, fieldDetails.getRequestedBy());
                    }
                }
            }
        }
        return requestedBy;
    }

    private void buildRequestedBy(Map<String, Set<Requester>> map, String key, Requester value) {
        if (value == null) {
            return;
        }
        Set<Requester> valueList = map.get(key);
        if (isEmpty(valueList)) {
            valueList = new HashSet<>();
            map.put(key, valueList);
        }
        valueList.add(value);
    }

    public boolean patientExists(String healthId) {
        Patient patient = cassandraOps.selectOneById(Patient.class, healthId);
        if (patient != null) return true;
        return false;
    }
}
