package ventures.of.api.common.utils;

import org.dfdeshom.math.GMP;
import ventures.of.api.model.WowCryptoInfo;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static ventures.of.api.common.utils.ByteUtils.asSha1;
import static ventures.of.api.common.utils.ByteUtils.reverseEndianess;

public class CryptographyUtils {


    public static WowCryptoInfo calculateVerifier(String username, String password) throws NoSuchAlgorithmException {

        //Magic numbers are from WOWs cryptographic constants
        GMP gGmp = new GMP(7);
        GMP nGmp = new GMP();
        nGmp.fromString("894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7", 16);

        // Generate a secure salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);

        String prehash1 = (username + ":" + password).toUpperCase();
        byte[] hash1 = asSha1(prehash1.getBytes(StandardCharsets.UTF_8));

        //First set array to salt and then append hash1 to the end of it
        byte[] prehash2 = new byte[salt.length + hash1.length];
        System.arraycopy(salt, 0, prehash2, 0, salt.length);
        System.arraycopy(hash1, 0, prehash2, salt.length, hash1.length);
        // reverse endianness to be true to PHP code
        byte[] hash2 = reverseEndianess(asSha1(prehash2));
        GMP hash2AsGMP = new GMP();
        hash2AsGMP.fromSignedMagnitude(hash2, false);

        //powm on gGmp
        GMP verifier = new GMP();
        gGmp.modPow(hash2AsGMP, nGmp, verifier);

        byte[] verifierAsBytes = new byte[32];
        verifier.toByteArray(verifierAsBytes);

        //reverse verifier
        for (int i = 0; i < verifierAsBytes.length / 2; i++) {
            byte temp = verifierAsBytes[i];
            verifierAsBytes[i] = verifierAsBytes[verifierAsBytes.length - 1 - i];
            verifierAsBytes[verifierAsBytes.length - 1 - i] = temp;
        }

        return new WowCryptoInfo(salt, verifierAsBytes);
    }
    public static boolean validatePassword(String username, String password, WowCryptoInfo dbCryptoInfo) throws NoSuchAlgorithmException {
        WowCryptoInfo wowCryptoInfo = CryptographyUtils.calculateVerifier(username, password);
        return wowCryptoInfo.equals(dbCryptoInfo);
    }

    public static String reverseHash(Byte[] salt, Byte[] verifier) throws NoSuchAlgorithmException {
        return "";
    }
}