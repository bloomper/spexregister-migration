package nu.fgv.register.migration.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class SourceDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.source")
    public DataSourceProperties sourceDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource sourceDataSource(@Qualifier("sourceDataSourceProperties") final DataSourceProperties dataSourceProperties) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public JdbcTemplate sourceJdbcTemplate(@Qualifier("sourceDataSource") final DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
