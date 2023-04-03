package nu.fgv.register.migration.reader;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.model.Tag;
import nu.fgv.register.migration.model.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserReader extends AbstractReader implements Reader {

    protected UserReader(@Qualifier("sourceJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void read(final MigrationContext context) {
        jdbcTemplate.query("""
                        SELECT id, username, created_by, created_at, updated_by, updated_at
                        FROM users""",
                rs -> {
                    context.getUsers().add(
                            User.builder()
                                    .id(rs.getLong("id"))
                                    .uid(rs.getString("username"))
                                    .createdBy(rs.getString("created_by"))
                                    .createdAt(rs.getDate("created_at"))
                                    .lastModifiedBy(rs.getString("updated_by"))
                                    .lastModifiedAt(rs.getDate("updated_at"))
                                    .build()
                    );
                });
    }

}
