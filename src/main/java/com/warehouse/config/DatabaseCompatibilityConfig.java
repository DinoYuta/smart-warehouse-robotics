package com.warehouse.config;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseCompatibilityConfig {

    @Bean
    CommandLineRunner missionStatusEnumCompatibility(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        return args -> {
            if (!isSqlServer(dataSource)) {
                return;
            }

            dropColumnCheckConstraints(jdbcTemplate, "missions", "status");
            dropColumnCheckConstraints(jdbcTemplate, "missions", "execution_step");
        };
    }

    private boolean isSqlServer(DataSource dataSource) throws SQLException {
        try (var connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            return databaseProductName != null
                    && databaseProductName.toLowerCase().contains("microsoft sql server");
        }
    }

    private void dropColumnCheckConstraints(JdbcTemplate jdbcTemplate, String tableName, String columnName) {
        List<String> constraintNames = jdbcTemplate.queryForList(
                """
                SELECT cc.name
                FROM sys.check_constraints cc
                JOIN sys.tables t ON cc.parent_object_id = t.object_id
                WHERE t.name = ?
                  AND LOWER(cc.definition) LIKE ?
                """,
                String.class,
                tableName,
                "%" + columnName.toLowerCase() + "%"
        );

        for (String constraintName : constraintNames) {
            jdbcTemplate.execute("ALTER TABLE " + quoteIdentifier(tableName)
                    + " DROP CONSTRAINT " + quoteIdentifier(constraintName));
        }
    }

    private String quoteIdentifier(String identifier) {
        return "[" + identifier.replace("]", "]]") + "]";
    }
}
