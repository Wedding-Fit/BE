package com.weddingfit.global.util;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RsaEncryptUtil {
    private static final String CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private static final String KEY_ALGORITHM = "RSA";

    /**
     * @param plainText 암호화할 평문 (예: 인터넷뱅킹 로그인 비밀번호)
     * @param pemPublicKey CODEF 공개키 (PEM 전체 문자열)
     */
    public static String encryptRSA(String plainText, String pemPublicKey) {
        try {
            // -----BEGIN / END----- 제거
            String base64Key = pemPublicKey
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", ""); // 공백/줄바꿈 제거

            byte[] bytePublicKey = Base64.getDecoder().decode(base64Key);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(bytePublicKey));

            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] bytePlain = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(bytePlain);
        } catch (Exception e) {
            throw new RuntimeException("RSA 암호화 실패", e);
        }
    }
}
