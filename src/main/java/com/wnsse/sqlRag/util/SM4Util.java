package com.wnsse.sqlRag.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.Security;

@Component
public class SM4Util {

    private static final String ALGORITHM = "SM4";
    private static final String TRANSFORMATION = "SM4/CBC/PKCS5Padding";

    @Value("${Sm4.key}")
    private String keyBase64;

    private String ivBase64;

    public SM4Util() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(keyBase64.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            if (ivBase64 == null) {
                ivBase64 = generateDefaultIv();
            }
            IvParameterSpec ivSpec = new IvParameterSpec(ivBase64.getBytes(StandardCharsets.UTF_8));

            Cipher cipher = Cipher.getInstance(TRANSFORMATION, BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("SM4 encryption failed", e);
        }
    }

    public String decrypt(String cipherText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(keyBase64.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            if (ivBase64 == null) {
                ivBase64 = generateDefaultIv();
            }
            IvParameterSpec ivSpec = new IvParameterSpec(ivBase64.getBytes(StandardCharsets.UTF_8));

            Cipher cipher = Cipher.getInstance(TRANSFORMATION, BouncyCastleProvider.PROVIDER_NAME);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("SM4 decryption failed", e);
        }
    }

    private String generateDefaultIv() {
        byte[] iv = new byte[16];
        for (int i = 0; i < 16; i++) {
            iv[i] = 0;
        }
        return Base64.getEncoder().encodeToString(iv);
    }

    public String getKeyBase64() {
        return keyBase64;
    }

    public String getIvBase64() {
        return ivBase64;
    }
}
