package nu.fgv.register.migration.writer;

import nu.fgv.register.migration.model.User;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class AbstractWriter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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

    static String mapUser(final List<User> users, final String id) {
        return users.stream()
                .filter(u -> Objects.equals(u.getId(), Long.valueOf(id)))
                .map(User::getUid)
                .findFirst()
                .orElse("system");
    }

    static String quote(final String str) {
        return "'" + str + "'";
    }

    static String quote(final LocalDate date) {
        return quote(date.format(DATE_FORMAT));
    }
}
