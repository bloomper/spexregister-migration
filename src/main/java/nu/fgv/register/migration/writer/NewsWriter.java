package nu.fgv.register.migration.writer;

import nu.fgv.register.migration.MigrationContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class NewsWriter extends AbstractWriter implements Writer {

    protected NewsWriter(@Qualifier("targetJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void clean() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE news");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Override
    public void write(final MigrationContext context) {
        context.getNews().forEach(t ->
                jdbcTemplate.execute(String.format("""
                                INSERT INTO news
                                (id, subject, text, visible_from, published, created_by, created_at, last_modified_by, last_modified_at)
                                values
                                (%s, '%s', '%s', '%s', 1, '%s', '%s', '%s', '%s')""",
                        t.getId(), escapeSql(t.getSubject()), escapeSql(t.getText()), t.getVisibleFrom(),
                        mapUser(context.getUsers(), t.getCreatedBy()), t.getCreatedAt(), mapUser(context.getUsers(), t.getLastModifiedBy()), t.getLastModifiedAt())));
    }
}
