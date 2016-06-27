package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.model.GeneratedHIDBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static com.datastax.driver.core.utils.UUIDs.timeBased;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.CF_GENERATED_HID_BLOCKS;
import static org.sharedhealth.mci.domain.util.TestUtil.truncateAllColumnFamilies;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class GeneratedHidBlockRepositoryIT {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cqlTemplate;

    @Autowired
    private GeneratedHidBlockRepository hidBlockRepository;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
        truncateAllColumnFamilies(cqlTemplate);
    }

    @Test
    public void shouldInsertAHIDBlock() throws Exception {
        GeneratedHIDBlock hidBlockToInsert = new GeneratedHIDBlock(9100L, "MCI", 9100L, 9150L, 10L, null, timeBased());
        hidBlockRepository.saveGeneratedHidBlock(hidBlockToInsert);

        String cql = select().all().from(CF_GENERATED_HID_BLOCKS).toString();
        List<GeneratedHIDBlock> hidBlocks = cqlTemplate.select(cql, GeneratedHIDBlock.class);
        assertEquals(1, hidBlocks.size());
        assertEquals(hidBlockToInsert, hidBlocks.get(0));
    }

    @Test
    public void shouldRetrieveAHIDBlockBySeriesNo() throws Exception {
        GeneratedHIDBlock hidBlock1 = new GeneratedHIDBlock(9100L, "MCI", 9100L, 9150L, 10L, null, timeBased());
        GeneratedHIDBlock hidBlock2 = new GeneratedHIDBlock(9200L, "MCI", 9200L, 9250L, 10L, null, timeBased());
        GeneratedHIDBlock hidBlock3 = new GeneratedHIDBlock(9100L, "MCI", 9151L, 9199L, 10L, null, timeBased());
        cqlTemplate.insert(asList(hidBlock1, hidBlock2, hidBlock3));

        List<GeneratedHIDBlock> hidBlocks = hidBlockRepository.getPreGeneratedHidBlocks(9100L);

        assertEquals(2, hidBlocks.size());
        assertEquals(hidBlock1, hidBlocks.get(0));
        assertEquals(hidBlock3, hidBlocks.get(1));
    }
}