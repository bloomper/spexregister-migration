package nu.fgv.register.migration.writer;

import org.springframework.jdbc.core.JdbcTemplate;

public class AbstractWriter {

    protected final JdbcTemplate jdbcTemplate;

    protected AbstractWriter(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    static String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("'", "''");
    }

}
