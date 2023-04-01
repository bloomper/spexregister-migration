package nu.fgv.register.migration.reader;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.model.Task;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
@Slf4j
public class TaskReader extends AbstractReader implements Reader {

    protected TaskReader(@Qualifier("sourceJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void read(final MigrationContext context) {
        jdbcTemplate.query("""
                        SELECT id, name, function_category_id, created_by, created_at, updated_by, updated_at
                        FROM functions""",
                rs -> {
                    context.getTasks().add(
                            Task.builder()
                                    .id(rs.getLong("id"))
                                    .name(rs.getString("name"))
                                    .category(context.getTaskCategories().stream()
                                            .filter(c -> {
                                                try {
                                                    return c.getId().equals(rs.getLong("function_category_id"));
                                                } catch (SQLException e) {
                                                    log.error("Unexpected SQL exception when finding category for task", e);
                                                    return false;
                                                }
                                            })
                                            .findFirst()
                                            .orElse(null)
                                    )
                                    .createdBy(rs.getString("created_by"))
                                    .createdAt(rs.getDate("created_at"))
                                    .lastModifiedBy(rs.getString("updated_by"))
                                    .lastModifiedAt(rs.getDate("updated_at"))
                                    .build()
                    );
                });
    }

}
