package ventures.of.api.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.WebDataBinder;
import ventures.of.api.common.acore.AccountRepository;
import ventures.of.api.common.acore.CharacterRepository;
import ventures.of.api.common.custom.AccountResetRequestRepository;
import ventures.of.api.model.ResponseStatus;
import ventures.of.api.model.api.requests.ConfirmResetAccountRequest;
import ventures.of.api.model.api.requests.CreateAccountRequest;
import ventures.of.api.model.api.requests.ResetAccountRequest;
import ventures.of.api.model.api.responses.CreateAccountResponse;
import ventures.of.api.model.db.acore.Account;
import ventures.of.api.model.db.custom.AccountResetRequest;
import ventures.of.api.service.CaptchaService;
import ventures.of.api.common.smtp.MailService;
import ventures.of.api.common.utils.CryptographyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ventures.of.api.model.*;
import ventures.of.api.common.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static ventures.of.api.common.utils.StringUtils.isEmpty;

@Log4j2
@RestController
@RequestMapping("/rest/account")
public class AccountEndpoint {

    private final AccountRepository accountRepository;
    private final CharacterRepository characterRepository;
    private final AccountResetRequestRepository accountResetRequestRepository;
    private final CaptchaService captchaService;
    private final MailService mailService;

    public AccountEndpoint(@Autowired AccountRepository accountRepository, @Autowired CharacterRepository characterRepository, @Autowired AccountResetRequestRepository accountResetRequestRepository, @Autowired CaptchaService captchaService, @Autowired MailService mailService) {
        this.accountRepository = accountRepository;
        this.characterRepository = characterRepository;
        this.accountResetRequestRepository = accountResetRequestRepository;
        this.captchaService = captchaService;
        this.mailService = mailService;
    }

    @PostMapping(value = "", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public CreateAccountResponse createAccount(@RequestBody CreateAccountRequest body, HttpServletRequest request) {
        try {
            if (!captchaService.verifyCaptcha(body.getCaptchaToken(), request.getHeader("X-Real-IP"))) {
                //Captcha failed but maybe don't tell the client that
                log.debug("Failed to pass Captcha");
                return new CreateAccountResponse(ResponseStatus.ERROR, "Failed to create account: try again later", "4");
            }
        } catch (Exception e) {
            return new CreateAccountResponse(ResponseStatus.ERROR, "Failed to create account: try again later", "5");
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
            if (accountRepository.findByUsername(username) != null) {
                return new CreateAccountResponse(ResponseStatus.ERROR, "Username already in use", "3");
            } else if (accountRepository.findByEmail(email) != null) {
                return new CreateAccountResponse(ResponseStatus.ERROR, "Email already in use", "3");
            } else {
                WowCryptoInfo wowCryptoInfo = CryptographyUtils.calculateVerifier(username, new String(DatatypeConverter.parseBase64Binary(new String(passwordBase64))));
                Account newAccount = new Account(username, wowCryptoInfo.getSalt(), wowCryptoInfo.getVerifier(), email);
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

    @PostMapping(value = "/recover", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public CreateAccountResponse forgotPassword(@RequestBody ResetAccountRequest requestData, HttpServletRequest request) {
        Account account = accountRepository.findByEmail(requestData.getEmail());
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (account == null) {
            return new CreateAccountResponse(ResponseStatus.ERROR, "FAILURE", "1");
        }
        else if (ipBlacklisted(ipAddress)) {
            return new CreateAccountResponse(ResponseStatus.ERROR, "FAILURE", "2");
        }
        else if (!captchaService.verifyCaptcha(requestData.getCaptchaToken(), ipAddress)) {
            //Captcha failed but maybe don't tell the client that
            log.debug("Failed to pass Captcha to recover account");
            return new CreateAccountResponse(ResponseStatus.ERROR, "FAILURE: try again later", "4");
        }

        // create database entry for allowing Password change
        AccountResetRequest resetAccountRequest = new AccountResetRequest(account.getEmail(), ipAddress);
        resetAccountRequest.setValidRequest(true);
        accountResetRequestRepository.save(resetAccountRequest);
        mailService.sendEmail("cs.world@of.ventures", requestData.getEmail(), "Password reset", StringUtils.buildResetAccountUrl(account.getEmail(), resetAccountRequest.getUuid()));

        // wait for user to confirm change by clicking link to /confirmPasswordChange
        return new CreateAccountResponse(ResponseStatus.SUCCESS, "SUCCESS", "0");
    }

    @PostMapping(value = "/confirmPasswordChange", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public CreateAccountResponse confirmPasswordChange(@RequestBody ConfirmResetAccountRequest requestData, HttpServletRequest request) throws NoSuchAlgorithmException {
        Account account = accountRepository.findByEmail(requestData.getEmail());

        // validate
        ArrayList<AccountResetRequest> accountResetRequest =
                accountResetRequestRepository.findByUuidAndEmailAndValidRequestIsTrue(requestData.getUuid(), requestData.getEmail());
        if (!accountResetRequest.isEmpty() && account != null) {
            accountResetRequest.forEach(e -> {
                e.setValidRequest(false);
                accountResetRequestRepository.save(e);
            });
        } else {
            return new CreateAccountResponse(ResponseStatus.ERROR, "FAILURE", "1");
        }

        // change password
        WowCryptoInfo wowCryptoInfo = CryptographyUtils.calculateVerifier(account.getUsername(), new String(DatatypeConverter.parseBase64Binary(new String(requestData.getPasswordBase64()))));
        account.setSalt(wowCryptoInfo.getSalt());
        account.setVerifier(wowCryptoInfo.getVerifier());
        accountRepository.save(account);

        return new CreateAccountResponse(ResponseStatus.SUCCESS, "SUCCESS", "0");
    }

    // You only need to add this method in one of your controllers in order to prevent exploitation.
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // This code protects Spring Core from a "Remote Code Execution" attack (dubbed "Spring4Shell").
        // By applying this mitigation, you prevent the "Class Loader Manipulation" attack vector from firing.
        // For more details, see this post: https://www.lunasec.io/docs/blog/spring-rce-vulnerabilities/
        String[] blackList = {"class.*", "Class.*", "*.class.*", ".*Class.*"};
        binder.setDisallowedFields(blackList);
    }

    public boolean ipBlacklisted(String ip) {
        return accountResetRequestRepository.countByIpAddressAndCreatedAtAfter(ip, LocalDateTime.now().minusDays(1)) > 3;
    }
}
