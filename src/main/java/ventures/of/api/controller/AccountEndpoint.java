package ventures.of.api.controller;

import lombok.extern.log4j.Log4j2;
import ventures.of.api.common.AccountRepository;
import ventures.of.api.common.CharacterRepository;
import ventures.of.api.model.ResponseStatus;
import ventures.of.api.model.api.requests.CreateAccountRequest;
import ventures.of.api.model.api.responses.CreateAccountResponse;
import ventures.of.api.model.db.Account;
import ventures.of.api.service.CaptchaService;
import ventures.of.api.utils.CryptographyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ventures.of.api.model.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static ventures.of.api.utils.StringUtils.isEmpty;

@Log4j2
@RestController
@RequestMapping("/rest/account")
public class AccountEndpoint {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private CaptchaService captchaService;


    @PostMapping(value = "", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public CreateAccountResponse createAccount(@RequestBody CreateAccountRequest body, HttpServletRequest request) {

        try {
            if (!captchaService.verifyCaptcha(body.getCaptchaToken(), request.getRemoteAddr())) {
                //Captcha failed but maybe don't tell the client that
                log.debug("Failed to pass Captcha");
                return new CreateAccountResponse(ResponseStatus.ERROR, "Failed to create account: try again later", "4");
            }
        } catch (Exception e) {
            return new CreateAccountResponse(ResponseStatus.ERROR, "Failed to create account: try again later", "4");
        }

        String email = body.getEmail();
        String username = body.getUsername().toUpperCase();
        char[] passwordBase64 = body.getPasswordBase64();
        String recruiterName = body.getRecruiterName();
        int recruiterId = 0;
        if (!isEmpty(recruiterName)) {
            Account recruiterAccount = accountRepository.findByUsername(recruiterName);
            if (recruiterAccount == null) {
                return new CreateAccountResponse(ResponseStatus.ERROR, "Recruiter doesn't exist", "1");
            }
            recruiterId = recruiterAccount.getId();
        }

        if (isEmpty(email) && email.contains("@") && email.contains(".")) {
            log.info("Missing parameter: \"email\" required for account creation");
            return new CreateAccountResponse(ResponseStatus.ERROR, "Missing parameter: \"email\" required for account creation ", "1");
        } else if (isEmpty(username)) {
            log.info("Missing parameter: \"username\" required for account creation ");
            return new CreateAccountResponse(ResponseStatus.ERROR, "Missing parameter: \"username\" required for account creation ", "1");
        } else if (isEmpty(new String(passwordBase64))) {
            log.info("Missing parameter: \"password\" required for account creation");
            return new CreateAccountResponse(ResponseStatus.ERROR, "Missing parameter: \"password\" required for account creation ", "1");
        }

        try {
            WowCryptoInfo wowCryptoInfo = CryptographyUtils.calculateVerifier(username, new String(DatatypeConverter.parseBase64Binary(new String(passwordBase64))));
            if (accountRepository.findByUsername(username) != null) {
                return new CreateAccountResponse(ResponseStatus.ERROR, "Username already in use", "3");
            } else if (accountRepository.findByEmail(email) != null) {
                return new CreateAccountResponse(ResponseStatus.ERROR, "Email already in use", "3");
            } else {
                Account newAccount = new Account(username, wowCryptoInfo.salt, wowCryptoInfo.verifier, email);
                newAccount.setRecruiter(recruiterId);
                accountRepository.save(newAccount);
                return new CreateAccountResponse(ResponseStatus.SUCCESS, "Account created", "0");
            }
        } catch (Exception e) {
            log.info("Failed to create account, Exception below:", e);
            //todo Add more specific execption handling and error codes in the future
            return new CreateAccountResponse(ResponseStatus.ERROR, "Failed to create account: try again later", "1");
        }
    }

    @GetMapping(value = "/online")
    @ResponseBody
    public String getOnlineAccounts() {
        return String.valueOf(characterRepository.countByOnlineTrue());
    }

    @GetMapping(value = "/recover/account", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String forgotAccount() {
        return "";
    }

    @GetMapping(value = "/recover/password", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public String forgotPassword() {
        return "";
    }

}
