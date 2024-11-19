package nu.fgv.register.migration.writer;

import nu.fgv.register.migration.model.User;
import nu.fgv.register.migration.util.PermissionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class AbstractWriter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static final String ROLE_ADMIN = "ROLE_spexregister_ADMIN";
    static final String ROLE_EDITOR = "ROLE_spexregister_EDITOR";
    static final String ROLE_USER = "ROLE_spexregister_USER";
    static final GrantedAuthoritySid ROLE_ADMIN_SID = new GrantedAuthoritySid(ROLE_ADMIN);
    static final GrantedAuthoritySid ROLE_EDITOR_SID = new GrantedAuthoritySid(ROLE_EDITOR);
    static final GrantedAuthoritySid ROLE_USER_SID = new GrantedAuthoritySid(ROLE_USER);

    final JdbcTemplate jdbcTemplate;
    final PermissionService permissionService;

    protected AbstractWriter(final JdbcTemplate jdbcTemplate, final PermissionService permissionService) {
        this.jdbcTemplate = jdbcTemplate;
        this.permissionService = permissionService;
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

    static ObjectIdentity toObjectIdentity(final String clazz, final Serializable id) {
        return new ObjectIdentityImpl(clazz, id);
    }
}
