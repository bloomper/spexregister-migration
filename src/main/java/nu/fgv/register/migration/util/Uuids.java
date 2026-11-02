package nu.fgv.register.migration.util;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Creates the external ids that entities are known by outside the system.
 *
 * @author Anders Jacobsson
 * @since 2.0
 */
public final class Uuids {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Uuids() {
    }

    /**
     * @return a new time-ordered UUID (version 7, RFC 9562)
     */
    public static UUID v7() {
        final long millis = System.currentTimeMillis();
        final long mostSignificantBits = (millis << 16) | 0x7000L | (RANDOM.nextLong() & 0x0FFFL);
        final long leastSignificantBits = (RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;

        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}
