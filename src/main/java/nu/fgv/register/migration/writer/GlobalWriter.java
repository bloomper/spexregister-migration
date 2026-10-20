package nu.fgv.register.migration.writer;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GlobalWriter extends AbstractWriter implements Writer {

    protected GlobalWriter(@Qualifier("targetJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void clean() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE permission_grant_audit");
        jdbcTemplate.execute("TRUNCATE TABLE permission_grant");
        jdbcTemplate.execute("TRUNCATE TABLE image");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Override
    public void write(final MigrationContext context) {
        // Do nothing
    }

}
