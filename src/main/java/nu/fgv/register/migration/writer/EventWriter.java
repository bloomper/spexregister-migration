package nu.fgv.register.migration.writer;

import nu.fgv.register.migration.MigrationContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventWriter extends AbstractWriter implements Writer {

    protected EventWriter(@Qualifier("targetJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void clean() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE event");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Override
    public void write(final MigrationContext context) {
        context.getEvents().forEach(t -> {
            final String mappedUser = mapUser(context.getUsers(), t.getCreatedBy());
            if (!"system".equals(mappedUser)) {
                jdbcTemplate.execute(String.format("""
                                INSERT INTO event
                                (id, event, source, created_by, created_at)
                                values
                                (%s, '%s', '%s', '%s', '%s')""",
                        t.getId(), t.getEvent().name(), t.getSource().name(), mappedUser, t.getCreatedAt()));
            }
        });
    }
}
