package nu.fgv.register.migration.reader;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.model.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TagReader extends AbstractReader implements Reader {

    protected TagReader(@Qualifier("sourceJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void read(final MigrationContext context) {
        jdbcTemplate.query("""
                        SELECT id, name, created_by, created_at, updated_by, updated_at
                        FROM tags""",
                rs -> {
                    context.getTags().add(
                            Tag.builder()
                                    .id(rs.getLong("id"))
                                    .name(rs.getString("name"))
                                    .createdBy(rs.getString("created_by"))
                                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                                    .lastModifiedBy(rs.getString("updated_by"))
                                    .lastModifiedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                                    .build()
                    );
                });
    }

}
