package org.sharedhealth.mci.web.infrastructure.persistence;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.CF_PATIENT_AUDIT_LOG;
import static org.sharedhealth.mci.web.infrastructure.persistence.PatientRepositoryConstants.HEALTH_ID;

public class PatientAuditLogQueryBuilder {

    public static String buildFindByHidStmt(String healthId) {
        return select().from(CF_PATIENT_AUDIT_LOG).where(eq(HEALTH_ID, healthId)).toString();
    }
}
