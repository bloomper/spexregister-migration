package nu.fgv.register.migration.writer;

import nu.fgv.register.migration.model.User;
import nu.fgv.register.migration.util.PermissionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.ObjectIdentity;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AbstractWriter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    static final String ROLE_ADMIN = "ROLE_spexregister_ADMIN";
    static final String ROLE_EDITOR = "ROLE_spexregister_EDITOR";
    static final String ROLE_USER = "ROLE_spexregister_USER";
    static final GrantedAuthoritySid ROLE_ADMIN_SID = new GrantedAuthoritySid(ROLE_ADMIN);
    static final GrantedAuthoritySid ROLE_EDITOR_SID = new GrantedAuthoritySid(ROLE_EDITOR);
    static final GrantedAuthoritySid ROLE_USER_SID = new GrantedAuthoritySid(ROLE_USER);

    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofSeconds(30);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

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
        return str.replace("\\", "\\\\").replace("'", "''");
    }

    static String mapUser(final List<User> users, final String id) {
        return users.stream()
                .filter(u -> Objects.equals(u.getId(), Long.valueOf(id)))
                .map(User::getUid)
                .map(AbstractWriter::escapeSql)
                .findFirst()
                .orElse("system");
    }

    static String quote(final String str) {
        return "'" + str + "'";
    }

    static String quote(final LocalDate date) {
        return quote(date.format(DATE_FORMAT));
    }

    static String formatTimestamp(final Instant instant) {
        return TIMESTAMP_FORMAT.format(instant);
    }

    static ObjectIdentity toObjectIdentity(final String clazz, final Serializable id) {
        return new ObjectIdentityImpl(clazz, id);
    }

    void writeImage(final String url, final String ownerTable, final String ownerColumn, final Long ownerId) throws IOException {
        final byte[] data = downloadImage(url);
        final String contentType = detectImageContentType(data)
                .orElseThrow(() -> new IOException("Not a PNG, JPEG or GIF image: " + url));
        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO image (data, content_type) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setBytes(1, data);
            preparedStatement.setString(2, contentType);
            return preparedStatement;
        }, keyHolder);

        jdbcTemplate.update("UPDATE %s SET %s = ? WHERE id = ?".formatted(ownerTable, ownerColumn), Objects.requireNonNull(keyHolder.getKey()).longValue(), ownerId);
    }

    private static byte[] downloadImage(final String url) throws IOException {
        try {
            final HttpResponse<byte[]> response = HTTP_CLIENT.send(
                    HttpRequest.newBuilder(URI.create(url)).timeout(DOWNLOAD_TIMEOUT).GET().build(),
                    HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new IOException("HTTP %d when downloading image: %s".formatted(response.statusCode(), url));
            }
            return response.body();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted when downloading image: " + url, e);
        }
    }

    static Optional<String> detectImageContentType(final byte[] data) {
        if (startsWith(data, 0xFF, 0xD8, 0xFF)) {
            return Optional.of("image/jpeg");
        }
        if (startsWith(data, 0x89, 'P', 'N', 'G')) {
            return Optional.of("image/png");
        }
        if (startsWith(data, 'G', 'I', 'F', '8')) {
            return Optional.of("image/gif");
        }
        return Optional.empty();
    }

    private static boolean startsWith(final byte[] data, final int... prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if ((data[i] & 0xFF) != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
