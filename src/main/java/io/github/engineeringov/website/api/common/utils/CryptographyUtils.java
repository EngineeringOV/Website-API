package io.github.engineeringov.website.api.common.utils;

import org.dfdeshom.math.GMP;
import io.github.engineeringov.website.api.model.WowCryptoInfo;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class CryptographyUtils {
    private CryptographyUtils() {
    }

    public static WowCryptoInfo calculateVerifierAndSalt(CharSequence usernameAndPassword) {
        return calculateVerifierWithSalt(calculateSalt(), usernameAndPassword.toString().toUpperCase());
    }

    public static WowCryptoInfo calculateVerifierAndSalt(CharSequence username, CharSequence password) {
        return calculateVerifierWithSalt(calculateSalt(), (username + ":" + password).toUpperCase());
    }

    public static WowCryptoInfo calculateVerifierWithSalt(byte[] salt, String usernameColonPass) {
        byte[] hash1 = ByteUtils.asSha1(usernameColonPass.getBytes(StandardCharsets.UTF_8));
        //First set array to salt and then append hash1 to the end of it
        byte[] prehash2 = new byte[salt.length + hash1.length];
        System.arraycopy(salt, 0, prehash2, 0, salt.length);
        System.arraycopy(hash1, 0, prehash2, salt.length, hash1.length);
        // reverse endianness to be true to PHP code
        byte[] hash2 = ByteUtils.reverseEndianess(ByteUtils.asSha1(prehash2));
        byte[] verifierAsBytes = calculateVerifier(hash2);

        return new WowCryptoInfo(salt, verifierAsBytes);
    }

    public static byte[] calculateSalt() {
        // Generate a secure salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        return salt;
    }

    public static byte[] calculateVerifier(byte[] hash2) {

        //Magic numbers are from WOWs cryptographic constants
        GMP nGmp = new GMP();
        nGmp.fromString("894B645E89E1535BBDAD5B8B290650530801B18EBFBF5E8FAB3C82872A3E9BB7", 16);
        GMP gGmp = new GMP(7);

        GMP hash2AsGMP = new GMP();
        hash2AsGMP.fromByteArray(hash2);

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

        return verifierAsBytes;
    }
}