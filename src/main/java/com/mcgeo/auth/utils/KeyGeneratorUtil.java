package com.mcgeo.auth.utils;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.apache.commons.codec.binary.Base64;

public class KeyGeneratorUtil {

    public static void main(String[] args) {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // AES-256
            SecretKey secretKey = keyGen.generateKey();
            String encodedKey = Base64.encodeBase64String(secretKey.getEncoded());
            System.out.println("Generated Secret Key: " + encodedKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
