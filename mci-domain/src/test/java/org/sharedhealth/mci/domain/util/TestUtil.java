package org.sharedhealth.mci.domain.util;

import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableList;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.*;

public class TestUtil {

    public static void setupLocation(CassandraOperations cassandraOps) {
        cassandraOps.execute("TRUNCATE " + CF_LOCATIONS);
        cassandraOps.execute("INSERT INTO locations (\"code\", \"name\", \"parent\") VALUES ('01', 'Union X', '20134942')");

        cassandraOps.execute("INSERT INTO locations (\"code\", \"name\", \"parent\") VALUES ('64', 'Upazila X', '5573')");
        cassandraOps.execute("INSERT INTO locations (\"code\", \"name\", \"parent\") VALUES ('26', 'Division Y', '30')");
        cassandraOps.execute("INSERT INTO locations (\"code\", \"name\", \"parent\") VALUES ('09', 'Upazila 1', '1004')");
        cassandraOps.execute("INSERT INTO locations (\"code\", \"name\", \"parent\") VALUES ('20', 'City Corp 1', '100409')");
        cassandraOps.execute("INSERT INTO locations (\"code\", \"name\", \"parent\") VALUES ('18', 'City Corp 2', '3026')");
        cassandraOps.execute("INSERT INTO locations (\"code\", \"name\", \"parent\") VALUES ('01', 'Union 1', '10040920')");
        cassandraOps.execute("INSERT INTO locations (\"code\", \"name\", \"parent\") VALUES ('06', 'Union 2', '10040920')");
    }

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
                CF_APPROVAL_FIELDS,
                CF_LOCATIONS,
                CF_MCI_HEALTH_ID,
                CF_GENERATED_HID_RANGE,
                CF_GENERATED_HID_BLOCKS,
                CF_ORG_HEALTH_ID,
                CF_MARKER,
                CF_FAILED_EVENTS
        );
    }

    public static Set<String> asSet(String... values) {
        Set<String> set = new HashSet<>();
        addAll(set, values);
        return set;
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

    public static List<UUID> buildTimeUuids() throws InterruptedException {
        List<UUID> timeUuids = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            timeUuids.add(TimeUuidUtil.uuidForDate(new Date()));
        }
        return unmodifiableList(timeUuids);
    }
}
