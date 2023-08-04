package nu.fgv.register.migration.reader;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.model.Event;
import nu.fgv.register.migration.model.News;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventReader extends AbstractReader implements Reader {

    protected EventReader(@Qualifier("sourceJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void read(final MigrationContext context) {
        jdbcTemplate.query("""
                        SELECT id, user_id, kind_id, created_by, created_at, updated_by, updated_at
                        FROM user_events
                        WHERE kind_id = 1""", // Only interested in logins
                rs -> {
                    context.getEvents().add(
                            Event.builder()
                                    .id(rs.getLong("id"))
                                    .event(Event.EventType.CREATE) // Only logins
                                    .source(Event.SourceType.SESSION) // That is the only event that the old system has
                                    .createdBy(rs.getString("user_id")) // created_by is not the relevant one
                                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                                    .build()
                    );
                });
    }

}
