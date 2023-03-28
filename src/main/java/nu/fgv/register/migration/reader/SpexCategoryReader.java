package nu.fgv.register.migration.reader;

import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.model.SpexCategory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SpexCategoryReader extends AbstractReader implements Reader {

    protected SpexCategoryReader(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void read(final MigrationContext context) {
        jdbcTemplate.query("""
                        SELECT id, name, first_year, created_by, created_at, updated_by, updated_at
                        FROM spex_categories""",
                rs -> {
                    context.getSpexCategories().add(
                            SpexCategory.builder()
                                    .id(rs.getLong("id"))
                                    .name(rs.getString("name"))
                                    .firstYear(rs.getString("first_year"))
                                    .createdBy(rs.getString("created_by"))
                                    .createdAt(rs.getDate("created_at"))
                                    .lastModifiedBy(rs.getString("updated_by"))
                                    .lastModifiedAt(rs.getDate("updated_at"))
                                    .build()
                    );
                });

    }
}
