package ventures.of.api.common.security;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import ventures.of.api.common.jpa.repositories.acore.AccountRepository;
import ventures.of.api.common.utils.CryptographyUtils;
import ventures.of.api.common.jpa.model.acore.Account;

@Log4j2
public class SRP6PasswordEncoder implements PasswordEncoder {

    @Autowired
    private AccountRepository accountRepository;

    @SneakyThrows
    @Override
    public String encode(CharSequence usernameColonPassword) {
        return new String (CryptographyUtils.calculateVerifierAndSalt(usernameColonPassword.toString()).getVerifier());
    }

    //todo think this needs fixing?
    @SneakyThrows
    @Override
    public boolean matches(CharSequence nameAndPass, String encodedPassword) {
        String nameAndPassString = nameAndPass.toString();
        int semiColonsNameAndPass = nameAndPassString.length() - nameAndPassString.replace(":", "").length();
        if(semiColonsNameAndPass != 1) {
            log.warn("Semicolon error during password matching");
            return false;
        }
        String username = nameAndPass
                .subSequence(0, nameAndPassString.indexOf(":"))
                .toString();
        Account user = accountRepository.findByUsername(username);
        if(user == null) {
            log.info("user null during password matching");
            return false;
        }
        byte[] userSalt = accountRepository.findByUsername(username).getSalt();
        byte[] inputEncoded = CryptographyUtils
                .calculateVerifierWithSalt(userSalt, nameAndPass.toString())
                .getVerifier();

        return new String(inputEncoded).equals(encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return PasswordEncoder.super.upgradeEncoding(encodedPassword);
    }
}
