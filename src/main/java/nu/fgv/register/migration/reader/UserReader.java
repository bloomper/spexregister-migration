package nu.fgv.register.migration.reader;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
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
                        SELECT id, username, crypted_password, password_salt, spexare_id, state, created_by, created_at, updated_by, updated_at
                        FROM users""",
                rs -> {
                    context.getUsers().add(
                            User.builder()
                                    .id(rs.getLong("id"))
                                    .uid(rs.getString("username"))
                                    .password(rs.getString("crypted_password"))
                                    .passwordSalt(rs.getString("password_salt"))
                                    .spexareId(rs.getInt("spexare_id") != 0 ? rs.getInt("spexare_id") : null)
                                    .state(rs.getString("state"))
                                    .createdBy(rs.getString("created_by"))
                                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                                    .lastModifiedBy(rs.getString("updated_by"))
                                    .lastModifiedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                                    .build()
                    );
                });

        // Groups
        context.getUsers().forEach(u ->
                jdbcTemplate.query(String.format("""
                                SELECT name
                                FROM user_groups_users AS ugu
                                LEFT JOIN user_groups AS ug ON ug.id = ugu.user_group_id
                                WHERE user_id = %s""", u.getId()),
                        rs -> {
                            u.getGroups().add(rs.getString("name"));
                        }));
    }

}
