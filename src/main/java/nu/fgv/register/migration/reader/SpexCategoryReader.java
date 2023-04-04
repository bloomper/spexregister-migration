package nu.fgv.register.migration.reader;

import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.model.SpexCategory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import static org.springframework.util.StringUtils.hasText;

@Service
public class SpexCategoryReader extends AbstractReader implements Reader {

    protected SpexCategoryReader(@Qualifier("sourceJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void read(final MigrationContext context) {
        jdbcTemplate.query("""
                        SELECT id, name, first_year, logo_file_name, logo_content_type, created_by, created_at, updated_by, updated_at
                        FROM spex_categories""",
                rs -> {
                    context.getSpexCategories().add(
                            SpexCategory.builder()
                                    .id(rs.getLong("id"))
                                    .name(rs.getString("name"))
                                    .firstYear(rs.getString("first_year"))
                                    .logoUrl(hasText(rs.getString("logo_file_name")) ? String.format("https://register.fgv.nu/system/logos/%s/original/%s", rs.getLong("id"), rs.getString("logo_file_name")) : null)
                                    .logoContentType(rs.getString("logo_content_type"))
                                    .createdBy(rs.getString("created_by"))
                                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                                    .lastModifiedBy(rs.getString("updated_by"))
                                    .lastModifiedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                                    .build()
                    );
                });

    }
}
