package org.sharedhealth.mci.web.infrastructure.persistence;

public class PatientQueryBuilder {

    public static final String HEALTH_ID = "health_id";
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
    public static final String UPAZILLA_ID = "upazilla_id";
    public static final String UNION_ID = "union_id";
    public static final String BIN_BRN = "bin_brn";
    public static final String UID ="uid";
    public static final String FATHERS_NAME_BANGLA ="fathers_name_bangla";
    public static final String FATHERS_GIVEN_NAME ="fathers_given_name";
    public static final String FATHERS_SUR_NAME ="fathers_sur_name";
    public static final String FATHERS_UID ="fathers_uid";
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
    public static final String WARD = "ward_id";
    public static final String THANA = "thana_id";
    public static final String CITY_CORPORATION = "city_corporation_id";
    public static final String COUNTRY = "country_code";
    public static final String PERMANENT_ADDRESS_LINE = "permanent_address_line";
    public static final String PERMANENT_DIVISION_ID = "permanent_division_id";
    public static final String PERMANENT_DISTRICT_ID = "permanent_district_id";
    public static final String PERMANENT_UPAZILLA_ID = "permanent_upazilla_id";
    public static final String PERMANENT_UNION_ID = "permanent_union_id";
    public static final String PERMANENT_HOLDING_NUMBER = "permanent_holding_number";
    public static final String PERMANENT_STREET = "permanent_street";
    public static final String PERMANENT_AREA_MOUJA = "permanent_area_mouja";
    public static final String PERMANENT_VILLAGE = "permanent_village";
    public static final String PERMANENT_POST_OFFICE = "permanent_post_office";
    public static final String PERMANENT_POST_CODE = "permanent_post_code";
    public static final String PERMANENT_WARD = "permanent_ward";
    public static final String PERMANENT_THANA = "permanent_thana";
    public static final String PERMANENT_CITY_CORPORATION = "permanent_city_corporation";
    public static final String PERMANENT_COUNTRY = "permanent_country";
    public static final String FULL_NAME = "full_name";
    public static final String IS_ALIVE = "is_alive";
    public static final String RELATIONS = "relations";
    public static final String PRIMARY_CONTACT = "primary_contact";
    public static final String CELL_NO = "cell_no";
    public static final String PRIMARY_CELL_NO = "primary_cell_no";

    public static String getCreateQuery() {
        return String.format("INSERT INTO patient ( %s,%s,%s,%s,%s,%s,%s,%s,%s,%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s,%s) values " +
                        "('%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s','%%s' );",
                HEALTH_ID,
                NATIONAL_ID,
                BIN_BRN,
                FULL_NAME_BANGLA,
                GIVEN_NAME,
                SUR_NAME,
                DATE_OF_BIRTH,
                GENDER,
                OCCUPATION,
                EDU_LEVEL,
                FATHERS_NAME_BANGLA,
                FATHERS_GIVEN_NAME,
                FATHERS_SUR_NAME,
                FATHERS_BRN,
                FATHERS_NID,
                FATHERS_UID,
                MOTHERS_NAME_BANGLA,
                MOTHERS_GIVEN_NAME,
                MOTHERS_SUR_NAME,
                MOTHERS_BRN,
                MOTHERS_NID,
                MOTHERS_UID,
                UID,
                PLACE_OF_BIRTH,
                RELIGION,
                BLOOD_GROUP,
                NATIONALITY,
                DISABILITY,
                ETHNICITY,
                IS_ALIVE,
                ADDRESS_LINE,
                DIVISION_ID,
                DISTRICT_ID,
                UPAZILLA_ID,
                UNION_ID,
                HOLDING_NUMBER,
                STREET,
                AREA_MOUJA,
                VILLAGE,
                POST_OFFICE,
                POST_CODE,
                WARD,
                THANA,
                CITY_CORPORATION,
                COUNTRY,
                PERMANENT_ADDRESS_LINE,
                PERMANENT_DIVISION_ID,
                PERMANENT_DISTRICT_ID,
                PERMANENT_UPAZILLA_ID,
                PERMANENT_UNION_ID,
                PERMANENT_HOLDING_NUMBER,
                PERMANENT_STREET,
                PERMANENT_AREA_MOUJA,
                PERMANENT_VILLAGE,
                PERMANENT_POST_OFFICE,
                PERMANENT_POST_CODE,
                PERMANENT_WARD,
                PERMANENT_THANA,
                PERMANENT_CITY_CORPORATION,
                PERMANENT_COUNTRY,
                FULL_NAME,
                RELATIONS,
                PRIMARY_CONTACT,
                CELL_NO,
                PRIMARY_CELL_NO

        );
    }

