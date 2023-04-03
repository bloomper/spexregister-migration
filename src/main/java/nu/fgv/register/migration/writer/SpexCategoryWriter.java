package nu.fgv.register.migration.writer;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.net.URL;
import java.sql.PreparedStatement;

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
                            (id, name, first_year, logo_content_type, created_by, created_at, last_modified_by, last_modified_at)
                            values
                            (%s, '%s', '%s', %s, '%s', '%s', '%s', '%s')""",
                    t.getId(), escapeSql(t.getName()), t.getFirstYear(),
                    hasText(t.getLogoContentType()) ? "'" + t.getLogoContentType() + "'" : null,
                    mapUser(context.getUsers(), t.getCreatedBy()), t.getCreatedAt(), mapUser(context.getUsers(), t.getLastModifiedBy()), t.getLastModifiedAt()));
            if (hasText(t.getLogoUrl())) {
                try (final BufferedInputStream inputStream = new BufferedInputStream(new URL(t.getLogoUrl()).openStream())) {
                    jdbcTemplate.update(connection -> {
                        PreparedStatement preparedStatement = connection.prepareStatement(String.format("UPDATE spex_category SET logo = ? WHERE id = %s", t.getId()));
                        preparedStatement.setBlob(1, inputStream);
                        return preparedStatement;
                    });
                } catch (Exception e) {
                    log.error("Unexpected error when writing logo for spex category", e);
                }
            }
        });

    }
}
