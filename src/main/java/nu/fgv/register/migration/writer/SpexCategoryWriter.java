package nu.fgv.register.migration.writer;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


import static org.springframework.util.StringUtils.hasText;

@Service
@Slf4j
public class SpexCategoryWriter extends AbstractWriter implements Writer {

    protected SpexCategoryWriter(@Qualifier("targetJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void clean() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE spex_category");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Override
    public void write(final MigrationContext context) {
        context.getSpexCategories().forEach(t -> {
            jdbcTemplate.execute(String.format("""
                            INSERT INTO spex_category
                            (id, name, first_year, created_by, created_at, last_modified_by, last_modified_at, version)
                            values
                            (%s, '%s', '%s', '%s', '%s', '%s', '%s', 0)""",
                    t.getId(), escapeSql(t.getName()), t.getFirstYear(),
                    mapUser(context.getUsers(), t.getCreatedBy()), formatTimestamp(t.getCreatedAt()), mapUser(context.getUsers(), t.getLastModifiedBy()), formatTimestamp(t.getLastModifiedAt())));

            if (hasText(t.getLogoUrl())) {
                try {
                    writeImage(t.getLogoUrl(), "spex_category", "logo_id", t.getId());
                } catch (Exception e) {
                    log.error("Unexpected error when writing logo for spex category", e);
                }
            }
        });

    }
}
