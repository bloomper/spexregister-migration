package nu.fgv.register.migration.writer;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.model.SpexActivity;
import nu.fgv.register.migration.util.CryptoService;
import nu.fgv.register.migration.util.PermissionService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.net.URL;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.springframework.util.StringUtils.hasText;

@Service
@Slf4j
public class SpexareWriter extends AbstractWriter implements Writer {

    private static final DateTimeFormatter BIRTH_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final CryptoService cryptoService;

    protected SpexareWriter(@Qualifier("targetJdbcTemplate") final JdbcTemplate jdbcTemplate,
                            final PermissionService permissionService,
                            final CryptoService cryptoService) {
        super(jdbcTemplate, permissionService);
        this.cryptoService = cryptoService;
    }

    @Override
    public void clean() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE actor");
        jdbcTemplate.execute("TRUNCATE TABLE task_activity");
        jdbcTemplate.execute("TRUNCATE TABLE spex_activity");
        jdbcTemplate.execute("TRUNCATE TABLE activity");
        jdbcTemplate.execute("TRUNCATE TABLE membership");
        jdbcTemplate.execute("TRUNCATE TABLE address");
        jdbcTemplate.execute("TRUNCATE TABLE consent");
        jdbcTemplate.execute("TRUNCATE TABLE toggle");
        jdbcTemplate.execute("TRUNCATE TABLE tagging");
        jdbcTemplate.execute("TRUNCATE TABLE spexare");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Override
    public void write(final MigrationContext context) {
        context.getSpexare().forEach(t -> {
            jdbcTemplate.execute(String.format("""
                            INSERT INTO spexare
                            (id, first_name, last_name, nick_name, social_security_number, deceased, published, graduation, comment, image_content_type,
                             created_by, created_at, last_modified_by, last_modified_at) values
                            (%s, %s, %s, %s, %s, %s, %s, %s, '%s', '%s', '%s', '%s')""",
                    t.getId(), hasText(t.getFirstName()) ? quote(escapeSql(t.getFirstName())) : null, hasText(t.getLastName()) ? quote(escapeSql(t.getLastName())) : null,
                    hasText(t.getNickName()) ? quote(escapeSql(t.getNickName())) : null, constructSocialSecurityNumber(t.getBirthDate(), t.getSocialSecurityNumber()),
                    t.getDeceased(), t.getPublished(),
                    hasText(t.getGraduation()) ? quote(t.getGraduation()) : null, hasText(t.getComment()) ? quote(escapeSql(t.getComment())) : null,
                    hasText(t.getImageContentType()) ? quote(t.getImageContentType()) : null,
                    mapUser(context.getUsers(), t.getCreatedBy()), t.getCreatedAt(), mapUser(context.getUsers(), t.getLastModifiedBy()), t.getLastModifiedAt()));

            final ObjectIdentity oid = toObjectIdentity("nu.fgv.register.server.spexare.Spexare", t.getId());

            permissionService.grantPermission(oid, BasePermission.ADMINISTRATION, ROLE_ADMIN_SID);
            if (t.getPublished()) {
                permissionService.grantPermission(oid, BasePermission.READ, ROLE_EDITOR_SID, ROLE_USER_SID);
                permissionService.grantPermission(oid, BasePermission.WRITE, ROLE_EDITOR_SID);
            }

            if (hasText(t.getImageUrl())) {
                try (final BufferedInputStream inputStream = new BufferedInputStream(new URL(t.getImageUrl()).openStream())) {
                    jdbcTemplate.update(connection -> {
                        PreparedStatement preparedStatement = connection.prepareStatement(String.format("UPDATE spexare SET image = ? WHERE id = %s", t.getId()));
                        preparedStatement.setBlob(1, inputStream);
                        return preparedStatement;
                    });
                } catch (Exception e) {
                    log.error("Unexpected error when writing image for spexare", e);
                }
            }

            // Membership
            t.getMemberships().forEach(m ->
                    jdbcTemplate.execute(String.format("""
                                    INSERT INTO membership
                                    (id, year, type_id, spexare_id,
                                     created_by, created_at, last_modified_by, last_modified_at) values
                                    (%s, '%s', '%s', %s, '%s', '%s', '%s', '%s')""",
                            m.getId(), m.getYear(), m.getType().getId(), t.getId(),
                            mapUser(context.getUsers(), m.getCreatedBy()), m.getCreatedAt(), mapUser(context.getUsers(), m.getLastModifiedBy()), m.getLastModifiedAt()))
            );

            // Address
            t.getAddresses().forEach(a ->
                    jdbcTemplate.execute(String.format("""
                                    INSERT INTO address
                                    (street_address, postal_code, city, country, phone, phone_mobile, email_address, type_id, spexare_id,
                                     created_by, created_at, last_modified_by, last_modified_at) values
                                    (%s, %s, %s, %s, %s, %s, %s, '%s', %s, '%s', '%s', '%s', '%s')""",
                            hasText(a.getStreetAddress()) ? quote(escapeSql(a.getStreetAddress())) : null, hasText(a.getPostalCode()) ? quote(escapeSql(a.getPostalCode())) : null,
                            hasText(a.getCity()) ? quote(escapeSql(a.getCity())) : null, hasText(a.getCountry()) ? quote(escapeSql(a.getCountry())) : null,
                            hasText(a.getPhone()) ? quote(escapeSql(a.getPhone())) : null, hasText(a.getPhoneMobile()) ? quote(escapeSql(a.getPhoneMobile())) : null,
                            hasText(a.getEmailAddress()) ? quote(escapeSql(a.getEmailAddress())) : null, a.getType().getId(), t.getId(),
                            mapUser(context.getUsers(), a.getCreatedBy()), a.getCreatedAt(), mapUser(context.getUsers(), a.getLastModifiedBy()), a.getLastModifiedAt()))
            );

            // Consent
            t.getConsents().forEach(c ->
                    jdbcTemplate.execute(String.format("""
                                    INSERT INTO consent
                                    (id, value, type_id, spexare_id,
                                     created_by, created_at, last_modified_by, last_modified_at) values
                                    (%s, %s, '%s', %s, '%s', '%s', '%s', '%s')""",
                            c.getId(), c.getValue(), c.getType().getId(), t.getId(),
                            mapUser(context.getUsers(), c.getCreatedBy()), c.getCreatedAt(), mapUser(context.getUsers(), c.getLastModifiedBy()), c.getLastModifiedAt()))
            );

            // Toggle
            t.getToggles().forEach(g ->
                    jdbcTemplate.execute(String.format("""
                                    INSERT INTO toggle
                                    (id, value, type_id, spexare_id,
                                     created_by, created_at, last_modified_by, last_modified_at) values
                                    (%s, %s, '%s', %s, '%s', '%s', '%s', '%s')""",
                            g.getId(), g.getValue(), g.getType().getId(), t.getId(),
                            mapUser(context.getUsers(), g.getCreatedBy()), g.getCreatedAt(), mapUser(context.getUsers(), g.getLastModifiedBy()), g.getLastModifiedAt()))
            );

            // Tagging
            t.getTaggings().forEach(g ->
                    jdbcTemplate.execute(String.format("""
                                    INSERT INTO tagging
                                    (tag_id, spexare_id) values
                                    (%s, %s)""",
                            g.getTag().getId(), t.getId()))
            );

            // Activity
            t.getActivities().forEach(a -> {
                jdbcTemplate.execute(String.format("""
                                INSERT INTO activity
                                (id, spexare_id,
                                 created_by, created_at, last_modified_by, last_modified_at) values
                                (%s, %s, '%s', '%s', '%s', '%s')""",
                        a.getId(), a.getSpexare().getId(),
                        mapUser(context.getUsers(), a.getCreatedBy()), a.getCreatedAt(), mapUser(context.getUsers(), a.getLastModifiedBy()), a.getLastModifiedAt()));

                // Spex activity
                if (a.getSpexActivity() != null) {
                    final SpexActivity sa = a.getSpexActivity();

                    jdbcTemplate.execute(String.format("""
                                    INSERT INTO spex_activity
                                    (id, activity_id, spex_id,
                                     created_by, created_at, last_modified_by, last_modified_at) values
                                    (%s, %s, %s, '%s', '%s', '%s', '%s')""",
                            sa.getId(), a.getId(), sa.getSpex().getId(),
                            mapUser(context.getUsers(), sa.getCreatedBy()), sa.getCreatedAt(), mapUser(context.getUsers(), sa.getLastModifiedBy()), sa.getLastModifiedAt()));
                }

                // Task activity
                a.getTaskActivities().forEach(ta -> {
                    jdbcTemplate.execute(String.format("""
                                    INSERT INTO task_activity
                                    (id, activity_id, task_id,
                                     created_by, created_at, last_modified_by, last_modified_at) values
                                    (%s, %s, %s, '%s', '%s', '%s', '%s')""",
                            ta.getId(), a.getId(), ta.getTask().getId(),
                            mapUser(context.getUsers(), ta.getCreatedBy()), ta.getCreatedAt(), mapUser(context.getUsers(), ta.getLastModifiedBy()), ta.getLastModifiedAt()));

                    // Actors
                    ta.getActors().forEach(ac ->
                            jdbcTemplate.execute(String.format("""
                                            INSERT INTO actor
                                            (id, role, vocal_id, task_activity_id,
                                             created_by, created_at, last_modified_by, last_modified_at) values
                                            (%s, %s, '%s', %s, '%s', '%s', '%s', '%s')""",
                                    ac.getId(), hasText(ac.getRole()) ? quote(escapeSql(ac.getRole())) : null, ac.getVocal().getId(), ta.getId(),
                                    mapUser(context.getUsers(), ac.getCreatedBy()), ac.getCreatedAt(), mapUser(context.getUsers(), ac.getLastModifiedBy()), ac.getLastModifiedAt())));
                });
            });
        });

        // Partner
        context.getSpexare().stream().filter(t -> t.getPartner() != null).forEach(t ->
                jdbcTemplate.execute(String.format("""
                                UPDATE spexare SET
                                partner_id = %s
                                WHERE id = %s""",
                        t.getPartner().getId(), t.getId())));

    }

    private String constructSocialSecurityNumber(final LocalDate birthDate, final String socialSecurityNumber) {
        if (birthDate != null) {
            return quote(cryptoService.encrypt(String.format("%s%s", birthDate.format(BIRTH_DATE_FORMATTER), hasText(socialSecurityNumber) ? "-" + socialSecurityNumber : "")));
        } else {
            return null;
        }
    }
}
