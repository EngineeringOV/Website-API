package ventures.of.api.utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class ByteUtils {

    @SuppressWarnings("java:S3012")
    public static byte[] unboxArrayOfBytes(Byte[] bytes) {
        byte[] retArr = new byte[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            retArr[i] = bytes[i];
        }
        return retArr;
    }

    public static String byteArrayToStringOfValue(byte[] bytes) {
        StringBuilder retVal = new StringBuilder();
        for (Byte b : bytes) {
            retVal.append(unsignedToBytes(b)).append(" ");
        }
        return retVal.toString();
    }

    public static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    public static byte[] asSha1(byte[] bytes) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA1").digest(bytes);
    }

    public static String asBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] concat(byte[] a, byte[] b) {
        byte[] result = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static byte[] reverseEndianes(byte[] input) {
        byte[] retArray = input;
        for (int i = 0, j = retArray.length - 1; i < j; i++, j--) {
            byte b = retArray[i];
            retArray[i] = retArray[j];
            retArray[j] = b;
        }
        return retArray;
    }

    public static String byteArrayToStringOfValue(Byte[] bytes) {
        StringBuilder retVal = new StringBuilder();
        for (Byte b : bytes) {
            retVal.append(unsignedToBytes(b)).append(" ");
        }
        return retVal.toString();
    }

    public static String intArrayToStringOfValue(int[] bytes) {
        StringBuilder retVal = new StringBuilder();
        for (int i : bytes) {
            retVal.append(i).append(" ");
        }
        return retVal.toString();
    }

    public static int unsignedArrayToBytesArray(byte b) {
        return b & 0xFF;
    }

    public static byte[] intToByteArray(int input) {
        return ByteBuffer.allocate(4).putInt(input).array();
    }

    public static int byteArrayToIntLE(final byte[] b, final int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value |= ((int) b[i + offset] & 0x000000FF) << (i * 8);
        }
        return value;
    }

    public String byteArrayToBinaryString(byte[] input) {
        return new String(input, StandardCharsets.UTF_8);
    }


}
