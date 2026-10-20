package nu.fgv.register.migration.writer;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.model.Spex;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


import static org.springframework.util.StringUtils.hasText;

@Service
@Slf4j
public class SpexWriter extends AbstractWriter implements Writer {

    protected SpexWriter(@Qualifier("targetJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void clean() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE spex_details");
        jdbcTemplate.execute("TRUNCATE TABLE spex");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Override
    public void write(final MigrationContext context) {
        context.getSpex().stream().filter(s -> !s.isRevival()).forEach(t -> {
            jdbcTemplate.execute(String.format("""
                            INSERT INTO spex_details
                            (id, title, category_id, created_by, created_at, last_modified_by, last_modified_at, version)
                            values
                            (%s, '%s', %s, '%s', '%s', '%s', '%s', 0)""",
                    t.getDetails().getId(), escapeSql(t.getDetails().getTitle()),
                    t.getDetails().getCategory().getId(),
                    mapUser(context.getUsers(), t.getCreatedBy()), formatTimestamp(t.getCreatedAt()), mapUser(context.getUsers(), t.getLastModifiedBy()), formatTimestamp(t.getLastModifiedAt())));

            if (hasText(t.getDetails().getPosterUrl())) {
                try {
                    writeImage(t.getDetails().getPosterUrl(), "spex_details", "poster_id", t.getDetails().getId());
                } catch (Exception e) {
                    log.error("Unexpected error when writing poster for spex", e);
                }
            }

            jdbcTemplate.execute(String.format("""
                            INSERT INTO spex
                            (id, year, details_id, created_by, created_at, last_modified_by, last_modified_at, version)
                            values
                            (%s, '%s', %s, '%s', '%s', '%s', '%s', 0)""",
                    t.getId(), t.getYear(), t.getDetails().getId(),
                    mapUser(context.getUsers(), t.getCreatedBy()), formatTimestamp(t.getCreatedAt()), mapUser(context.getUsers(), t.getLastModifiedBy()), formatTimestamp(t.getLastModifiedAt())));
        });

        // Revivals
        context.getSpex().stream().filter(Spex::isRevival).forEach(t -> {
                jdbcTemplate.execute(String.format("""
                                INSERT INTO spex
                                (id, year, details_id, parent_id, created_by, created_at, last_modified_by, last_modified_at, version)
                                values
                                (%s, '%s', %s, %s, '%s', '%s', '%s', '%s', 0)""",
                        t.getId(), t.getYear(), t.getDetails().getId(), t.getParent().getId(),
                        mapUser(context.getUsers(), t.getCreatedBy()), formatTimestamp(t.getCreatedAt()), mapUser(context.getUsers(), t.getLastModifiedBy()), formatTimestamp(t.getLastModifiedAt())));
        });
    }
}
