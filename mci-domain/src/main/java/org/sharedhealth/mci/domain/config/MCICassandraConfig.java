package org.sharedhealth.mci.domain.config;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.SocketOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "org.sharedhealth.mci.domain.repository")
public class MCICassandraConfig extends AbstractCassandraConfiguration {

    @Autowired
    private MCIProperties mciProperties;

    @Override
    protected String getKeyspaceName() {
        return mciProperties.getCassandraKeySpace();
    }

    @Override
    protected String getContactPoints() {
        return mciProperties.getContactPoints();
    }

    @Override
    protected int getPort() {
        return mciProperties.getCassandraPort();
    }

    @Override
    protected AuthProvider getAuthProvider() {
        return new PlainTextAuthProvider(mciProperties.getCassandraUser(), mciProperties.getCassandraPassword());
    }

    @Override
    protected SocketOptions getSocketOptions() {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.setConnectTimeoutMillis(mciProperties.getCassandraTimeout());
        socketOptions.setReadTimeoutMillis(mciProperties.getCassandraTimeout());
        return socketOptions;
    }

    @Bean(name = "MCICassandraTemplate")
    public CassandraOperations CassandraTemplate() throws Exception {
        return new CassandraTemplate(session().getObject());
    }

}
