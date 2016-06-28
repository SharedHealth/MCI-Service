package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.util.BaseIntegrationTest;
import org.sharedhealth.mci.web.model.GeneratedHIDBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.CF_GENERATED_HID_BLOCKS;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class GeneratedHidBlockRepositoryIT extends BaseIntegrationTest {
    @Autowired
    private GeneratedHidBlockRepository hidBlockRepository;

    @Test
    public void shouldInsertAHIDBlock() throws Exception {
        GeneratedHIDBlock hidBlockToInsert = new GeneratedHIDBlock(9100L, "MCI", 9100L, 9150L, 10L, null, timeBased());
        hidBlockRepository.saveGeneratedHidBlock(hidBlockToInsert);

        String cql = select().all().from(CF_GENERATED_HID_BLOCKS).toString();
        List<GeneratedHIDBlock> hidBlocks = cassandraOps.select(cql, GeneratedHIDBlock.class);
        assertEquals(1, hidBlocks.size());
        assertEquals(hidBlockToInsert, hidBlocks.get(0));
    }

    @Test
    public void shouldRetrieveAHIDBlockBySeriesNo() throws Exception {
        GeneratedHIDBlock hidBlock1 = new GeneratedHIDBlock(9100L, "MCI", 9100L, 9150L, 10L, null, timeBased());
        GeneratedHIDBlock hidBlock2 = new GeneratedHIDBlock(9200L, "MCI", 9200L, 9250L, 10L, null, timeBased());
        GeneratedHIDBlock hidBlock3 = new GeneratedHIDBlock(9100L, "MCI", 9151L, 9199L, 10L, null, timeBased());
        cassandraOps.insert(asList(hidBlock1, hidBlock2, hidBlock3));

        List<GeneratedHIDBlock> hidBlocks = hidBlockRepository.getPreGeneratedHidBlocks(9100L);

        assertEquals(2, hidBlocks.size());
        assertEquals(hidBlock1, hidBlocks.get(0));
        assertEquals(hidBlock3, hidBlocks.get(1));
    }
}