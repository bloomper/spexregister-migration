package nu.fgv.register.migration.reader;

import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.model.TaskCategory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskCategoryReader extends AbstractReader implements Reader {

    protected TaskCategoryReader(@Qualifier("sourceJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void read(final MigrationContext context) {
        jdbcTemplate.query("""
                        SELECT id, name, has_actor, created_by, created_at, updated_by, updated_at
                        FROM function_categories""",
                rs -> {
                    context.getTaskCategories().add(
                            TaskCategory.builder()
                                    .id(rs.getLong("id"))
                                    .name(rs.getString("name"))
                                    .hasActor(rs.getBoolean("has_actor"))
                                    .createdBy(rs.getString("created_by"))
                                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                                    .lastModifiedBy(rs.getString("updated_by"))
                                    .lastModifiedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                                    .build()
                    );
                });

    }
}
