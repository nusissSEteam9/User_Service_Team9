package nus.iss.se.team9.user_service_team9.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "nus.iss.se.team9.user_service_team9.repo",
        entityManagerFactoryRef = "rdbmsEntityManagerFactory",
        transactionManagerRef = "rdbmsTransactionManager")
public class RdbmsDataSourceConfig {

    @Bean(name = "rdbmsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.rdbms")
    public DataSource rdbmsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "rdbmsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean rdbmsEntityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("rdbmsDataSource") DataSource rdbmsDataSource) {
        return builder
                .dataSource(rdbmsDataSource)
                .packages("nus.iss.se.team9.user_service_team9.entity")
                .persistenceUnit("rdbms")
                .build();
    }

    @Bean(name = "rdbmsTransactionManager")
    public PlatformTransactionManager rdbmsTransactionManager(
            @Qualifier("rdbmsEntityManagerFactory") EntityManagerFactory rdbmsEntityManagerFactory) {
        return new JpaTransactionManager(rdbmsEntityManagerFactory);
    }
}