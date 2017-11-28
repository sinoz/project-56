package services;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Singleton;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@Singleton
public final class SecurityService {

    private static final String PRIVATE_KEY = "72a8d4de91b660ae3c8bbd5f689772eb14948c6e84a95addd149cf2759b3d160";

    public static String createSalt() {
        return hash(UUID.randomUUID().toString());
    }

    public static String encodePassword(String password, String salt) {
        return encrypt(salt(password, salt), PRIVATE_KEY);
    }

    public static String secure(String text) {
        return encrypt(text, PRIVATE_KEY);
    }

    private static String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String salt(String text, String salt) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update((text + salt).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (messageDigest == null)
            return null;
        return hash((new BigInteger(messageDigest.digest())).toString(16));
    }

    private static SecretKeySpec secretKey;
    private static byte[] key;

    private static void setKey(String myKey) {
        MessageDigest sha;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String encrypt(String strToEncrypt, String k) {
        try {
            setKey(k);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            secretKey = null;
            key = null;
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        secretKey = null;
        key = null;
        return null;
    }

    private static String decrypt(String strToDecrypt, String k) {
        try {
            setKey(k);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            secretKey = null;
            key = null;
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        secretKey = null;
        key = null;
        return null;
    }
}