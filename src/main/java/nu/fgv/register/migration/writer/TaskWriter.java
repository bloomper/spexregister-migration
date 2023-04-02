package nu.fgv.register.migration.writer;

import nu.fgv.register.migration.MigrationContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskWriter extends AbstractWriter implements Writer {

    protected TaskWriter(@Qualifier("targetJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void clean() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE task");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Override
    public void write(final MigrationContext context) {
        context.getTasks().forEach(t ->
                jdbcTemplate.execute(String.format("""
                                INSERT INTO task
                                (id, name, category_id, created_by, created_at, last_modified_by, last_modified_at)
                                values
                                (%s, '%s', %s, '%s', '%s', '%s', '%s')""",
                        t.getId(), escapeSql(t.getName()), t.getCategory().getId(),
                        t.getCreatedBy(), t.getCreatedAt(), t.getLastModifiedBy(), t.getLastModifiedAt())));
    }
}
