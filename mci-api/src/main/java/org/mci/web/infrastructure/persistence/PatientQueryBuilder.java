package org.mci.web.infrastructure.persistence;

public class PatientQueryBuilder {

    public static final String HEALTH_ID = "health_id";
    public static final String NATIONAL_ID = "national_id";
    public static final String FIRST_NAME = "first_name";
    public static final String MIDDLE_NAME = "middle_name";
    public static final String LAST_NAME = "last_name";
    public static final String DATE_OF_BIRTH = "date_of_birth";
    public static final String GENDER = "gender";
    public static final String OCCUPATION = "occupation";
    public static final String EDU_LEVEL = "edu_level";
    public static final String PRIMARY_CONTACT = "primary_contact";
    public static final String ADDRESS_LINE = "address_line";
    public static final String DIVISION_ID = "division_id";
    public static final String DISTRICT_ID = "district_id";
    public static final String UPAZILLA_ID = "upazilla_id";
    public static final String UNION_ID = "union_id";

    public static String getCreateQuery() {
        return String.format("INSERT INTO patient (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) values " +
                "('%%s', '%%s', '%%s', '%%s', '%%s', '%%s', '%%s', '%%s', '%%s', '%%s', '%%s', '%%s', '%%s', '%%s', '%%s');",
                HEALTH_ID, NATIONAL_ID, FIRST_NAME, MIDDLE_NAME, LAST_NAME, DATE_OF_BIRTH, GENDER, OCCUPATION, EDU_LEVEL,
                PRIMARY_CONTACT, ADDRESS_LINE, DIVISION_ID, DISTRICT_ID, UPAZILLA_ID, UNION_ID);
    }

    public static String getFindByHealthIdQuery() {
        return String.format("SELECT * FROM patient WHERE %s = '%%s'", HEALTH_ID);
    }

    public static String getFindByNationalIdQuery() {
        return String.format("SELECT * FROM patient WHERE %s = '%%s'", NATIONAL_ID);
    }
}
