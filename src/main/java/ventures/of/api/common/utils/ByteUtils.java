package ventures.of.api.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ByteUtils {


    public static byte[] asSha1(byte[] bytes) throws NoSuchAlgorithmException {
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
