package nu.fgv.register.migration.writer;

import nu.fgv.register.migration.MigrationContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SpexareWriter extends AbstractWriter implements Writer {

    protected SpexareWriter(@Qualifier("targetJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void clean() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE actor");
        jdbcTemplate.execute("TRUNCATE TABLE task_activity");
        jdbcTemplate.execute("TRUNCATE TABLE spex_activity");
        jdbcTemplate.execute("TRUNCATE TABLE activity");
        jdbcTemplate.execute("TRUNCATE TABLE membership");
        jdbcTemplate.execute("TRUNCATE TABLE address");
        jdbcTemplate.execute("TRUNCATE TABLE consent");
        jdbcTemplate.execute("TRUNCATE TABLE toggle");
        jdbcTemplate.execute("TRUNCATE TABLE tagging");
        jdbcTemplate.execute("TRUNCATE TABLE spexare");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Override
    public void write(final MigrationContext context) {
    }
}
