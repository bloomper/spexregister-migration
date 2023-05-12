package nu.fgv.register.migration.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

@Service
@Slf4j
public class CryptoService {

    private final byte[] secretKey;
    private final IvParameterSpec iv;
    private final Cipher cipher;

    public CryptoService(
            @Value("${spexregister.crypto.algorithm}") final String algorithm,
            @Value("${spexregister.crypto.secret-key}") final String secretKey,
            @Value("${spexregister.crypto.initialization-vector}") final String iv) {
        this.secretKey = secretKey.getBytes(StandardCharsets.UTF_8);
        this.iv = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
        try {
            cipher = Cipher.getInstance(algorithm);
        } catch (Exception e) {
            log.error("Error during initialization", e);
            throw new RuntimeException(e);
        }
    }

    public String encrypt(final String plainValue) {
        final Key key = new SecretKeySpec(secretKey, "AES");

        try {
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plainValue.getBytes()));
        } catch (Exception e) {
            log.error("Unexpected error during encryption", e);
            throw new RuntimeException(e);
        }
    }

}
