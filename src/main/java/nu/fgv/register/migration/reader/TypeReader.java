package nu.fgv.register.migration.reader;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.model.Task;
import nu.fgv.register.migration.model.Type;
import nu.fgv.register.migration.model.TypeType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
@Slf4j
public class TypeReader extends AbstractReader implements Reader {

    protected TypeReader(@Qualifier("targetJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void read(final MigrationContext context) {
        jdbcTemplate.query("""
                        SELECT id, type, created_by, created_at, last_modified_by, last_modified_at
                        FROM type""",
                rs -> {
                    context.getTypes().add(
                            Type.builder()
                                    .id(rs.getString("id"))
                                    .type(TypeType.valueOf(rs.getString("type")))
                                    .createdBy(rs.getString("created_by"))
                                    .createdAt(rs.getDate("created_at"))
                                    .lastModifiedBy(rs.getString("last_modified_by"))
                                    .lastModifiedAt(rs.getDate("last_modified_at"))
                                    .build()
                    );
                });
    }

}
