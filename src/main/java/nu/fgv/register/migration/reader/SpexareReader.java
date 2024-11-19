package nu.fgv.register.migration.reader;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.model.Activity;
import nu.fgv.register.migration.model.Actor;
import nu.fgv.register.migration.model.Address;
import nu.fgv.register.migration.model.Consent;
import nu.fgv.register.migration.model.Membership;
import nu.fgv.register.migration.model.SpexActivity;
import nu.fgv.register.migration.model.Spexare;
import nu.fgv.register.migration.model.Tagging;
import nu.fgv.register.migration.model.TaskActivity;
import nu.fgv.register.migration.model.Toggle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;

@Service
@Slf4j
public class SpexareReader extends AbstractReader implements Reader {

    private static final String ENCRYPTION_KEY = "A8AD3BC66E66FC6C255312D70FFA547E1CE8FB8A4382BE961DFFBED0DD45B340";
    private final Invocable invocable;

    protected SpexareReader(@Qualifier("sourceJdbcTemplate") final JdbcTemplate jdbcTemplate) throws ScriptException {
        super(jdbcTemplate);
        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName("ruby");
        engine.eval(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/decrypt.rb"))));
        invocable = (Invocable) engine;
    }

    public void read(final MigrationContext context) {
        jdbcTemplate.query("""
                        SELECT id, last_name, first_name, nick_name, birth_date, encrypted_social_security_number,
                        deceased, publish_approval, graduation, comment, picture_file_name, picture_content_type,
                        created_by, created_at, updated_by, updated_at
                        FROM spexare""",
                rs -> {
                    context.getSpexare().add(
                            Spexare.builder()
                                    .id(rs.getLong("id"))
                                    .lastName(rs.getString("last_name"))
                                    .firstName(rs.getString("first_name"))
                                    .nickName(rs.getString("nick_name"))
                                    .birthDate(rs.getDate("birth_date") != null ? rs.getDate("birth_date").toLocalDate() : null)
                                    .socialSecurityNumber(decrypt(rs.getString("encrypted_social_security_number")).orElse(null))
                                    .deceased(rs.getBoolean("deceased"))
                                    .published(rs.getBoolean("publish_approval"))
                                    .graduation(rs.getString("graduation"))
                                    .comment(rs.getString("comment"))
                                    .imageUrl(hasText(rs.getString("picture_file_name")) ? String.format("https://register.fgv.nu/system/pictures/%s/original/%s", rs.getLong("id"), encodeUrl(rs.getString("picture_file_name"))) : null)
                                    .imageContentType(rs.getString("picture_content_type"))
                                    .addresses(new ArrayList<>())
                                    .memberships(new ArrayList<>())
                                    .consents(new ArrayList<>())
                                    .toggles(new ArrayList<>())
                                    .activities(new ArrayList<>())
                                    .taggings(new ArrayList<>())
                                    .createdBy(rs.getString("created_by"))
                                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                                    .lastModifiedBy(rs.getString("updated_by"))
                                    .lastModifiedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                                    .build()
                    );
                });

        // Addresses
        context.getSpexare().forEach(s ->
                jdbcTemplate.query(String.format("""
                                SELECT street_address, postal_code, postal_address, country, phone_home, phone_work, phone_mobile, phone_other, email_address
                                FROM spexare
                                WHERE id = %s""", s.getId()),
                        rs -> {
                            if (hasText(rs.getString("street_address")) || hasText(rs.getString("postal_address")) ||
                                    hasText(rs.getString("postal_code")) || hasText(rs.getString("phone_home")) ||
                                    hasText(rs.getString("phone_mobile")) || hasText(rs.getString("email_address"))) {
                                s.getAddresses().add(
                                        Address.builder()
                                                .streetAddress(rs.getString("street_address"))
                                                .postalCode(rs.getString("postal_code"))
                                                .city(rs.getString("postal_address"))
                                                .country(rs.getString("country"))
                                                .phone(rs.getString("phone_home"))
                                                .phoneMobile(rs.getString("phone_mobile"))
                                                .emailAddress(rs.getString("email_address"))
                                                .type(context.getTypes().stream().filter(t -> t.getId().equals("HOME")).findFirst().orElseThrow(() -> new RuntimeException("Could not find type HOME")))
                                                .spexare(s)
                                                .createdBy(s.getCreatedBy())
                                                .createdAt(s.getCreatedAt())
                                                .lastModifiedBy(s.getLastModifiedBy())
                                                .lastModifiedAt(s.getLastModifiedAt())
                                                .build()
                                );
                            }
                            if (hasText(rs.getString("phone_work"))) {
                                s.getAddresses().add(
                                        Address.builder()
                                                .phone(rs.getString("phone_work"))
                                                .type(context.getTypes().stream().filter(t -> t.getId().equals("WORK")).findFirst().orElseThrow(() -> new RuntimeException("Could not find type WORK")))
                                                .spexare(s)
                                                .createdBy(s.getCreatedBy())
                                                .createdAt(s.getCreatedAt())
                                                .lastModifiedBy(s.getLastModifiedBy())
                                                .lastModifiedAt(s.getLastModifiedAt())
                                                .build()
                                );
                            }
                            if (hasText(rs.getString("phone_other"))) {
                                s.getAddresses().add(
                                        Address.builder()
                                                .phone(rs.getString("phone_other"))
                                                .type(context.getTypes().stream().filter(t -> t.getId().equals("OTHER")).findFirst().orElseThrow(() -> new RuntimeException("Could not find type OTHER")))
                                                .spexare(s)
                                                .createdBy(s.getCreatedBy())
                                                .createdAt(s.getCreatedAt())
                                                .lastModifiedBy(s.getLastModifiedBy())
                                                .lastModifiedAt(s.getLastModifiedAt())
                                                .build()
                                );
                            }
                        }
                )
        );

        // Consents
        context.getSpexare().forEach(s ->
                jdbcTemplate.query(String.format("""
                                SELECT want_circulars, want_email_circulars
                                FROM spexare
                                WHERE id = %s""", s.getId()),
                        rs -> {
                            s.getConsents().add(
                                    Consent.builder()
                                            .value(rs.getBoolean("want_circulars"))
                                            .type(context.getTypes().stream().filter(t -> t.getId().equals("CIRCULARS")).findFirst().orElseThrow(() -> new RuntimeException("Could not find type CIRCULARS")))
                                            .spexare(s)
                                            .createdBy(s.getCreatedBy())
                                            .createdAt(s.getCreatedAt())
                                            .lastModifiedBy(s.getLastModifiedBy())
                                            .lastModifiedAt(s.getLastModifiedAt())
                                            .build()
                            );
                            s.getConsents().add(
                                    Consent.builder()
                                            .value(rs.getBoolean("want_email_circulars"))
                                            .type(context.getTypes().stream().filter(t -> t.getId().equals("EMAIL_CIRCULARS")).findFirst().orElseThrow(() -> new RuntimeException("Could not find type EMAIL_CIRCULARS")))
                                            .spexare(s)
                                            .createdBy(s.getCreatedBy())
                                            .createdAt(s.getCreatedAt())
                                            .lastModifiedBy(s.getLastModifiedBy())
                                            .lastModifiedAt(s.getLastModifiedAt())
                                            .build()
                            );
                        }
                )
        );

        // Toggles
        context.getSpexare().forEach(s ->
                jdbcTemplate.query(String.format("""
                                SELECT chalmers_student
                                FROM spexare
                                WHERE id = %s""", s.getId()),
                        rs -> {
                            s.getToggles().add(
                                    Toggle.builder()
                                            .value(rs.getBoolean("chalmers_student"))
                                            .type(context.getTypes().stream().filter(t -> t.getId().equals("CHALMERS_STUDENT")).findFirst().orElseThrow(() -> new RuntimeException("Could not find type CHALMERS_STUDENT")))
                                            .spexare(s)
                                            .createdBy(s.getCreatedBy())
                                            .createdAt(s.getCreatedAt())
                                            .lastModifiedBy(s.getLastModifiedBy())
                                            .lastModifiedAt(s.getLastModifiedAt())
                                            .build()
                            );
                        }
                )
        );

        // Memberships
        context.getSpexare().forEach(s ->
                jdbcTemplate.query(String.format("""
                                SELECT id, year, kind_id, created_by, created_at, updated_by, updated_at
                                FROM memberships
                                WHERE spexare_id = %s""", s.getId()),
                        rs -> {
                            s.getMemberships().add(
                                    Membership.builder()
                                            .id(rs.getLong("id"))
                                            .year(rs.getString("year"))
                                            .type(context.getTypes().stream().filter(t -> {
                                                try {
                                                    return rs.getInt("kind_id") == 1 ? t.getId().equals("FGV") : t.getId().equals("CING");
                                                } catch (SQLException e) {
                                                    log.error("Unexpected SQL exception when finding type for membership", e);
                                                    return false;
                                                }
                                            }).findFirst().orElseThrow(() -> new RuntimeException("Could not find type FGV/CING")))
                                            .spexare(s)
                                            .createdBy(rs.getString("created_by"))
                                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                                            .lastModifiedBy(rs.getString("updated_by"))
                                            .lastModifiedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                                            .build()
                            );
                        }
                )
        );

        // Tags
        context.getSpexare().forEach(s ->
                jdbcTemplate.query(String.format("""
                                SELECT id, tag_id, created_by, created_at, updated_by, updated_at
                                FROM taggings
                                WHERE spexare_id = %s""", s.getId()),
                        rs -> {
                            s.getTaggings().add(
                                    Tagging.builder()
                                            .id(rs.getLong("id"))
                                            .tag(context.getTags().stream().filter(t -> {
                                                try {
                                                    return t.getId() == rs.getInt("tag_id");
                                                } catch (SQLException e) {
                                                    log.error("Unexpected SQL exception when finding tag for tagging", e);
                                                    return false;
                                                }
                                            }).findFirst().orElseThrow(() -> new RuntimeException("Could not find tag")))
                                            .spexare(s)
                                            .createdBy(rs.getString("created_by"))
                                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                                            .lastModifiedBy(rs.getString("updated_by"))
                                            .lastModifiedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                                            .build()
                            );
                        }
                )
        );

        // Partner
        context.getSpexare().forEach(s ->
                jdbcTemplate.query(String.format("""
                                SELECT spouse_id
                                FROM relationships
                                WHERE spexare_id = %s""", s.getId()),
                        rs -> {
                            s.setPartner(context.getSpexare().stream().filter(p -> {
                                try {
                                    return p.getId() == rs.getInt("spouse_id");
                                } catch (SQLException e) {
                                    log.error("Unexpected SQL exception when finding partner for spexare", e);
                                    return false;
                                }
                            }).findFirst().orElseThrow(() -> new RuntimeException("Could not find partner")));
                        }
                )
        );

        // Activity
        context.getSpexare().forEach(s -> {
            jdbcTemplate.query(String.format("""
                            SELECT id, created_by, created_at, updated_by, updated_at
                            FROM activities
                            WHERE spexare_id = %s""", s.getId()),
                    rs -> {
                        s.getActivities().add(
                                Activity.builder()
                                        .id(rs.getLong("id"))
                                        .spexare(s)
                                        .taskActivities(new ArrayList<>())
                                        .createdBy(rs.getString("created_by"))
                                        .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                                        .lastModifiedBy(rs.getString("updated_by"))
                                        .lastModifiedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                                        .build()
                        );
                    }
            );

            s.getActivities().forEach(a -> {
                // Spex activity
                jdbcTemplate.query(String.format("""
                                SELECT id, spex_id, created_by, created_at, updated_by, updated_at
                                FROM spex_activities
                                WHERE activity_id = %s""", a.getId()),
                        rs -> {
                            a.setSpexActivity(
                                    SpexActivity.builder()
                                            .id(rs.getLong("id"))
                                            .spex(context.getSpex().stream().filter(t -> {
                                                try {
                                                    return t.getId() == rs.getInt("spex_id");
                                                } catch (SQLException e) {
                                                    log.error("Unexpected SQL exception when finding spex for spex activity", e);
                                                    return false;
                                                }
                                            }).findFirst().orElseThrow(() -> new RuntimeException("Could not find spex")))
                                            .activity(a)
                                            .createdBy(rs.getString("created_by"))
                                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                                            .lastModifiedBy(rs.getString("updated_by"))
                                            .lastModifiedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                                            .build()
                            );
                        }
                );

                // Task activity
                jdbcTemplate.query(String.format("""
                                SELECT id, function_id, created_by, created_at, updated_by, updated_at
                                FROM function_activities
                                WHERE activity_id = %s""", a.getId()),
                        rs -> {
                            a.getTaskActivities().add(
                                    TaskActivity.builder()
                                            .id(rs.getLong("id"))
                                            .task(context.getTasks().stream().filter(t -> {
                                                try {
                                                    return t.getId() == rs.getInt("function_id");
                                                } catch (SQLException e) {
                                                    log.error("Unexpected SQL exception when finding task for task activity", e);
                                                    return false;
                                                }
                                            }).findFirst().orElseThrow(() -> new RuntimeException("Could not find task")))
                                            .activity(a)
                                            .actors(new ArrayList<>())
                                            .createdBy(rs.getString("created_by"))
                                            .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                                            .lastModifiedBy(rs.getString("updated_by"))
                                            .lastModifiedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                                            .build()
                            );
                        }
                );

                // Actor
                a.getTaskActivities().forEach(ta ->
                        jdbcTemplate.query(String.format("""
                                        SELECT id, role, vocal_id, created_by, created_at, updated_by, updated_at
                                        FROM actors
                                        WHERE function_activity_id = %s""", ta.getId()),
                                rs2 -> {
                                    final String vocalId =
                                            switch (rs2.getInt("vocal_id")) {
                                                case 1 -> "UNKNOWN";
                                                case 2 -> "B1";
                                                case 3 -> "B2";
                                                case 4 -> "T1";
                                                case 5 -> "T2";
                                                case 6 -> "S1";
                                                case 7 -> "S2";
                                                case 8 -> "A1";
                                                case 9 -> "A2";
                                                default -> "UNKNOWN";
                                            };
                                    ta.getActors().add(
                                            Actor.builder()
                                                    .id(rs2.getLong("id"))
                                                    .role(rs2.getString("role"))
                                                    .vocal(context.getTypes().stream().filter(t -> t.getId().equals(vocalId)).findFirst().orElseThrow(() -> new RuntimeException("Could not find vocal")))
                                                    .taskActivity(ta)
                                                    .createdBy(rs2.getString("created_by"))
                                                    .createdAt(rs2.getTimestamp("created_at").toLocalDateTime())
                                                    .lastModifiedBy(rs2.getString("updated_by"))
                                                    .lastModifiedAt(rs2.getTimestamp("updated_at").toLocalDateTime())
                                                    .build()
                                    );
                                }
                        )
                );
            });
        });
    }

    public Optional<String> decrypt(final String encryptedValue) {
        if (hasText(encryptedValue)) {
            try {
                return Optional.ofNullable((String) invocable.invokeFunction("decrypt", Base64.getDecoder().decode(encryptedValue.replaceAll("\\n", "")), ENCRYPTION_KEY));
            } catch (Exception e) {
                log.error("Error while invoking decrypt", e);
            }
        }
        return Optional.empty();
    }

}
