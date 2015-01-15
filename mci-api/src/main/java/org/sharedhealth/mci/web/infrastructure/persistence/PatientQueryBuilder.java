package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import org.sharedhealth.mci.utils.DateUtil;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.mapper.PatientData;
import org.sharedhealth.mci.web.model.*;
import org.springframework.data.cassandra.convert.CassandraConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.datastax.driver.core.querybuilder.Select.Where;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.*;
import static org.springframework.data.cassandra.core.CassandraTemplate.createDeleteQuery;
import static org.springframework.data.cassandra.core.CassandraTemplate.createInsertQuery;
import static org.springframework.data.cassandra.core.CassandraTemplate.toUpdateQuery;

public class PatientQueryBuilder {

    public static String buildFindByHidStmt(String[] values) {
        return select().from(CF_PATIENT).where(in(HEALTH_ID, values)).toString();
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

    public static String buildFindByPhoneNumberStmt(String phoneNumber) {
        return select(HEALTH_ID).from(CF_PHONE_NUMBER_MAPPING).where(eq(PHONE_NO, phoneNumber)).toString();
    }

    public static String buildFindPendingApprovalMappingStmt(Catchment catchment, UUID after, UUID before, int limit) {
        Where where = select(HEALTH_ID, LAST_UPDATED).from(CF_PENDING_APPROVAL_MAPPING)
                .where(eq(CATCHMENT_ID, catchment.getId()));

        if (after != null) {
            where = where.and(gt(LAST_UPDATED, after));
        }
        if (before != null) {
            where = where.and(lt(LAST_UPDATED, before));
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

    public static Batch buildSaveBatch(Patient patient, CassandraConverter converter) {
        String healthId = patient.getHealthId();
        Batch batch = QueryBuilder.batch();

        batch.add(createInsertQuery(CF_PATIENT, patient, null, converter));

        String nationalId = patient.getNationalId();
        if (isNotBlank(nationalId)) {
            batch.add(createInsertQuery(CF_NID_MAPPING, new NidMapping(nationalId, healthId), null, converter));
        }

        String brn = patient.getBirthRegistrationNumber();
        if (isNotBlank(brn)) {
            batch.add(createInsertQuery(CF_BRN_MAPPING, new BrnMapping(brn, healthId), null, converter));
        }

        String uid = patient.getUid();
        if (isNotBlank(uid)) {
            batch.add(createInsertQuery(CF_UID_MAPPING, new UidMapping(uid, healthId), null, converter));
        }

        String phoneNumber = patient.getCellNo();
        if (isNotBlank(phoneNumber)) {
            batch.add(createInsertQuery(CF_PHONE_NUMBER_MAPPING,
                    new PhoneNumberMapping(phoneNumber, healthId), null, converter));
        }

        String divisionId = patient.getDivisionId();
        String districtId = patient.getDistrictId();
        String upazilaId = patient.getUpazilaId();
        String givenName = patient.getGivenName();
        String surname = patient.getSurName();

        if (isNotBlank(divisionId) && isNotBlank(districtId) && isNotBlank(upazilaId) && isNotBlank(givenName) && isNotBlank(surname)) {
            NameMapping mapping = new NameMapping(divisionId, districtId, upazilaId, givenName.toLowerCase(), surname.toLowerCase(), healthId);
            batch.add(createInsertQuery(CF_NAME_MAPPING, mapping, null, converter));
        }

        buildCreateCatchmentMappingsStmt(patient.getCatchment(), patient.getUpdatedAt(), patient.getHealthId(), converter, batch);
        return batch;
    }

    static void buildCreateCatchmentMappingsStmt(Catchment catchment, Date lastUpdated, String healthId, CassandraConverter converter, Batch batch) {
        for (String catchmentId : catchment.getAllIds()) {
            CatchmentMapping mapping = new CatchmentMapping(catchmentId, lastUpdated, healthId);
            batch.add(createInsertQuery(CF_CATCHMENT_MAPPING, mapping, null, converter));
        }
    }

    static void buildDeleteCatchmentMappingsStmt(Catchment catchment, Date lastUpdated, String healthId, CassandraConverter converter, Batch batch) {
        for (String catchmentId : catchment.getAllIds()) {
            CatchmentMapping mapping = new CatchmentMapping(catchmentId, lastUpdated, healthId);
            batch.add(createDeleteQuery(CF_CATCHMENT_MAPPING, mapping, null, converter));
        }
    }

    public static Update buildUpdateStmt(Patient patient, CassandraConverter converter) {
        return toUpdateQuery(CF_PATIENT, patient, null, converter);
    }

    public static String buildFindByCatchmentStmt(Catchment catchment, Date after, int limit) {
        Where where = select(HEALTH_ID, LAST_UPDATED).from(CF_CATCHMENT_MAPPING)
                .where(eq(CATCHMENT_ID, catchment.getId()));

        if (after != null) {
            where = where.and(gt(LAST_UPDATED, after));
        }
        return where.limit(limit).toString();
    }

    public static String buildFindCatchmentMappingsStmt(PatientData patient) {
        List<String> catchmentIds = patient.getCatchment().getAllIds();
        return select().from(CF_CATCHMENT_MAPPING)
                .where(in(CATCHMENT_ID, catchmentIds.toArray(new String[catchmentIds.size()])))
                .and(eq(LAST_UPDATED, patient.getUpdatedAt()))
                .and(eq(HEALTH_ID, patient.getHealthId())).toString();
    }

    public static String buildFindUpdateLogStmt(Date after, int limit) {
        Where where = select().from(CF_PATIENT_UPDATE_LOG)
                .where(in(YEAR, getYearsSince(after).toArray()));

        if (after != null) {
            where = where.and(gt(EVENT_TIME, after));
        }

        return where.limit(limit).toString();
    }

    private static List<Integer> getYearsSince(Date after)
    {
        List<Integer> years = new ArrayList<>();
        int end = DateUtil.getYear(new Date());

        for (int i = DateUtil.getYear(after); i <= end; i++) {
            years.add(i);
        }

        return years;
    }
}
