package nu.fgv.register.migration.reader;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.model.Spex;
import nu.fgv.register.migration.model.SpexDetails;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
@Slf4j
public class SpexReader extends AbstractReader implements Reader {

    protected SpexReader(final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void read(final MigrationContext context) {
        jdbcTemplate.query("""
                        SELECT s.id AS spexId, s.year AS spexYear, s.spex_category_id AS spexCategoryId, s.created_by AS spexCreatedBy, s.created_at AS spexCreatedAt, s.updated_by AS spexUpdatedBy, s.updated_at AS spexUpdatedAt,
                        sd.id AS spexDetailsId, sd.title AS spexDetailsTitle, sd.created_by AS spexDetailsCreatedBy, sd.created_at AS spexDetailsCreatedAt, sd.updated_by AS spexDetailsUpdatedBy, sd.updated_at AS spexDetailsUpdatedAt
                        FROM spex AS s
                        JOIN spex_details AS sd ON s.spex_detail_id = sd.id
                        WHERE s.parent_id IS NULL""",
                rs -> {
                    context.getSpex().add(
                            Spex.builder()
                                    .id(rs.getLong("spexId"))
                                    .year(rs.getString("spexYear"))
                                    .details(SpexDetails.builder()
                                            .id(rs.getLong("spexDetailsId"))
                                            .title(rs.getString("spexDetailsTitle"))
                                            .category(context.getSpexCategories().stream()
                                                    .filter(c -> {
                                                        try {
                                                            return c.getId().equals(rs.getLong("spexCategoryId"));
                                                        } catch (SQLException e) {
                                                            log.error("Unexpected SQL exception when finding category for spex", e);
                                                            return false;
                                                        }
                                                    })
                                                    .findFirst()
                                                    .orElse(null)
                                            )
                                            .createdBy(rs.getString("spexDetailsCreatedBy"))
                                            .createdAt(rs.getDate("spexDetailsCreatedAt"))
                                            .lastModifiedBy(rs.getString("spexDetailsUpdatedBy"))
                                            .lastModifiedAt(rs.getDate("spexDetailsUpdatedAt"))
                                            .build()
                                    )
                                    .createdBy(rs.getString("spexCreatedBy"))
                                    .createdAt(rs.getDate("spexCreatedAt"))
                                    .lastModifiedBy(rs.getString("spexUpdatedBy"))
                                    .lastModifiedAt(rs.getDate("spexUpdatedAt"))
                                    .build()
                    );
                });

        // Revivals
        jdbcTemplate.query("""
                        SELECT id, year, parent_id, created_by, created_at, updated_by, updated_at
                        FROM spex
                        WHERE parent_id IS NOT NULL""",
                rs -> {
                    context.getSpex().stream()
                            .filter(c -> {
                                try {
                                    return c.getId().equals(rs.getLong("parent_id"));
                                } catch (SQLException e) {
                                    log.error("Unexpected SQL exception when finding parent for revival", e);
                                    return false;
                                }
                            })
                            .findFirst()
                            .ifPresent(parent -> {
                                try {
                                    context.getSpex().add(
                                            Spex.builder()
                                                    .id(rs.getLong("id"))
                                                    .year(rs.getString("year"))
                                                    .parent(parent)
                                                    .details(parent.getDetails())
                                                    .createdBy(rs.getString("created_by"))
                                                    .createdAt(rs.getDate("created_at"))
                                                    .lastModifiedBy(rs.getString("updated_by"))
                                                    .lastModifiedAt(rs.getDate("updated_at"))
                                                    .build()
                                    );
                                } catch (SQLException e) {
                                    log.error("Unexpected SQL exception when mapping revival to parent", e);
                                }
                            });
                });

    }

}
