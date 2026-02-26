package io.github.engineeringov.website.api.common.security;

import io.github.engineeringov.website.api.common.jpa.repositories.acore.AccountRepository;
import io.github.engineeringov.website.api.common.utils.CryptographyUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.github.engineeringov.website.api.common.jpa.model.acore.Account;

@Log4j2
public class SRP6PasswordEncoder implements PasswordEncoder {

    @Autowired
    private AccountRepository accountRepository;

    @SneakyThrows
    @Override
    public String encode(CharSequence usernameColonPassword) {
        return new String (CryptographyUtils.calculateVerifierAndSalt(usernameColonPassword.toString()).getVerifier());
    }
//fixme these 2 methods aren't very dry
    @SneakyThrows
    @Override
    public boolean matches(CharSequence nameAndPass, String encodedPassword) {
        String nameAndPassString = nameAndPass.toString();
        int semiColons = nameAndPassString.length() - nameAndPassString.replace(":", "").length();
        if(semiColons != 1) {
            log.warn("The amount of semicolons in the name:password is less or more than exactly 1");
            return false;
        }
        String username = nameAndPass
                .subSequence(0, nameAndPassString.indexOf(":"))
                .toString();
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            log.warn("Account not found for username");
            return false;
        }
        byte[] userSalt = account.getSalt();
        byte[] inputEncoded = CryptographyUtils
                .calculateVerifierWithSalt(userSalt, nameAndPass.toString())
                .getVerifier();

        return new String(inputEncoded).equals(encodedPassword);
    }

    @SneakyThrows
    public boolean efficientMatches(CharSequence nameAndPass, Account account) {
        String nameAndPassString = nameAndPass.toString();
        int semiColons = nameAndPassString.length() - nameAndPassString.replace(":", "").length();
        if(semiColons != 1) {
            log.warn("The amount of semicolons in the name:password is less or more than exactly 1");
            return false;
        }
        byte[] userSalt = account.getSalt();
        byte[] inputEncodedVerifier = CryptographyUtils
                .calculateVerifierWithSalt(userSalt, nameAndPass.toString())
                .getVerifier();

        return new String(inputEncodedVerifier).equals(new String(account.getVerifier()));    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return PasswordEncoder.super.upgradeEncoding(encodedPassword);
    }
}
