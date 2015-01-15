package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sharedhealth.mci.utils.UidGenerator;
import org.sharedhealth.mci.web.exception.HealthIDExistException;
import org.sharedhealth.mci.web.exception.PatientNotFoundException;
import org.sharedhealth.mci.web.handler.MCIResponse;
import org.sharedhealth.mci.web.handler.PendingApprovalFilter;
import org.sharedhealth.mci.web.mapper.*;
import org.sharedhealth.mci.web.model.CatchmentMapping;
import org.sharedhealth.mci.web.model.Patient;
import org.sharedhealth.mci.web.model.PatientUpdateLog;
import org.sharedhealth.mci.web.model.PendingApprovalMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.datastax.driver.core.utils.UUIDs.unixTimestamp;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientQueryBuilder.*;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.*;
import static org.sharedhealth.mci.web.utils.PatientDataConstants.PATIENT_STATUS_ALIVE;
import static org.springframework.data.cassandra.core.CassandraTemplate.createDeleteQuery;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;

@Component
public class PatientRepository extends BaseRepository {

    protected static final Logger logger = LoggerFactory.getLogger(PatientRepository.class);
    private UidGenerator uidGenerator;
    private PatientMapper mapper;

    @Autowired
    public PatientRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOperations,
                             PatientMapper mapper,
                             UidGenerator uidGenerator) {
        super(cassandraOperations);
        this.mapper = mapper;
        this.uidGenerator = uidGenerator;
    }

    public MCIResponse create(PatientData patientData) {
        if (!isBlank(patientData.getHealthId())) {
            DirectFieldBindingResult bindingResult = new DirectFieldBindingResult(patientData, "patient");
            bindingResult.addError(new FieldError("patient", "hid", "3001"));
            throw new HealthIDExistException(bindingResult);
        }
        Patient patient = mapper.map(patientData, new PatientData());
        patient.setHealthId(uidGenerator.getId());
        patient.setCreatedAt(new Date());
        patient.setUpdatedAt(new Date());

        if (isBlank(patient.getStatus())) {
            patient.setStatus(PATIENT_STATUS_ALIVE);
        }

        if (patient.getConfidential() == null) {
            patient.setConfidential(false);
        }

        cassandraOps.execute(buildSaveBatch(patient, cassandraOps.getConverter()));
        return new MCIResponse(patient.getHealthId(), HttpStatus.CREATED);
    }

    public MCIResponse update(PatientData updateRequest, String healthId) {
        updateRequest.setHealthId(healthId);
        PatientData existingPatientData = this.findByHealthId(healthId);

        PendingApprovalFilter pendingApprovalFilter = new PendingApprovalFilter(getApprovalProperties());
        PatientData newPatientData = pendingApprovalFilter.filter(existingPatientData, updateRequest);

        Patient newPatient = mapper.map(newPatientData, existingPatientData);
        newPatient.setHealthId(healthId);
        newPatient.setUpdatedAt(new Date());

        final Batch batch = buildUpdateBatch(newPatient, existingPatientData);
        buildCreateUpdateLogStmt(newPatientData, existingPatientData, batch);

        cassandraOps.execute(batch);
        return new MCIResponse(newPatient.getHealthId(), HttpStatus.ACCEPTED);
    }

    private Properties getApprovalProperties() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("approvalFeilds.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read approval property file.", e);
        }
        return properties;
    }

    private Batch buildUpdateBatch(Patient newPatient, PatientData existingPatientData) {
        Batch batch = batch();
        TreeSet<PendingApproval> newPendingApprovals = newPatient.getPendingApprovals();

        if (isNotEmpty(newPendingApprovals)) {
            TreeSet<PendingApproval> existingPendingApprovals = existingPatientData.getPendingApprovals();
            String healthId = newPatient.getHealthId();

            if (existingPendingApprovals != null && existingPendingApprovals.size() > 0) {
                buildDeletePendingApprovalMappingStmt(healthId, batch);
            }

            UUID uuid = findLatestUuid(newPatient.getPendingApprovals());
            buildCreatePendingApprovalMappingStmt(newPatient.getCatchment(), healthId, uuid, batch);
        }

        buildUpdateCatchmentMappingsStmt(newPatient, existingPatientData, batch);
        batch.add(buildUpdateStmt(newPatient, cassandraOps.getConverter()));
        return batch;
    }

    private void buildUpdateCatchmentMappingsStmt(Patient patientToSave, PatientData existingPatient, Batch batch) {
        buildDeleteCatchmentMappingsStmt(existingPatient.getCatchment(), existingPatient.getUpdatedAt(), existingPatient.getHealthId(),
                cassandraOps.getConverter(), batch);

        Catchment catchment = patientToSave.getCatchment() != null ? patientToSave.getCatchment() : existingPatient.getCatchment();

        buildCreateCatchmentMappingsStmt(catchment, patientToSave.getUpdatedAt(), patientToSave.getHealthId(),
                cassandraOps.getConverter(), batch);
    }

    private void buildDeletePendingApprovalMappingStmt(String healthId, Batch batch) {
        String cql = select().from(CF_PENDING_APPROVAL_MAPPING).where(eq(HEALTH_ID, healthId)).toString();
        List<PendingApprovalMapping> mappings = cassandraOps.select(cql, PendingApprovalMapping.class);
        for (PendingApprovalMapping mapping : mappings) {
            batch.add(createDeleteQuery(CF_PENDING_APPROVAL_MAPPING, mapping, null, cassandraOps.getConverter()));
        }
    }

    private void buildCreatePendingApprovalMappingStmt(Catchment catchment, String healthId, UUID uuid, Batch batch) {
        List<PendingApprovalMapping> mappings = buildPendingApprovalMappings(catchment, healthId, uuid);
        for (PendingApprovalMapping mapping : mappings) {
            batch.add(createInsertQuery(CF_PENDING_APPROVAL_MAPPING, mapping, null, cassandraOps.getConverter()));
        }
    }

    private void buildCreateUpdateLogStmt(PatientData patientDataToSave, PatientData existingPatientData, Batch batch) {
        PatientUpdateLog patientUpdateLog = new PatientUpdateLog();
        String changeSet = getChangeSet(patientDataToSave, existingPatientData);

        if (changeSet != null) {
            patientUpdateLog.setHealthId(existingPatientData.getHealthId());
            patientUpdateLog.setEventTime(new Date());
            patientUpdateLog.setChangeSet(changeSet);
            batch.add(createInsertQuery(CF_PATIENT_UPDATE_LOG, patientUpdateLog, null, cassandraOps.getConverter()));
        }
    }

    private String getChangeSet(PatientData newData, PatientData oldData) {
        PatientData patient = new PatientData();

        patient.setSurName(getChangedValue(newData.getSurName(), oldData.getSurName()));
        patient.setGivenName(getChangedValue(newData.getGivenName(), oldData.getGivenName()));
        patient.setConfidential(getChangedValueIgnoreCase(newData.getConfidential(), oldData.getConfidential()));
        patient.setAddress(getChangedValue(newData.getAddress(), oldData.getAddress()));

        if (patient.getSurName() != null || patient.getGivenName() != null) {
            ObjectMapper oMapper = new ObjectMapper();
            try {
                return oMapper.writeValueAsString(patient);
            } catch (JsonProcessingException e) {
                return null;
            }
        }

        return null;
    }

    private Address getChangedValue(Address newValue, Address old) {
        return newValue != null && !newValue.equals(old) ? newValue : null;
    }

    private String getChangedValue(String newValue, String old) {
        return newValue != null && !newValue.equals(old) ? newValue : null;
    }

    private String getChangedValueIgnoreCase(String newValue, String old) {
        return newValue != null && !newValue.equalsIgnoreCase(old) ? newValue : null;
    }


    UUID findLatestUuid(TreeSet<PendingApproval> pendingApprovals) {
        UUID latest = null;
        for (PendingApproval pendingApproval : pendingApprovals) {
            UUID uuid = pendingApproval.getFieldDetails().keySet().iterator().next();
            if (latest == null) {
                latest = uuid;
                continue;
            }
            if (unixTimestamp(uuid) > unixTimestamp(latest)) {
                latest = uuid;
            }
        }
        return latest;
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
        if (isNotBlank(searchQuery.getGiven_name()) && !searchQuery.getGiven_name().equalsIgnoreCase(p.getGivenName())) {
            return false;
        }
        if (isNotBlank(searchQuery.getSur_name()) && !searchQuery.getSur_name().equalsIgnoreCase(p.getSurName())) {
            return false;
        }
        return true;
    }

    public List<PatientData> findAllByCatchment(Catchment catchment, Date after, int limit) {
        List<CatchmentMapping> mappings = cassandraOps.select(buildFindByCatchmentStmt(catchment, after, limit), CatchmentMapping.class);
        if (isEmpty(mappings)) {
            return emptyList();
        }
        List<String> healthIds = new ArrayList<>();
        for (CatchmentMapping mapping : mappings) {
            healthIds.add(mapping.getHealthId());
        }
        return findByHealthId(healthIds);
    }

    public List<PendingApprovalMapping> findPendingApprovalMapping(Catchment catchment, UUID after, UUID before, int limit) {
        return cassandraOps.select(buildFindPendingApprovalMappingStmt(catchment, after, before, limit), PendingApprovalMapping.class);
    }

    public String processPendingApprovals(PatientData requestData, PatientData existingPatientData, boolean shouldAccept) {
        Batch batch = batch();
        Patient patient;
        if (shouldAccept) {
            patient = mapper.map(requestData, existingPatientData);
        } else {
            patient = new Patient();
            patient.setHealthId(requestData.getHealthId());
        }
        patient.setPendingApprovals(existingPatientData.getPendingApprovals());

        String healthId = requestData.getHealthId();
        UUID lastUpdated = findLatestUuid(patient.getPendingApprovals());

        TreeSet<PendingApproval> pendingApprovals = updatePendingApprovals(patient.getPendingApprovals(), requestData, shouldAccept);
        patient.setPendingApprovals(pendingApprovals);
        batch.add(buildUpdateStmt(patient, cassandraOps.getConverter()));

        if (isNotEmpty(pendingApprovals)) {
            UUID toBeUpdated = findLatestUuid(pendingApprovals);
            if (!toBeUpdated.equals(lastUpdated)) {
                buildDeletePendingApprovalMappingStmt(healthId, batch);
                Catchment catchment = patient.getCatchment() != null ? patient.getCatchment() : existingPatientData.getCatchment();
                buildCreatePendingApprovalMappingStmt(catchment, healthId, toBeUpdated, batch);
            }
        } else {
            buildDeletePendingApprovalMappingStmt(healthId, batch);
        }

        cassandraOps.execute(batch);
        return healthId;
    }

    TreeSet<PendingApproval> updatePendingApprovals(TreeSet<PendingApproval> pendingApprovals, PatientData patient, boolean shouldAccept) {
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

    public List<PatientUpdateLog> findPatientsUpdatedSince(Date after, int limit) {
        return cassandraOps.select(buildFindUpdateLogStmt(after, limit),
                PatientUpdateLog.class);
    }
}
