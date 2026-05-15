package com.smart.parking.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource(
            @Value("${DB_URL:}") String dbUrl,
            @Value("${DB_USERNAME:}") String dbUser,
            @Value("${DB_PASSWORD:}") String dbPass,
            @Value("${DATABASE_URL:}") String databaseUrl
    ) {
        String jdbcUrl = (dbUrl == null || dbUrl.isBlank()) ? null : dbUrl.trim();
        String username = (dbUser == null || dbUser.isBlank()) ? null : dbUser.trim();
        String password = (dbPass == null || dbPass.isBlank()) ? null : dbPass.trim();

        if ((jdbcUrl == null || jdbcUrl.isEmpty()) && databaseUrl != null && !databaseUrl.isBlank()) {
            // Handle postgres://user:pass@host:port/dbname (or postgresql://...)
            String noPrefix = databaseUrl.replaceFirst("^postgres(?:ql)?://", "");
            String[] parts = noPrefix.split("@", 2);
            if (parts.length == 2) {
                String userpass = parts[0];
                String hostAndDb = parts[1];
                String[] up = userpass.split(":", 2);
                username = up.length > 0 ? up[0] : username;
                password = up.length > 1 ? up[1] : password;

                String[] hostDb = hostAndDb.split("/", 2);
                String host = hostDb[0];
                String dbName = hostDb.length > 1 ? hostDb[1] : "";
                jdbcUrl = "jdbc:postgresql://" + host + (dbName.isEmpty() ? "" : "/" + dbName);
            }
        }

        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            // let Spring Boot fallback to its defaults (e.g., embedded) which will surface clearer errors
            throw new IllegalStateException("No database URL configured. Set DB_URL (jdbc) or DATABASE_URL (postgres://) environment variable.");
        }

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        if (username != null) cfg.setUsername(username);
        if (password != null) cfg.setPassword(password);

        return new HikariDataSource(cfg);
    }
}
