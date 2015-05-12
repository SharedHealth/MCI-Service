package org.sharedhealth.mci.web.infrastructure.persistence;

import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.addAll;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.*;

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

    public static Set<String> asSet(String... values) {
        Set<String> set = new HashSet<>();
        addAll(set, values);
        return set;
    }
}
