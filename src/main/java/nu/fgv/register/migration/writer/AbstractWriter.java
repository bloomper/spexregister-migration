package nu.fgv.register.migration.writer;

import nu.fgv.register.migration.model.User;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class AbstractWriter {

    protected final JdbcTemplate jdbcTemplate;

    protected AbstractWriter(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    static String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("'", "''");
    }

    static String mapUser(final List<User> users, final String uid) {
        return users.stream()
                .map(User::getUid)
                .filter(u -> u.equals(uid))
                .findFirst()
                .orElse("system");
    }
}
