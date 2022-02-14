package io.github.engineeringov.website.api.common;

import io.github.engineeringov.website.api.common.jpa.model.acore.Character;
import io.github.engineeringov.website.api.common.jpa.model.acore.*;
import io.github.engineeringov.website.api.common.jpa.model.custom.StorePackageBase;
import io.github.engineeringov.website.api.common.jpa.model.custom.StorePackageItem;
import io.github.engineeringov.website.api.common.jpa.repositories.acore.*;
import io.github.engineeringov.website.api.common.jpa.repositories.custom.StorePackageAvailabilityRepository;
import io.github.engineeringov.website.api.common.utils.CryptographyUtils;
import io.github.engineeringov.website.api.model.WowCryptoInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
@Log4j2
public class SpringEventListeners {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    AccountAccessRepository accountAccessRepository;
    @Autowired
    CharacterRepository characterRepository;

    @Autowired
    StorePackageAvailabilityRepository storePackageAvailabilityRepository;
    @Autowired
    GameMailItemsRepository gameMailItemsRepository;
    @Autowired
    GameMailRepository gameMailRepository;
    @Autowired
    ItemInstanceRepository itemInstanceRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void createTestAccount(ApplicationReadyEvent event) {
        if (event.getApplicationContext().getEnvironment().acceptsProfiles(Profiles.of("dev"))) {
            String username = "TEST";
            Account account = accountRepository.findByUsername(username);
            if (account == null) {
                log.info("Creating test account");
                WowCryptoInfo wowCryptoInfo = CryptographyUtils.calculateVerifierAndSalt(username, ("TEST"));
                Account newAccount = new Account(username, wowCryptoInfo, null);
                accountRepository.save(newAccount);
                AccountAccess newAccountAccess = new AccountAccess();
                newAccountAccess.setGmlevel((short) 1);
                newAccountAccess.setId(newAccount.getId());
                accountAccessRepository.save(newAccountAccess);
                Character character1 = new Character(1, "test1");
                character1.setMoney(256);
                characterRepository.save(character1);
                Character character2 = new Character(1, "test2");
                character2.setMoney(256);
                characterRepository.save(character2);


                log.info("Attempting to send mail");
                StorePackageBase packageBase = new StorePackageBase();
                packageBase.items = List.of(new StorePackageItem("adwadadaadw",null,23720,23720,1,1));
                GameMail gameMail = new GameMail();
                gameMail.setSubject("Store purchase");
                gameMail.setBody("Store purchase");
                gameMail.setStationary((byte)61); //61 = GM (Blizzard)
                gameMail.setMessageType((byte)4);//4 = Gameobject
                gameMail.setReceiver(character2);
                gameMail.setSender(character2);
                gameMail.setHasItems(true);

                gameMailRepository.saveAndFlush(gameMail);
                //  https://www.azerothcore.org/wiki/chrraces
                boolean isAlliance = Stream.of(1,4,8,64,1024)
                        .anyMatch(e -> character2.getRace() == e);

                for(StorePackageItem i : packageBase.items) {
                    ItemInstance itemInstance = new ItemInstance();
                    if(isAlliance) {
                        itemInstance.setItemEntry(i.getItemIdAlliance());
                        itemInstance.setCount(i.getQuantityAlliance());
                    }
                    else {
                        itemInstance.setItemEntry(i.getItemIdHorde());
                        itemInstance.setCount(i.getQuantityHorde());
                    }
                    GameMailItems gameMailItems = new GameMailItems(gameMail, itemInstance.getGuid(), character2 );
                    itemInstanceRepository.saveAndFlush(itemInstance);
                    gameMailItemsRepository.saveAndFlush(gameMailItems);
                }
                log.info("Mail saved");
            }
            }

        }
    }

