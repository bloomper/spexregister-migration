package nu.fgv.register.migration.reader;

import org.springframework.jdbc.core.JdbcTemplate;

public class AbstractReader {

    protected final JdbcTemplate jdbcTemplate;

    protected AbstractReader(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

}
