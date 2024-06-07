package com.mcgeo.auth.utils;

import org.apache.commons.codec.digest.DigestUtils;

public class EncryptionUtils {
    // Hash a text using SHA-256
    public static String hashSHA256(String text) {
        return DigestUtils.sha256Hex(text);
    }
}
