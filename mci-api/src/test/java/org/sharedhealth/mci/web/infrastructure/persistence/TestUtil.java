package org.sharedhealth.mci.web.infrastructure.persistence;

import org.springframework.data.cassandra.core.CassandraOperations;

import java.util.List;

import static java.util.Arrays.asList;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.*;

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
}
