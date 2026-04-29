package com.mmlm.useradmin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaUpgradeRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SchemaUpgradeRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public SchemaUpgradeRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureUserThemeColumn();
    }

    private void ensureUserThemeColumn() {
        Integer columnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " +
                        "FROM information_schema.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() " +
                        "AND TABLE_NAME = 'sys_user' " +
                        "AND COLUMN_NAME = 'theme'",
                Integer.class
        );

        if (columnCount == null || columnCount.intValue() == 0) {
            log.warn("Missing sys_user.theme column detected. Applying compatibility upgrade.");
            jdbcTemplate.execute(
                    "ALTER TABLE sys_user " +
                            "ADD COLUMN theme VARCHAR(16) NOT NULL DEFAULT 'light' AFTER remark"
            );
        }

        jdbcTemplate.update(
                "UPDATE sys_user " +
                        "SET theme = 'light' " +
                        "WHERE theme IS NULL OR theme NOT IN ('light', 'dark')"
        );
    }
}
