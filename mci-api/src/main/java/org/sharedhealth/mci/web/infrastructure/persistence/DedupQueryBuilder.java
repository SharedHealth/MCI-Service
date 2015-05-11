package org.sharedhealth.mci.web.infrastructure.persistence;

import org.sharedhealth.mci.web.mapper.Catchment;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.CATCHMENT_ID;
import static org.sharedhealth.mci.web.infrastructure.persistence.RepositoryConstants.CF_PATIENT_DUPE;

public class DedupQueryBuilder {

    public static String buildFindByCatchmentStmt(Catchment catchment) {
        return select().from(CF_PATIENT_DUPE).where(eq(CATCHMENT_ID, catchment.getId())).toString();
    }
}
