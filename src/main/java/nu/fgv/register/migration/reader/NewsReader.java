package nu.fgv.register.migration.reader;

import lombok.extern.slf4j.Slf4j;
import nu.fgv.register.migration.MigrationContext;
import nu.fgv.register.migration.model.News;
import nu.fgv.register.migration.model.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NewsReader extends AbstractReader implements Reader {

    protected NewsReader(@Qualifier("sourceJdbcTemplate") final JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    public void read(final MigrationContext context) {
        jdbcTemplate.query("""
                        SELECT id, publication_date, subject, body, created_by, created_at, updated_by, updated_at
                        FROM news""",
                rs -> {
                    context.getNews().add(
                            News.builder()
                                    .id(rs.getLong("id"))
                                    .visibleFrom(rs.getDate("publication_date"))
                                    .subject(rs.getString("subject"))
                                    .text(rs.getString("body"))
                                    .createdBy(rs.getString("created_by"))
                                    .createdAt(rs.getDate("created_at"))
                                    .lastModifiedBy(rs.getString("updated_by"))
                                    .lastModifiedAt(rs.getDate("updated_at"))
                                    .build()
                    );
                });
    }

}
