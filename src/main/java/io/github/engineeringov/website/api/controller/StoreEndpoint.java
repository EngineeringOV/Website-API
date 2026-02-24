package io.github.engineeringov.website.api.controller;

import io.github.engineeringov.website.api.common.jpa.model.acore.Account;
import io.github.engineeringov.website.api.common.jpa.model.acore.GameMail;
import io.github.engineeringov.website.api.common.jpa.model.acore.GameMailItems;
import io.github.engineeringov.website.api.common.jpa.model.acore.ItemInstance;
import io.github.engineeringov.website.api.common.jpa.model.custom.StoreAccountTokens;
import io.github.engineeringov.website.api.common.jpa.model.custom.StorePackageAvailability;
import io.github.engineeringov.website.api.common.jpa.model.custom.StorePackageItem;
import io.github.engineeringov.website.api.common.jpa.repositories.acore.AccountRepository;
import io.github.engineeringov.website.api.common.jpa.repositories.acore.GameMailItemsRepository;
import io.github.engineeringov.website.api.common.jpa.repositories.acore.GameMailRepository;
import io.github.engineeringov.website.api.common.jpa.repositories.acore.ItemInstanceRepository;
import io.github.engineeringov.website.api.common.jpa.repositories.custom.StoreAccountTokensRepository;
import io.github.engineeringov.website.api.common.jpa.repositories.custom.StorePackageAvailabilityRepository;
import io.github.engineeringov.website.api.model.api.requests.BuyFromStoreRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.github.engineeringov.website.api.common.jpa.model.acore.Character;
import io.github.engineeringov.website.api.common.jpa.model.custom.StorePackageBase;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Log4j2
@RestController
@RequestMapping("/rest/store")
public class StoreEndpoint {
    private final AccountRepository accountRepository;
    private final StoreAccountTokensRepository storeAccountTokensRepository;
    private final StorePackageAvailabilityRepository storePackageAvailabilityRepository;
    private final GameMailItemsRepository gameMailItemsRepository;
    private final GameMailRepository gameMailRepository;
    private final ItemInstanceRepository itemInstanceRepository;

    public StoreEndpoint(@Autowired AccountRepository accountRepository,
                         @Autowired StoreAccountTokensRepository storeAccountTokensRepository,
                         @Autowired StorePackageAvailabilityRepository storePackageAvailabilityRepository,
                         @Autowired GameMailItemsRepository gameMailItemsRepository,
                         @Autowired GameMailRepository gameMailRepository,
                         @Autowired ItemInstanceRepository itemInstanceRepository) {
        this.accountRepository = accountRepository;
        this.storeAccountTokensRepository = storeAccountTokensRepository;
        this.storePackageAvailabilityRepository = storePackageAvailabilityRepository;
        this.gameMailItemsRepository = gameMailItemsRepository;
        this.gameMailRepository = gameMailRepository;
        this.itemInstanceRepository = itemInstanceRepository;
    }

    @GetMapping(value = "", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ArrayList<StorePackageAvailability>> getStoreItems(Authentication authentication,
                                                                             Principal principal) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(storePackageAvailabilityRepository
                        .findByAvailableAtDate(LocalDateTime.now()));
    }

    @GetMapping(value = "/authenticated/buyItem", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> buyItem(Authentication authentication,
                                          @RequestBody BuyFromStoreRequest requestData) {
        Account account = accountRepository.findByUsername(authentication.getName());
        StorePackageAvailability storePackage = storePackageAvailabilityRepository.findById(requestData.getItemId()).orElseThrow(StoreException::new);
        long currentPrice = storePackage.currentPrice;
        int characterUuid = requestData.getReceivingCharacterUuid();
        StoreAccountTokens credits = account.getStoreAccountTokens();

        if ((storePackage.startsAt.isAfter(LocalDateTime.now()) || storePackage.endsAt.isBefore(LocalDateTime.now()))) {
            throw new StoreException();
        }

        Character character = account.getCharacters().stream().filter(e -> e.getGuid() == (characterUuid)).findFirst().orElseThrow(StoreException::new);
        // fixme there's probably a better way to do this but with this you could theoretically support real cash or crypto or ingame gold etc,
        // could at least be dry
        switch (storePackage.currentPriceUnits.toUpperCase()) {
            case "PREMIUM_CREDITS":
                if (credits.premiumToken >= currentPrice) {
                    credits.premiumToken -= currentPrice;
                    storeAccountTokensRepository.save(credits);
                } else {
                    throw new StoreException();
                }
                break;
            case "FREE_CREDITS":
                if (credits.freeToken >= currentPrice) {
                    credits.freeToken -= currentPrice;
                    storeAccountTokensRepository.save(credits);
                } else {
                    throw new StoreException();
                }
                break;
            case "VOTING_CREDITS":
                if (credits.voteToken >= currentPrice) {
                    credits.voteToken -= currentPrice;
                    storeAccountTokensRepository.save(credits);
                } else {
                    throw new StoreException();
                }
                break;
            case "NONE":
                break;
            default:
                throw new StoreException();
        }

        sendCharacterPackage(character, storePackage.itemBase);

        return ResponseEntity.ok("Bought");
    }

    //fixme fill this out so you can display tokens/credis w/e in the frontend without requesting the whole account
    @GetMapping(value = "/authenticated/userTokens", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String thisAccount(Authentication authentication,
                              HttpServletRequest req,
                              HttpServletResponse res) {

        return "";
    }

    private void sendCharacterPackage(Character character, StorePackageBase packageBase) {
        GameMail gameMail = new GameMail();
        gameMail.setSubject("Store purchase");
        gameMail.setBody("Store purchase");
        gameMail.setStationary((byte)61); //61 = GM (Blizzard)
        gameMail.setMessageType((byte)4);//4 = Gameobject
        gameMail.setReceiver(character);
        gameMail.setSender(character);
        gameMail.setHasItems(true);

        //  https://www.azerothcore.org/wiki/chrraces
        boolean isAlliance = Stream.of(1,4,8,64,1024)
                .anyMatch(e -> character.getRace() == e);

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
            GameMailItems gameMailItems = new GameMailItems(gameMail, itemInstance.getGuid(), character );
            itemInstanceRepository.save(itemInstance);
            gameMailItemsRepository.save(gameMailItems);
        }
        gameMailRepository.save(gameMail);
    }

}
