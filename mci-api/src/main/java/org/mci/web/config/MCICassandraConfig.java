package org.mci.web.config;

import com.datastax.driver.core.SocketOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cassandra.core.CqlOperations;
import org.springframework.cassandra.core.CqlTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.java.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@EnableCassandraRepositories(basePackages = "org.mci")
public class MCICassandraConfig extends AbstractCassandraConfiguration {

    @Autowired
    private MCIProperties MCIProperties;

    @Override
    protected String getKeyspaceName() {
        return MCIProperties.getCassandraKeySpace();
    }

    @Override
    protected String getContactPoints() {
        return MCIProperties.getContactPoints();
    }

    @Override
    protected int getPort() {
        return MCIProperties.getCassandraPort();
    }

    @Override
    protected SocketOptions getSocketOptions() {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.setConnectTimeoutMillis(MCIProperties.getCassandraTimeout());
        socketOptions.setReadTimeoutMillis(MCIProperties.getCassandraTimeout());
        return socketOptions;
    }

    @Bean(name = "MCICassandraTemplate")
    public CqlOperations CassandraTemplate() throws Exception {
        return new CqlTemplate(session().getObject());
    }

}
