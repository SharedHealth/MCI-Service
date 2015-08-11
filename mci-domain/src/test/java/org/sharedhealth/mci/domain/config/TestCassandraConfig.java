package org.sharedhealth.mci.domain.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@Configuration
@ComponentScan(basePackages = {"org.sharedhealth.mci", "org.sharedhealth.mci.cassandra.migrations"})
@EnableCassandraRepositories(basePackages = "org.sharedhealth.mci")
public class TestCassandraConfig extends MCICassandraConfig{
    
}
