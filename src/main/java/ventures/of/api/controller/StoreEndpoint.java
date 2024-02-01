package ventures.of.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ventures.of.api.common.jpa.model.acore.Account;
import ventures.of.api.common.jpa.model.custom.StoreItemCurrent;
import ventures.of.api.common.jpa.repositories.acore.AccountRepository;
import ventures.of.api.common.jpa.repositories.custom.StoreItemCurrentRepository;
import ventures.of.api.model.api.requests.BuyFromStoreRequest;

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
    private final StoreItemCurrentRepository storeItemCurrentRepository;

    public StoreEndpoint(@Autowired AccountRepository accountRepository,
                         @Autowired StoreItemCurrentRepository storeItemCurrentRepository) {
        this.accountRepository = accountRepository;
        this.storeItemCurrentRepository = storeItemCurrentRepository;
    }

    /*
     todo implement real store table etc
     todo credit table
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

        if ("G".equalsIgnoreCase(storeItem.itemBase.priceUnits)) {
            if (!safeToRun) {
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
        File initialFile = new File("src/main/resources/" + fileName);
        return new ObjectMapper().readTree(initialFile).toString();
    }

}
