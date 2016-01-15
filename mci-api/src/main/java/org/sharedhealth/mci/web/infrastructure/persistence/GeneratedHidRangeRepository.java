package org.sharedhealth.mci.web.infrastructure.persistence;


import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.sharedhealth.mci.domain.repository.BaseRepository;
import org.sharedhealth.mci.web.model.GeneratedHidRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.BLOCK_BEGINS_AT;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.CF_GENERATED_HID_RANGE;

@Component
public class GeneratedHidRangeRepository extends BaseRepository {

    @Autowired
    public GeneratedHidRangeRepository(@Qualifier("MCICassandraTemplate") CassandraOperations cassandraOps) {
        super(cassandraOps);
    }

    public List<GeneratedHidRange> getPreGeneratedHidRanges(long blockBiginsAt) {
        Select selectHIDRanges = QueryBuilder.select().from(CF_GENERATED_HID_RANGE);
        selectHIDRanges.where(eq(BLOCK_BEGINS_AT, blockBiginsAt));
        return cassandraOps.select(selectHIDRanges, GeneratedHidRange.class);
    }

    public GeneratedHidRange saveGeneratedHidRange(GeneratedHidRange generatedHidRange) {
        return cassandraOps.insert(generatedHidRange);
    }
}
