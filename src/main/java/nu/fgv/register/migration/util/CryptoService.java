package nu.fgv.register.migration.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class CryptoService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretKeySpec key;

    public CryptoService(@Value("${spexregister.crypto.secret-key}") final String secretKey) {
        this.key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
    }

    public String encrypt(final String plainValue) {
        try {
            final byte[] iv = new byte[IV_LENGTH];
            RANDOM.nextBytes(iv);

            final Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH, iv));

            final byte[] encrypted = cipher.doFinal(plainValue.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array());
        } catch (final Exception e) {
            log.error("Unexpected error during encryption", e);
            throw new RuntimeException(e);
        }
    }

}
