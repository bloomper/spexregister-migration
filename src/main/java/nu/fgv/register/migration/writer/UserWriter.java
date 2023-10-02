package nu.fgv.register.migration.writer;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserWriter extends AbstractWriter implements Writer {

    protected UserWriter(@Qualifier("targetJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void clean() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE user");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Override
    public void write(final MigrationContext context) {
        context.getUsers().forEach(t ->
                jdbcTemplate.execute(String.format("""
                                INSERT INTO user
                                (id, uid, state, created_by, created_at, last_modified_by, last_modified_at)
                                values
                                (%s, '%s', '%s', '%s', '%s', '%s', '%s')""",
                        t.getId(), escapeSql(t.getUid()), mapState(t.getState()),
                        mapUser(context.getUsers(), t.getCreatedBy()), t.getCreatedAt(), mapUser(context.getUsers(), t.getLastModifiedBy()), t.getLastModifiedAt()))
        );

        // Groups
        context.getUsers().stream().filter(t -> !t.getGroups().isEmpty()).forEach(t ->
                t.getGroups().forEach(g ->
                        jdbcTemplate.execute(String.format("""
                                        INSERT INTO user_authority
                                        (user_id, authority_id, created_by, created_at)
                                        values
                                        (%s, %s, '%s', '%s')""",
                                t.getId(), mapGroup(g),
                                mapUser(context.getUsers(), t.getCreatedBy()), t.getCreatedAt()))
                ));


        // Spexare
        context.getUsers().stream().filter(t -> t.getSpexareId() != null).forEach(t ->
                jdbcTemplate.execute(String.format("""
                                UPDATE user
                                SET spexare_id = %s
                                WHERE id = %s""",
                        t.getSpexareId(), t.getId()))
        );
    }

    private Integer mapGroup(final String group) {
        switch (group) {
            case "Administrators" -> {
                return 1;
            }
            case "Users" -> {
                return 2;
            }
            default -> throw new IllegalArgumentException("Unknown group");
        }
    }

    private String mapState(final String state) {
        switch (state) {
            case "active" -> {
                return "ACTIVE";
            }
            case "inactive" -> {
                return "INACTIVE";
            }
            case "pending" -> {
                return "PENDING";
            }
            default -> throw new IllegalArgumentException("Unknown state");
        }
    }

}
