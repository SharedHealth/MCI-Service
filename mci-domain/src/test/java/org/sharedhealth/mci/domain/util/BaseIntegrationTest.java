package org.sharedhealth.mci.domain.util;

import net.sf.ehcache.CacheManager;
import org.junit.After;
import org.sharedhealth.mci.domain.config.EnvironmentMock;
import org.sharedhealth.mci.domain.config.TestCassandraConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(initializers = EnvironmentMock.class, classes = TestCassandraConfig.class)
public class BaseIntegrationTest {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier("MCICassandraTemplate")
    protected CassandraOperations cassandraOps;

    @After
    public void tearDown() throws Exception {
        CacheManager.getInstance().clearAll();
        TestUtil.truncateAllColumnFamilies(cassandraOps);
    }
}