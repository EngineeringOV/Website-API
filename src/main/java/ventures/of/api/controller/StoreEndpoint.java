package ventures.of.api.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ventures.of.api.common.jpa.acore.AccountRepository;
import ventures.of.api.common.jpa.acore.CharacterRepository;
import ventures.of.api.common.jpa.custom.AccountResetRequestRepository;
import ventures.of.api.common.service.smtp.MailService;
import ventures.of.api.model.ResponseStatus;
import ventures.of.api.model.api.responses.CreateAccountResponse;
import ventures.of.api.model.api.responses.GetStoreItemsResponse;
import ventures.of.api.common.service.captcha.CaptchaService;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Log4j2
@RestController
@RequestMapping("/rest/store")
public class StoreEndpoint {
    private final AccountRepository accountRepository;
    private final CharacterRepository characterRepository;
    private final AccountResetRequestRepository accountResetRequestRepository;
    private final CaptchaService captchaService;
    private final MailService mailService;

    public StoreEndpoint(@Autowired AccountRepository accountRepository, @Autowired CharacterRepository characterRepository, @Autowired AccountResetRequestRepository accountResetRequestRepository, @Autowired CaptchaService captchaService, @Autowired MailService mailService) {
        this.accountRepository = accountRepository;
        this.characterRepository = characterRepository;
        this.accountResetRequestRepository = accountResetRequestRepository;
        this.captchaService = captchaService;
        this.mailService = mailService;
    }

    //todo set inparameter to request class instead pf response
    @PostMapping(value = "", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public CreateAccountResponse createAccount(@RequestBody GetStoreItemsResponse getStoreItemsResponse, HttpServletRequest request) {
            return new CreateAccountResponse(ResponseStatus.ERROR, "Failed to create account: try again later", "1");
    }

}
