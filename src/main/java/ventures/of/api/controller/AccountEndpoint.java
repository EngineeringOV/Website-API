package ventures.of.api.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.WebDataBinder;
import ventures.of.api.common.jpa.repositories.acore.AccountRepository;
import ventures.of.api.common.jpa.repositories.acore.CharacterRepository;
import ventures.of.api.common.jpa.repositories.custom.AccountResetRequestRepository;
import ventures.of.api.common.utils.AccountUtils;
import ventures.of.api.common.utils.RecruiterNotFoundException;
import ventures.of.api.model.ResponseStatus;
import ventures.of.api.model.api.requests.ConfirmResetAccountRequest;
import ventures.of.api.model.api.requests.CreateAccountRequest;
import ventures.of.api.model.api.requests.ResetAccountRequest;
import ventures.of.api.model.api.responses.CreateAccountResponse;
import ventures.of.api.common.jpa.model.acore.Account;
import ventures.of.api.common.jpa.model.custom.AccountReset;
import ventures.of.api.common.service.captcha.CaptchaService;
import ventures.of.api.common.service.smtp.MailService;
import ventures.of.api.common.utils.CryptographyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ventures.of.api.model.*;
import ventures.of.api.common.utils.StringUtils;
import ventures.of.api.common.service.captcha.FailedCaptchaException;
import ventures.of.api.common.service.captcha.GenericCaptchaException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;

import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
        //verify captcha
        //todo should limit password and account names (not emails) to 15 chars
        try {
            captchaService.throwOnFailedCaptcha(body.getCaptchaToken(), request.getHeader("X-Real-IP"));
        } catch (FailedCaptchaException | GenericCaptchaException e) {
            return new CreateAccountResponse(ResponseStatus.ERROR, "Failed to create account: try again later", "4");
        }

        //verify recruiter
        String recruiterName = body.getRecruiterName();
        int recruiterId;
        try {
            recruiterId = AccountUtils.getRecruiterId(recruiterName, accountRepository);
        } catch (RecruiterNotFoundException e) {
            return new CreateAccountResponse(ResponseStatus.ERROR, "Recruiter doesn't exist", "1");
        }

        return AccountUtils.createAccount(body.getEmail(), body.getUsername().toUpperCase(), body.getPasswordBase64(), recruiterId, accountRepository);
    }

    @GetMapping(value = "/online")
    @ResponseBody
    public String getOnlineAccounts() {
        return String.valueOf(characterRepository.countByOnlineTrue());
    }

    @GetMapping(value = "/authenticated/gold")
    @ResponseBody
    public String getAccountGold(Authentication authentication, Principal principal) {
//        Set<Character> characterList = accountRepository.findByUsername(authentication.getName()).getCharacters();
//        return String.valueOf(characterList.stream().mapToLong(Character::getMoney).sum());
        return String.valueOf(authentication);
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
        try {
            captchaService.throwOnFailedCaptcha(requestData.getCaptchaToken(), request.getHeader("X-Real-IP"));
        } catch (FailedCaptchaException | GenericCaptchaException e) {
            return new CreateAccountResponse(ResponseStatus.ERROR, "FAILURE: try again later", "4");
        }

        // create database entry for allowing Password change
        AccountReset resetAccountRequest = new AccountReset(account.getEmail(), ipAddress);
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
        ArrayList<AccountReset> accountResetRequest =
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
        WowCryptoInfo wowCryptoInfo = CryptographyUtils.calculateVerifierAndSalt(account.getUsername(), new String(DatatypeConverter.parseBase64Binary(new String(requestData.getPasswordBase64()))));
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

        //todo add real blacklisting
        return accountResetRequestRepository.countByIpAddressAndCreatedAtAfter(ip, LocalDateTime.now().minusDays(1)) > 3;
    }
}