    public static String getUpdateQuery() {
        return String.format("UPDATE patient SET  %s='%%s',%s='%%s',%s='%%s',%s='%%s',%s='%%s',%s='%%s',%s='%%s',%s='%%s',%s='%%s',%s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s', %s='%%s' WHERE %s ='%%s'",
                NATIONAL_ID,
                BIN_BRN,
                FULL_NAME_BANGLA,
                GIVEN_NAME,
                SUR_NAME,
                DATE_OF_BIRTH,
                GENDER,
                OCCUPATION,
                EDU_LEVEL,
                FATHERS_NAME_BANGLA,
                FATHERS_GIVEN_NAME,
                FATHERS_SUR_NAME,
                FATHERS_BRN,
                FATHERS_NID,
                FATHERS_UID,
                MOTHERS_NAME_BANGLA,
                MOTHERS_GIVEN_NAME,
                MOTHERS_SUR_NAME,
                MOTHERS_BRN,
                MOTHERS_NID,
                MOTHERS_UID,
                UID,
                PLACE_OF_BIRTH,
                RELIGION,
                BLOOD_GROUP,
                NATIONALITY,
                DISABILITY,
                ETHNICITY,
                IS_ALIVE,
                ADDRESS_LINE,
                DIVISION_ID,
                DISTRICT_ID,
                UPAZILLA_ID,
                UNION_ID,
                HOLDING_NUMBER,
                STREET,
                AREA_MOUJA,
                VILLAGE,
                POST_OFFICE,
                POST_CODE,
                WARD,
                THANA,
                CITY_CORPORATION,
                COUNTRY,
                PERMANENT_ADDRESS_LINE,
                PERMANENT_DIVISION_ID,
                PERMANENT_DISTRICT_ID,
                PERMANENT_UPAZILLA_ID,
                PERMANENT_UNION_ID,
                PERMANENT_HOLDING_NUMBER,
                PERMANENT_STREET,
                PERMANENT_AREA_MOUJA,
                PERMANENT_VILLAGE,
                PERMANENT_POST_OFFICE,
                PERMANENT_POST_CODE,
                PERMANENT_WARD,
                PERMANENT_THANA,
                PERMANENT_CITY_CORPORATION,
                PERMANENT_COUNTRY,
                FULL_NAME,
                RELATIONS,
                PRIMARY_CONTACT,
                CELL_NO,
                PRIMARY_CELL_NO,
                HEALTH_ID
        );

    }

    public static String getFindByHealthIdQuery() {
        return String.format("SELECT * FROM patient WHERE %s = '%%s'", HEALTH_ID);
    }

    public static String getFindByNationalIdQuery() {
        return String.format("SELECT * FROM patient WHERE %s = '%%s'", NATIONAL_ID);
    }

    public static String getFindByBirthRegistrationNumberQuery() {
        return String.format("SELECT * FROM patient WHERE %s = '%%s'", BIN_BRN);
    }

    public static String getFindByNameQuery() {
        return String.format("SELECT * FROM patient WHERE %s = '%%s'", FULL_NAME);
    }

    public static String getFindByUidQuery() {
        return String.format("SELECT * FROM patient WHERE %s = '%%s'", UID);
    }

    public static String getFindAllQuery() {
        return String.format("SELECT * FROM patient");
    }
}
