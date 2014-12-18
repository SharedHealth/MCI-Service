package org.sharedhealth.mci.web.infrastructure.persistence;

import com.datastax.driver.core.querybuilder.*;
import org.sharedhealth.mci.web.mapper.Catchment;
import org.sharedhealth.mci.web.model.*;
import org.springframework.data.cassandra.convert.CassandraConverter;

import java.util.UUID;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.datastax.driver.core.querybuilder.Select.Where;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.springframework.data.cassandra.core.CassandraTemplate.*;

public class PatientQueryBuilder {

    public static final String CF_PATIENT = "patient";
    public static final String CF_NID_MAPPING = "nid_mapping";
    public static final String CF_BRN_MAPPING = "brn_mapping";
    public static final String CF_UID_MAPPING = "uid_mapping";
    public static final String CF_PHONE_NUMBER_MAPPING = "phone_number_mapping";
    public static final String CF_NAME_MAPPING = "name_mapping";
    public static final String CF_PENDING_APPROVAL_MAPPING = "approval_mapping";

    public static final String HEALTH_ID = "health_id";
    public static final String CREATED_AT = "created_at";
    public static final String NATIONAL_ID = "national_id";
    public static final String FULL_NAME_BANGLA = "full_name_bangla";
    public static final String GIVEN_NAME = "given_name";
    public static final String SUR_NAME = "sur_name";
    public static final String DATE_OF_BIRTH = "date_of_birth";
    public static final String GENDER = "gender";
    public static final String OCCUPATION = "occupation";
    public static final String EDU_LEVEL = "edu_level";
    public static final String ADDRESS_LINE = "address_line";
    public static final String DIVISION_ID = "division_id";
    public static final String DISTRICT_ID = "district_id";
    public static final String UPAZILA_ID = "upazila_id";
    public static final String UNION_OR_URBAN_WARD_ID = "union_or_urban_ward_id";
    public static final String BIN_BRN = "bin_brn";
    public static final String UID = "uid";
    public static final String FATHERS_NAME_BANGLA = "fathers_name_bangla";
    public static final String FATHERS_GIVEN_NAME = "fathers_given_name";
    public static final String FATHERS_SUR_NAME = "fathers_sur_name";
    public static final String FATHERS_UID = "fathers_uid";
    public static final String FATHERS_NID = "fathers_nid";
    public static final String FATHERS_BRN = "fathers_brn";
    public static final String MOTHERS_NAME_BANGLA = "mothers_name_bangla";
    public static final String MOTHERS_GIVEN_NAME = "mothers_given_name";
    public static final String MOTHERS_SUR_NAME = "mothers_sur_name";
    public static final String MOTHERS_UID = "mothers_uid";
    public static final String MOTHERS_NID = "mothers_nid";
    public static final String MOTHERS_BRN = "mothers_brn";
    public static final String PLACE_OF_BIRTH = "place_of_birth";
    public static final String MARITAL_STATUS = "marital_status";
    public static final String MARRIAGE_ID = "marriage_id";
    public static final String SPOUSE_NAME_BANGLA = "spouse_name_bangla";
    public static final String SPOUSE_NAME = "spouse_name";
    public static final String SPOUSE_UID_NID = "spouse_uid_nid";
    public static final String RELIGION = "religion";
    public static final String BLOOD_GROUP = "blood_group";
    public static final String NATIONALITY = "nationality";
    public static final String DISABILITY = "disability";
    public static final String ETHNICITY = "ethnicity";
    public static final String HOLDING_NUMBER = "holding_number";
    public static final String STREET = "street";
    public static final String AREA_MOUJA = "area_mouja";
    public static final String VILLAGE = "village";
    public static final String POST_OFFICE = "post_office";
    public static final String POST_CODE = "post_code";
    public static final String RURAL_WARD_ID = "rural_ward_id";
    public static final String CITY_CORPORATION = "city_corporation_id";
    public static final String COUNTRY = "country_code";
    public static final String PERMANENT_ADDRESS_LINE = "permanent_address_line";
    public static final String PERMANENT_DIVISION_ID = "permanent_division_id";
    public static final String PERMANENT_DISTRICT_ID = "permanent_district_id";
    public static final String PERMANENT_UPAZILA_ID = "permanent_upazila_id";
    public static final String PERMANENT_UNION_OR_URBAN_WARD_ID = "permanent_union_or_urban_ward_id";
    public static final String PERMANENT_HOLDING_NUMBER = "permanent_holding_number";
    public static final String PERMANENT_STREET = "permanent_street";
    public static final String PERMANENT_AREA_MOUJA = "permanent_area_mouja";
    public static final String PERMANENT_VILLAGE = "permanent_village";
    public static final String PERMANENT_POST_OFFICE = "permanent_post_office";
    public static final String PERMANENT_POST_CODE = "permanent_post_code";
    public static final String PERMANENT_RURAL_WARD_ID = "permanent_rural_ward_id";
    public static final String PERMANENT_CITY_CORPORATION = "permanent_city_corporation_id";
    public static final String PERMANENT_COUNTRY = "permanent_country_code";
    public static final String FULL_NAME = "full_name";
    public static final String IS_ALIVE = "is_alive";
    public static final String RELATIONS = "relations";
    public static final String PRIMARY_CONTACT = "primary_contact";
    public static final String PHONE_NO = "phone_no";
    public static final String PRIMARY_CONTACT_NO = "primary_contact_no";

    public static final String PHONE_NUMBER_COUNTRY_CODE = "phone_number_country_code";
    public static final String PHONE_NUMBER_AREA_CODE = "phone_number_area_code";
    public static final String PHONE_NUMBER_EXTENSION = "phone_number_extension";

    public static final String PRIMARY_CONTACT_NUMBER_COUNTRY_CODE = "primary_contact_number_country_code";
    public static final String PRIMARY_CONTACT_NUMBER_AREA_CODE = "primary_contact_number_area_code";
    public static final String PRIMARY_CONTACT_NUMBER_EXTENSION = "primary_contact_number_extension";

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
        Where where = select(HEALTH_ID, CREATED_AT).from(CF_PENDING_APPROVAL_MAPPING)
                .where(eq(DIVISION_ID, catchment.getDivisionId()))
                .and(eq(DISTRICT_ID, catchment.getDistrictId()))
                .and(eq(UPAZILA_ID, catchment.getUpazilaId()));

        if (after != null) {
            where = where.and(gt(CREATED_AT, after));
        }
        if (before != null) {
            where = where.and(lt(CREATED_AT, before));
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
        return batch;
    }

    public static Update buildUpdateStmt(Patient patient, CassandraConverter converter) {
        return toUpdateQuery(CF_PATIENT, patient, null, converter);
    }

    public static Insert buildCreatePendingApprovalMappingStmt(PendingApprovalMapping mapping, CassandraConverter converter) {
        return createInsertQuery(CF_PENDING_APPROVAL_MAPPING, mapping, null, converter);
    }

    public static Delete buildDeletePendingApprovalMappingStmt(PendingApprovalMapping mapping, CassandraConverter converter) {
        return createDeleteQuery(CF_PENDING_APPROVAL_MAPPING, mapping, null, converter);
    }
}
