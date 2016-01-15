package org.sharedhealth.mci.web.infrastructure.persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.mci.domain.config.EnvironmentMock;
import org.sharedhealth.mci.web.launch.WebMvcConfig;
import org.sharedhealth.mci.web.model.GeneratedHidRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.sharedhealth.mci.domain.constant.RepositoryConstants.CF_GENERATED_HID_RANGE;
import static org.sharedhealth.mci.domain.repository.TestUtil.truncateAllColumnFamilies;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(initializers = EnvironmentMock.class, classes = WebMvcConfig.class)
public class GeneratedHidRangeRepositoryIT {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier("MCICassandraTemplate")
    private CassandraOperations cqlTemplate;

    @Autowired
    private GeneratedHidRangeRepository hidRangeRepository;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
        truncateAllColumnFamilies(cqlTemplate);
    }

    @Test
    public void shouldInsertAHIDRange() throws Exception {
        GeneratedHidRange hidRangeToInsert = new GeneratedHidRange(91L, 9100L, 9150L, "MCI", null);
        hidRangeRepository.saveGeneratedHidRange(hidRangeToInsert);

        String cql = select().all().from(CF_GENERATED_HID_RANGE).toString();
        List<GeneratedHidRange> hidRanges = cqlTemplate.select(cql, GeneratedHidRange.class);
        assertEquals(1, hidRanges.size());
        assertEquals(hidRangeToInsert, hidRanges.get(0));
    }

    @Test
    public void shouldRetrieveAHIDRangeByBlockBiginKey() throws Exception {
        GeneratedHidRange hidRange1 = new GeneratedHidRange(91L, 9100L, 9150L, "MCI", null);
        GeneratedHidRange hidRange2 = new GeneratedHidRange(92L, 9200L, 9250L, "MCI", null);
        GeneratedHidRange hidRange3 = new GeneratedHidRange(91L, 9151L, 9199L, "MCI", null);
        cqlTemplate.insert(asList(hidRange1, hidRange2, hidRange3));

        List<GeneratedHidRange> hidRanges = hidRangeRepository.getPreGeneratedHidRanges(91L);

        assertEquals(2, hidRanges.size());
        assertEquals(hidRange1, hidRanges.get(0));
        assertEquals(hidRange3, hidRanges.get(1));
    }
}