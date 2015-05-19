package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.datastax.driver.core.utils.UUIDs;
import org.sharedhealth.mci.web.mapper.Address;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.mapper.PatientMapper;
import org.sharedhealth.mci.web.mapper.PatientSummaryData;
import org.sharedhealth.mci.web.mapper.PhoneNumber;
import org.sharedhealth.mci.web.mapper.Requester;
import org.sharedhealth.mci.web.model.*;
import org.springframework.data.cassandra.convert.CassandraConverter;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.datastax.driver.core.querybuilder.Select.Where;
import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static org.apache.commons.lang3.StringUtils.*;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.*;
import static org.sharedhealth.mci.web.utils.JsonConstants.HOUSEHOLD_CODE;
import static org.sharedhealth.mci.web.utils.JsonMapper.writeValueAsString;
import static org.springframework.data.cassandra.core.CassandraTemplate.*;

public class PatientQueryBuilder {

    static Batch buildSaveBatch(Patient patient, CassandraConverter converter) {
        String healthId = patient.getHealthId();
        Batch batch = QueryBuilder.batch();

        batch.add(createInsertQuery(CF_PATIENT, patient, null, converter));

        buildCreateMappingStmt(healthId, patient.getNationalId(), CF_NID_MAPPING, converter, batch);
        buildCreateMappingStmt(healthId, patient.getBirthRegistrationNumber(), CF_BRN_MAPPING, converter, batch);
        buildCreateMappingStmt(healthId, patient.getUid(), CF_UID_MAPPING, converter, batch);
        buildCreateMappingStmt(healthId, patient.getCellNo(), CF_PHONE_NUMBER_MAPPING, converter, batch);
        buildCreateMappingStmt(healthId, patient.getHouseholdCode(), CF_HOUSEHOLD_CODE_MAPPING, converter, batch);

        buildCreateNameMappingStmt(patient, converter, batch);
        buildCreateCatchmentMappingsStmt(patient.getCatchment(), patient.getUpdatedAt(), patient.getHealthId(), converter, batch);
        return batch;
    }

    static void addToPatientUpdateLogStmt(Patient patient,
                                         Map<String, Set<Requester>> requestedBy,
                                         CassandraConverter converter, Batch batch) {
        PatientUpdateLog patientUpdateLog = new PatientUpdateLog();
        PatientSummaryData patientSummaryData = new PatientMapper().mapSummary(patient);
        String changeSet = writeValueAsString(patientSummaryData);

        if (changeSet != null) {
            patientUpdateLog.setEventId(timeBased());
            patientUpdateLog.setHealthId(patient.getHealthId());
            patientUpdateLog.setChangeSet(changeSet);
            patientUpdateLog.setRequestedBy(writeValueAsString(requestedBy));
            patientUpdateLog.setApprovedBy(NOT_REQUIRED);
            patientUpdateLog.setEventType(EVENT_TYPE_CREATED);
            batch.add(createInsertQuery(CF_PATIENT_UPDATE_LOG, patientUpdateLog, null, converter));
        }
    }

    static Batch buildUpdateBatch(Patient newPatient, PatientData existingPatientData, CassandraConverter converter, Batch batch) {
        String healthId = newPatient.getHealthId();

        buildUpdateMappingStmt(healthId, newPatient.getNationalId(), existingPatientData.getNationalId(), CF_NID_MAPPING, converter, batch);
        buildUpdateMappingStmt(healthId, newPatient.getBirthRegistrationNumber(), existingPatientData.getBirthRegistrationNumber(), CF_BRN_MAPPING, converter, batch);
        buildUpdateMappingStmt(healthId, newPatient.getUid(), existingPatientData.getUid(), CF_UID_MAPPING, converter, batch);
        buildUpdateMappingStmt(healthId, newPatient.getHouseholdCode(), existingPatientData.getHouseholdCode(), CF_HOUSEHOLD_CODE_MAPPING, converter, batch);

        PhoneNumber existingPhone = existingPatientData.getPhoneNumber();
        String existingPhoneNumber = existingPhone == null ? null : existingPhone.getNumber();
        buildUpdateMappingStmt(healthId, newPatient.getCellNo(), existingPhoneNumber, CF_PHONE_NUMBER_MAPPING, converter, batch);

        buildUpdateNameMappingStmt(newPatient, existingPatientData, converter, batch);
        buildUpdateCatchmentMappingsStmt(newPatient, existingPatientData, converter, batch);

        batch.add(buildUpdateStmt(newPatient, converter));
        return batch;
    }

    private static void buildCreateMappingStmt(String healthId, String id, String columnFamily, CassandraConverter converter, Batch batch) {
        if (isNotBlank(id)) {
            Object objectToSave = buildObjectToSave(id, healthId, columnFamily);
            batch.add(createInsertQuery(columnFamily, objectToSave, null, converter));
        }
    }

