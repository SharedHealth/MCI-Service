package org.sharedhealth.mci.web.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import org.sharedhealth.mci.web.launch.Migrations;

import java.util.Map;

public class TestMigrations extends Migrations {

    public TestMigrations(Map<String, String> env) {
        super(env);
    }

    @Override
    protected Cluster connectCluster() {
        Cluster.Builder clusterBuilder = new Cluster.Builder();

        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setConsistencyLevel(ConsistencyLevel.QUORUM);


        PoolingOptions poolingOptions = new PoolingOptions();

        clusterBuilder
                .withPort(Integer.parseInt(env.get("CASSANDRA_PORT")))
                .withClusterName(env.get("CASSANDRA_KEYSPACE"))
                .withLoadBalancingPolicy(new RoundRobinPolicy())
                .withPoolingOptions(poolingOptions)
                .withProtocolVersion(Integer.parseInt(env.get("CASSANDRA_VERSION")))
                .withQueryOptions(queryOptions)
                .withReconnectionPolicy(new ConstantReconnectionPolicy(ONE_MINUTE))
                .addContactPoint(env.get("CASSANDRA_HOST"));
        return clusterBuilder.build();

    }

    @Override
    protected Session createSession(Cluster cluster) {
        String keyspace = env.get("CASSANDRA_KEYSPACE");

        Session session = cluster.connect();
        session.execute(
                String.format(
                        "CREATE KEYSPACE  IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', 'replication_factor':1}; ",
                        keyspace)
        );
        session.close();
        return cluster.connect(keyspace);
    }
}
