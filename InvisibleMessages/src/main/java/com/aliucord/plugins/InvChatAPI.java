package com.aliucord.plugins;

import com.aliucord.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class InvChatAPI {
    static Logger logger = new Logger("InvChatAPI");
    static String regex = "[\u200c\u200d\u2062\u2063\u2063]"; //husk
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int AES_KEY_SIZE = 256;

    public static boolean containsInvisibleMessage(String message) {
        return containsAny(message, "\u200c\u2062\u2063\u2063");
    }

    public static boolean containsAny(String string, String searchChars) {
        for (var a : searchChars.toCharArray()) {
            if (string.contains(String.valueOf(a))) return true;
        }
        return false;
    }

    public static String encrypt(String password, String secret, String cover) throws IOException {
        try {
            SecretKey key = getKeyFromPassword(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            byte[] cipherText = cipher.doFinal((secret + "\u200b").getBytes(StandardCharsets.UTF_8));
            String encryptedMessage = Base64.getEncoder().encodeToString(cipherText);
            return ("\u200b" + cover + encryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
        return null; //fail
    }

    public static String decrypt(String message, String password) throws IOException {
        try {
            SecretKey key = getKeyFromPassword(password);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[IV_LENGTH_BYTE];
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
            String encryptedPart = message.substring(message.indexOf("\u200b") + 1);
            byte[] decodedMessage = Base64.getDecoder().decode(encryptedPart);
            byte[] plainText = cipher.doFinal(decodedMessage);
            return new String(plainText, StandardCharsets.UTF_8).replace("\u200b", "");
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }
        return null; //fail
    }

    private static SecretKey getKeyFromPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(password.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(key, 0, AES_KEY_SIZE / 8, "AES");
    }
}
