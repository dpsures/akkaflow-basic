package org.suresh.learning.akkaflow.configuration;

import akka.actor.ActorSystem;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.suresh.learning.akkaflow.extension.SpringExtension;

import java.util.Properties;

@Configuration
@Lazy
@ComponentScan(basePackages = { "org.suresh.learning.akkaflow.services",
    "org.suresh.learning.akkaflow.actors", "org.suresh.learning.akkaflow.extension" })
public class ApplicationConfiguration {

    // The application context is needed to initialize the Akka Spring
    // Extension
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SpringExtension springExtension;

    /**
     * Actor system singleton for this application.
     */
    @Bean
    public ActorSystem actorSystem() {

        ActorSystem system = ActorSystem.create("AkkaTaskProcessing", akkaConfiguration());

        // Initialize the application context in the Akka Spring Extension
        springExtension.initialize(applicationContext);
        return system;
    }

    /**
     * Read configuration from application.conf file
     */
    @Bean
    public Config akkaConfiguration() {
        return ConfigFactory.load();
    }

    /**
     * Simple H2 based in memory backend using a connection pool.
     * Creates th only table needed.
     */
    @Bean
    public JdbcTemplate jdbcTemplate() throws Exception {

        // Disable c3p0 logging
        final Properties properties = new Properties(System.getProperties());
        properties.put("com.mchange.v2.log.MLog",
            "com.mchange.v2.log.FallbackMLog");
        properties.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL",
            "OFF");
        System.setProperties(properties);

        final ComboPooledDataSource source = new ComboPooledDataSource();
        source.setMaxPoolSize(100);
        source.setDriverClass("org.h2.Driver");
        source.setJdbcUrl("jdbc:h2:mem:taskdb");
        source.setUser("sa");
        source.setPassword("");

        JdbcTemplate template = new JdbcTemplate(source);
        template.update("CREATE TABLE tasks (id INT(11) AUTO_INCREMENT, " +
            "payload VARCHAR(255), updated DATETIME)");
        return template;
    }
}
