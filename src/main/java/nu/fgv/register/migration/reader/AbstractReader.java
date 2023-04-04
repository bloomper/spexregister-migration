package nu.fgv.register.migration.reader;

import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AbstractReader {

    protected final JdbcTemplate jdbcTemplate;

    protected AbstractReader(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    static String encodeUrl(final String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8);
    }

}
