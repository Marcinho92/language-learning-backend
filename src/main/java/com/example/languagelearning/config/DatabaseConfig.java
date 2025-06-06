package com.example.languagelearning.config;

import com.google.cloud.sql.postgres.SocketFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Value("${DATABASE_NAME:language_learning}")
    private String databaseName;

    @Value("${CLOUD_SQL_CONNECTION_NAME}")
    private String instanceConnectionName;

    @Value("${DATABASE_USER}")
    private String databaseUser;

    @Value("${DATABASE_PASSWORD}")
    private String databasePassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(String.format("jdbc:postgresql:///%s", databaseName));
        config.setUsername(databaseUser);
        config.setPassword(databasePassword);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(20000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1200000);

        config.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
        config.addDataSourceProperty("cloudSqlInstance", instanceConnectionName);
        config.addDataSourceProperty("enableIamAuth", "false");
        config.addDataSourceProperty("sslmode", "disable");

        return new HikariDataSource(config);
    }
}