    private static void buildDeleteMappingsStmt(String healthId, String id, String columnFamily, CassandraConverter converter, Batch batch) {
        if (isNotBlank(id)) {
            Object objectToSave = buildObjectToSave(id, healthId, columnFamily);
            batch.add(createDeleteQuery(columnFamily, objectToSave, null, converter));
        }
    }

    static void buildUpdateMappingStmt(String healthId, String newId, String existingId, String columnFamily, CassandraConverter converter, Batch batch) {
        if (defaultString(newId).equals(defaultString(existingId)) || newId == null) {
            return;
        }
        buildDeleteMappingsStmt(healthId, existingId, columnFamily, converter, batch);
        buildCreateMappingStmt(healthId, newId, columnFamily, converter, batch);
    }

    private static Object buildObjectToSave(String id, String healthId, String columnFamily) {
        Object objectToSave = null;
        switch (columnFamily) {
            case CF_NID_MAPPING:
                objectToSave = new NidMapping(id, healthId);
                break;
            case CF_BRN_MAPPING:
                objectToSave = new BrnMapping(id, healthId);
                break;
            case CF_UID_MAPPING:
                objectToSave = new UidMapping(id, healthId);
                break;
            case CF_PHONE_NUMBER_MAPPING:
                objectToSave = new PhoneNumberMapping(id, healthId);
                break;
            case CF_HOUSEHOLD_CODE_MAPPING:
                objectToSave = new HouseholdCodeMapping(id, healthId);
                break;
        }
        return objectToSave;
    }

    private static void buildCreateNameMappingStmt(Patient patient, CassandraConverter converter, Batch batch) {
        String healthId = patient.getHealthId();
        String divisionId = patient.getDivisionId();
        String districtId = patient.getDistrictId();
        String upazilaId = patient.getUpazilaId();
        String givenName = patient.getGivenName();
        String surname = patient.getSurName();

        if (isNotBlank(healthId) && isNotBlank(divisionId) && isNotBlank(districtId) && isNotBlank(upazilaId)
                && isNotBlank(givenName) && isNotBlank(surname)) {
            NameMapping mapping = new NameMapping(divisionId, districtId, upazilaId, givenName.toLowerCase(),
                    surname.toLowerCase(), patient.getHealthId());
            batch.add(createInsertQuery(CF_NAME_MAPPING, mapping, null, converter));
        }

    }

    private static void buildDeleteNameMappingStmt(PatientData patient, CassandraConverter converter, Batch batch) {
        String healthId = patient.getHealthId();
        Address address = patient.getAddress();
        String divisionId = address.getDivisionId();
        String districtId = address.getDistrictId();
        String upazilaId = address.getUpazilaId();
        String givenName = patient.getGivenName();
        String surname = patient.getSurName();

        if (isNotBlank(healthId) && isNotBlank(divisionId) && isNotBlank(districtId) && isNotBlank(upazilaId)
                && isNotBlank(givenName) && isNotBlank(surname)) {
            NameMapping mapping = new NameMapping(divisionId, districtId, upazilaId, givenName.toLowerCase(),
                    surname.toLowerCase(), patient.getHealthId());
            batch.add(createDeleteQuery(CF_NAME_MAPPING, mapping, null, converter));
        }
    }

    static void buildUpdateNameMappingStmt(Patient newPatient, PatientData existingPatient, CassandraConverter converter, Batch batch) {
        Address existingAddress = existingPatient.getAddress();
        String existingGivenName = existingPatient.getGivenName();
        String existingSurname = existingPatient.getSurName();
        String existingDivisionId = existingAddress.getDivisionId();
        String existingDistrictId = existingAddress.getDistrictId();
        String existingUpazilaId = existingAddress.getUpazilaId();

        String newGivenName = newPatient.getGivenName() == null ? existingGivenName : newPatient.getGivenName();
        String newSurname = newPatient.getSurName() == null ? existingSurname : newPatient.getSurName();
        String newDivisionId = newPatient.getDivisionId() == null ? existingDivisionId : newPatient.getDivisionId();
        String newDistrictId = newPatient.getDistrictId() == null ? existingDistrictId : newPatient.getDistrictId();
        String newUpazilaId = newPatient.getUpazilaId() == null ? existingUpazilaId : newPatient.getUpazilaId();

        if (defaultString(newGivenName).equals(defaultString(existingGivenName))
                && defaultString(newSurname).equals(defaultString(existingSurname))
                && defaultString(newDivisionId).equals(defaultString(existingDivisionId))
                && defaultString(newDistrictId).equals(defaultString(existingDistrictId))
                && defaultString(newUpazilaId).equals(defaultString(existingUpazilaId))) {
            return;
        }

        buildDeleteNameMappingStmt(existingPatient, converter, batch);

        Patient patient = new Patient();
        patient.setHealthId(newPatient.getHealthId());
        patient.setGivenName(newGivenName);
        patient.setSurName(newSurname);
        patient.setDivisionId(newDivisionId);
        patient.setDistrictId(newDistrictId);
        patient.setUpazilaId(newUpazilaId);
        buildCreateNameMappingStmt(patient, converter, batch);
    }

