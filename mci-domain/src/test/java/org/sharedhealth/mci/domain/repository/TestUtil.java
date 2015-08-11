package org.sharedhealth.mci.domain.repository;

import com.datastax.driver.core.utils.UUIDs;
import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableList;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;

public class TestUtil {

    public static void setupApprovalsConfig(CassandraOperations cassandraOps) {
        cassandraOps.execute("TRUNCATE " + CF_APPROVAL_FIELDS);
        cassandraOps.execute("INSERT INTO approval_fields (\"field\", \"option\") VALUES ('gender', 'NA')");
        cassandraOps.execute("INSERT INTO approval_fields (\"field\", \"option\") VALUES ('occupation', 'NA')");
        cassandraOps.execute("INSERT INTO approval_fields (\"field\", \"option\") VALUES ('date_of_birth', 'NU')");
        cassandraOps.execute("INSERT INTO approval_fields (\"field\", \"option\") VALUES ('phone_number', 'NA')");
        cassandraOps.execute("INSERT INTO approval_fields (\"field\", \"option\") VALUES ('present_address', 'NA')");
    }

    public static void truncateAllColumnFamilies(CassandraOperations cassandraOps) {
        List<String> cfs = getAllColumnFamilies();
        for (String cf : cfs) {
            cassandraOps.execute("truncate " + cf);
        }
    }

    public static String buildAddressChangeSet() {
        return "{\n" +
                "    \"present_address\":{\n" +
                "        \"old_value\": {\n" +
                "            \"address_line\":\"test2\",\n" +
                "            \"division_id\":\"10\",\n" +
                "            \"district_id\":\"20\",\n" +
                "            \"upazila_id\":\"30\",\n" +
                "            \"city_corporation_id\":\"40\",\n" +
                "            \"union_or_urban_ward_id\":\"50\",\n" +
                "            \"rural_ward_id\":\"60\",\n" +
                "            \"country_code\":\"050\"\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }

    private static List<String> getAllColumnFamilies() {
        return asList(
                CF_PATIENT,
                CF_PATIENT_DUPLICATE,
                CF_PATIENT_DUPLICATE_IGNORED,
                CF_NID_MAPPING,
                CF_BRN_MAPPING,
                CF_UID_MAPPING,
                CF_HOUSEHOLD_CODE_MAPPING,
                CF_PHONE_NUMBER_MAPPING,
                CF_NAME_MAPPING,
                CF_PENDING_APPROVAL_MAPPING,
                CF_CATCHMENT_MAPPING,
                CF_PATIENT_UPDATE_LOG,
                CF_PATIENT_AUDIT_LOG,
                CF_MARKER
        );
    }

}
