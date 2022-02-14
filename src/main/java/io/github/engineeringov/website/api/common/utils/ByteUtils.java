package io.github.engineeringov.website.api.common.utils;

import lombok.SneakyThrows;

import java.security.MessageDigest;

public class ByteUtils {
    private ByteUtils() {
    }

    @SneakyThrows
    public static byte[] asSha1(byte[] bytes) {
        return MessageDigest.getInstance("SHA1").digest(bytes);
    }

    public static byte[] reverseEndianess(byte[] input) {
        for (int i = 0, j = input.length - 1; i < j; i++, j--) {
            byte b = input[i];
            input[i] = input[j];
            input[j] = b;
        }
        return input;
    }
}