    private static void buildCreateCatchmentMappingsStmt(Catchment catchment, UUID lastUpdated, String healthId,
                                                         CassandraConverter converter, Batch batch) {
        for (String catchmentId : catchment.getAllIds()) {
            CatchmentMapping mapping = new CatchmentMapping(catchmentId, lastUpdated, healthId);
            batch.add(createInsertQuery(CF_CATCHMENT_MAPPING, mapping, null, converter));
        }
    }

    private static void buildDeleteCatchmentMappingsStmt(Catchment catchment, UUID lastUpdated, String healthId,
                                                         CassandraConverter converter, Batch batch) {
        for (String catchmentId : catchment.getAllIds()) {
            CatchmentMapping mapping = new CatchmentMapping(catchmentId, lastUpdated, healthId);
            batch.add(createDeleteQuery(CF_CATCHMENT_MAPPING, mapping, null, converter));
        }
    }

    static void buildUpdateCatchmentMappingsStmt(Patient newPatient, PatientData existingPatient, CassandraConverter converter,
                                                 Batch batch) {
        buildDeleteCatchmentMappingsStmt(existingPatient.getCatchment(), existingPatient.getUpdatedAt(),
                existingPatient.getHealthId(), converter, batch);

        Catchment catchment = newPatient.getCatchment() != null ? newPatient.getCatchment() : existingPatient.getCatchment();

        buildCreateCatchmentMappingsStmt(catchment, newPatient.getUpdatedAt(), newPatient.getHealthId(),
                converter, batch);
    }

    public static Update buildUpdateStmt(Patient patient, CassandraConverter converter) {
        return toUpdateQuery(CF_PATIENT, patient, null, converter);
    }

    public static String buildFindByCatchmentStmt(Catchment catchment, Date since, UUID lastMarker, int limit) {
        Where where = select(HEALTH_ID, LAST_UPDATED).from(CF_CATCHMENT_MAPPING)
                .where(eq(CATCHMENT_ID, catchment.getId()));

        if (lastMarker != null) {
            where = where.and(gt(LAST_UPDATED, lastMarker));
        } else if (since != null) {
            where = where.and(gte(LAST_UPDATED, UUIDs.startOf(since.getTime())));
        }
        return where.limit(limit).toString();
    }

    public static String buildFindByHidStmt(String[] values) {
        return select().from(CF_PATIENT).where(in(HEALTH_ID, (Object[]) values)).toString();
    }

    public static String buildFindByNidStmt(String nid) {
        return select(HEALTH_ID).from(CF_NID_MAPPING).where(eq(NATIONAL_ID, nid)).toString();
    }

    public static String buildFindByBrnStmt(String brn) {
        return select(HEALTH_ID).from(CF_BRN_MAPPING).where(eq(BIN_BRN, brn)).toString();
    }

    public static String buildFindByUidStmt(String uid) {
        return select(HEALTH_ID).from(CF_UID_MAPPING).where(eq(UID, uid)).toString();
    }

    public static String buildFindByHouseholdStmt(String householdCode) {
        return select(HEALTH_ID).from(CF_HOUSEHOLD_CODE_MAPPING).where(eq(HOUSEHOLD_CODE, householdCode)).toString();
    }

    public static String buildFindByPhoneNumberStmt(String phoneNumber) {
        return select(HEALTH_ID).from(CF_PHONE_NUMBER_MAPPING).where(eq(PHONE_NO, phoneNumber)).toString();
    }

    public static String buildFindPendingApprovalMappingStmt(Catchment catchment, UUID after, UUID before, int limit) {
        Where where = select(HEALTH_ID, LAST_UPDATED).from(CF_PENDING_APPROVAL_MAPPING)
                .where(eq(CATCHMENT_ID, catchment.getId()));

        if (after != null) {
            where.and(gt(LAST_UPDATED, after));
            where.orderBy(asc(LAST_UPDATED));
        }
        if (before != null) {
            where.and(lt(LAST_UPDATED, before));
            if (after == null) {
                where.orderBy(desc(LAST_UPDATED));
            }
        }
        return where.limit(limit).toString();
    }

    public static String buildFindByNameStmt(String divisionId, String districtId, String upazilaId, String givenName, String surname) {
        Where where = select(HEALTH_ID).from(CF_NAME_MAPPING)
                .where(eq(DIVISION_ID, divisionId))
                .and(eq(DISTRICT_ID, districtId))
                .and(eq(UPAZILA_ID, upazilaId))
                .and(eq(GIVEN_NAME, givenName.toLowerCase()));

        if (isNotEmpty(surname)) {
            where = where.and(eq(SUR_NAME, surname.toLowerCase()));
        }
        return where.toString();
    }
}
