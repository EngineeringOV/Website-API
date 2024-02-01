package ventures.of.api.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import ventures.of.api.common.jpa.repositories.acore.AccountRepository;
import ventures.of.api.common.jpa.repositories.acore.CharacterRepository;
import ventures.of.api.common.utils.CryptographyUtils;
import ventures.of.api.model.WowCryptoInfo;
import ventures.of.api.common.jpa.model.acore.Account;
import ventures.of.api.common.jpa.model.acore.Character;

import java.security.NoSuchAlgorithmException;

@Component
public class SpringEventListeners {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    CharacterRepository characterRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void createTestAccount(ApplicationReadyEvent event) {
        if (event.getApplicationContext().getEnvironment().acceptsProfiles(Profiles.of("dev"))) {

        String username = "TEST";
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            WowCryptoInfo wowCryptoInfo = CryptographyUtils.calculateVerifierAndSalt(username, ("TEST"));
            Account newAccount = new Account(username, wowCryptoInfo, null);
            accountRepository.save(newAccount);
        }
        Character character1 = new Character(1, "test1");
        character1.setMoney(256);
        characterRepository.save(character1);
        Character character2 = new Character(1, "test2");
        character2.setMoney(256);
        characterRepository.save(character2);

        }
    }

}
