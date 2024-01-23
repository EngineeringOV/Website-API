package ventures.of.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import uk.oczadly.karl.jnano.rpc.RpcQueryNode;
import uk.oczadly.karl.jnano.rpc.util.RpcServiceProviders;
import ventures.of.api.common.jpa.model.custom.StoreItemCurrent;
import ventures.of.api.common.jpa.repositories.acore.AccountRepository;
import ventures.of.api.common.jpa.repositories.acore.CharacterRepository;
import ventures.of.api.common.jpa.repositories.custom.AccountResetRequestRepository;
import ventures.of.api.common.jpa.repositories.custom.StoreItemCurrentRepository;
import ventures.of.api.common.service.captcha.CaptchaService;
import ventures.of.api.common.service.smtp.MailService;
import ventures.of.api.model.api.requests.BuyFromStoreRequest;
import ventures.of.api.common.jpa.model.acore.Account;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Log4j2
@RestController
@RequestMapping("/rest/store")
public class StoreEndpoint {
    private final AccountRepository accountRepository;
    private final CharacterRepository characterRepository;
    private final StoreItemCurrentRepository storeItemCurrentRepository;
    private final AccountResetRequestRepository accountResetRequestRepository;
    private final CaptchaService captchaService;
    private final MailService mailService;

    public StoreEndpoint(@Autowired AccountRepository accountRepository, @Autowired CharacterRepository characterRepository, @Autowired StoreItemCurrentRepository storeItemCurrentRepository, @Autowired AccountResetRequestRepository accountResetRequestRepository, @Autowired CaptchaService captchaService, @Autowired MailService mailService) {
        this.accountRepository = accountRepository;
        this.characterRepository = characterRepository;
        this.storeItemCurrentRepository = storeItemCurrentRepository;
        this.accountResetRequestRepository = accountResetRequestRepository;
        this.captchaService = captchaService;
        this.mailService = mailService;
    }

    /*
    todo to make store work:
     implement mechanism to remove gold
     implement feature to buy items from store that cost gold
     implement real store table etc
     implement frontend
     implement Add account bound XNO wallet
     implement XNO deposit
     implement buying XNO items
     implement XNO withdrawl
     implement way to get XNO to USD rate
     implement USD costing items in store paid by XNO
     */

    @GetMapping(value = "", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getStoreItems() throws IOException {
        return readFromFile("wip-mocks/StoreItems.json");
    }

    @GetMapping(value = "/authenticated/buyItem/{storeId}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String buyItem(Authentication authentication,
                          @RequestBody BuyFromStoreRequest requestData) throws Exception {
        Account account = accountRepository.findByUsername(authentication.getName());
        StoreItemCurrent storeItem = storeItemCurrentRepository.findById(requestData.getItemId()).orElseThrow(Exception::new);
        boolean safeToRun = !account.isOnline() && !account.isLocked();

        if("XNO".equalsIgnoreCase(storeItem.itemBase.priceUnits)) {
            //todo dynamic "check n change" rpc-node if node is down or slow
            RpcQueryNode rpc = RpcServiceProviders.nanex(); // Using nanex.cc public API

        }
        else if("G".equalsIgnoreCase(storeItem.itemBase.priceUnits)) {
            if(!safeToRun) {
                throw new Exception();
            }
        }

    int character = 0; // = request.getCharacter();
//      StoreItem item = request.getItem();
        long cost = storeItem.currentPrice; // = StoreItems.getItem(item).getCost();
        
        return readFromFile("wip-mocks/StoreItems.json");
    }

    @GetMapping(value = "/authenticated/thisAccount", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String thisAccount(Authentication authentication,
                          HttpServletRequest req,
                          HttpServletResponse res) throws Exception {
return "";

    }

    @GetMapping(value = "/authenticated/withdrawl", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String withdrawNano() throws IOException {
        return readFromFile("wip-mocks/StoreItems.json");
    }
    @GetMapping(value = "/test/{storeId}", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public int test(@PathVariable int storeId) {
        return storeId;
    }
/*
    @GetMapping(value = "/authenticated/deposit", produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String depositNano() throws IOException {
        return readFromFile("wip-mocks/StoreItems.json");
    }
*/

    private String readFromFile(String fileName) throws IOException {
        File initialFile = new File("src/main/resources/"+fileName);
        return new ObjectMapper().readTree(initialFile).toString();
    }
    private int gold(int gold){
        return gold*100*100;
    }
    private int silver(int silver){
        return silver*100;
    }
    private int copper(int copper){
        return copper;
    }
}
