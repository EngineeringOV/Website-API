package io.github.engineeringov.website.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.engineeringov.website.api.common.jpa.model.acore.Account;
import io.github.engineeringov.website.api.common.jpa.model.custom.AccountReset;
import io.github.engineeringov.website.api.common.jpa.model.custom.StoreAccountTokens;
import io.github.engineeringov.website.api.common.jpa.repositories.acore.AccountRepository;
import io.github.engineeringov.website.api.common.jpa.repositories.acore.CharacterRepository;
import io.github.engineeringov.website.api.common.jpa.repositories.custom.StoreAccountTokensRepository;
import io.github.engineeringov.website.api.common.service.captcha.CaptchaService;
import io.github.engineeringov.website.api.common.service.smtp.EMailService;
import io.github.engineeringov.website.api.common.utils.CryptographyUtils;
import io.github.engineeringov.website.api.common.utils.StringUtils;
import io.github.engineeringov.website.api.model.WowCryptoInfo;
import io.github.engineeringov.website.api.model.api.requests.ConfirmResetAccountRequest;
import io.github.engineeringov.website.api.model.api.requests.CreateAccountRequest;
import io.github.engineeringov.website.api.model.api.requests.ResetAccountRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.github.engineeringov.website.api.common.jpa.repositories.custom.AccountResetRequestRepository;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Log4j2
@RestController
@RequestMapping("/rest/account")
public class AccountEndpoint {

    private final AccountRepository accountRepository;
    private final StoreAccountTokensRepository storeAccountTokensRepository;
    private final CharacterRepository characterRepository;
    private final AccountResetRequestRepository accountResetRequestRepository;
    private final CaptchaService captchaService;
    private final io.github.engineeringov.website.api.common.service.smtp.EMailService EMailService;
    @Value("${api.customization.website.resetPasswordUrl}")
    private String resetUrl;
    @Autowired
    private ObjectMapper objectMapper;

    public AccountEndpoint(@Autowired AccountRepository accountRepository,
                           @Autowired StoreAccountTokensRepository storeAccountTokensRepository,
                           @Autowired CharacterRepository characterRepository,
                           @Autowired AccountResetRequestRepository accountResetRequestRepository,
                           @Autowired CaptchaService captchaService,
                           @Autowired EMailService EMailService) {
        this.accountRepository = accountRepository;
        this.storeAccountTokensRepository = storeAccountTokensRepository;
        this.characterRepository = characterRepository;
        this.accountResetRequestRepository = accountResetRequestRepository;
        this.captchaService = captchaService;
        this.EMailService = EMailService;
    }

    //fixme add blacklist for account creation
    //fixme add email approval for account creation
    @PostMapping(value = "", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> createAccount(@RequestBody CreateAccountRequest body, HttpServletRequest request) {
        //verify captcha
        if (!captchaService.captchaSuccessful(body.getCaptchaToken(), request.getHeader("X-Real-IP"))) {
            log.info("Account creation error, Email: {}, User: {}, Failed to pass Captcha ", body.getEmail(), body.getUsername());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // Password is recieved as base64 as to not be exposed in a poorly written frontend
        // and handled as a char array to not be logged in plain text if handled incorrectly in backend,
        // meaning that both these features is to avoid a person seeing the password "over the shoulder"
        char[] passwordBase64 = body.getPasswordBase64();
        String email = body.getEmail();
        String username = body.getUsername();
        //verify recruiter
        String recruiterName = body.getRecruiterName();
        int recruiterId = 0;
        if (!StringUtils.isEmpty(recruiterName)) {
            //fixme recruiterID should be based on something arbitrary and not recruiters account name,
            // that way you could give randoms your recruit token and give people recruitment based bonuses
            Account recruiterAccount = accountRepository.findByUsername(recruiterName);
            //Recruiter does not exist
            if (recruiterAccount == null) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Failed: Recruiter doesn't exist");
            }
            recruiterId = recruiterAccount.getId();
        }

        // Verify the fields we need are included and in correct format
        ResponseEntity<String> badDataResponse = verifyCreationData(email, 255, "Email", null, "@", ".");
        badDataResponse = verifyCreationData(username, 15, "Username", badDataResponse);
        badDataResponse = verifyCreationData(new String(passwordBase64), 15, "password", badDataResponse);
        if (badDataResponse != null) {
            return badDataResponse;
        }

        // Verify Username / email not used
        else if (accountRepository.findByUsername(username) != null) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Failed: Username already in use");
        } else if (accountRepository.findByEmail(email) != null) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Failed: Email already in use");
        }
        // Create account
        else {
            WowCryptoInfo wowCryptoInfo = CryptographyUtils.calculateVerifierAndSalt(username, new String(DatatypeConverter.parseBase64Binary(new String(passwordBase64))));
            Account newAccount = new Account(username, wowCryptoInfo, email);
            newAccount.setRecruiter(recruiterId);
            accountRepository.save(newAccount);

            return ResponseEntity.status(HttpStatus.CREATED).body("Success: Account created!");
        }
    }

    @GetMapping(value = "/online")
    @ResponseBody
    public String getOnlineAccounts() {
        return String.valueOf(characterRepository.countByOnlineTrue());
    }

    @GetMapping(value = "/authenticated/account")
    @ResponseBody
    public ResponseEntity<String> getAccount(Authentication authentication,
                                             Principal principal) throws JsonProcessingException {
        Account account = accountRepository.findByUsername(authentication.getName());
        StoreAccountTokens accountTokenList = account.getStoreAccountTokens();
        if(accountTokenList == null) {
            accountTokenList = new StoreAccountTokens(account);
            storeAccountTokensRepository.save(accountTokenList);
            storeAccountTokensRepository.flush();
        }
        return ResponseEntity.status(HttpStatus.OK).body(objectMapper.writeValueAsString(accountTokenList.toString()));
    }

    @PostMapping(value = "/recover", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> forgotPassword(@RequestBody ResetAccountRequest requestData,
                                                 HttpServletRequest request) {
        Account account = accountRepository.findByEmail(requestData.getEmail());
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (account == null) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Failed: Account does not exist");
        } else if (stopEmailSpam(ipAddress)) {
            log.info("Account reset error, Email: {}, Email spam ", requestData.getEmail());
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).build();
        }
        if (!captchaService.captchaSuccessful(requestData.getCaptchaToken(), request.getHeader("X-Real-IP"))) {
            log.info("Account reset error, Email: {}, Failed to pass Captcha ", requestData.getEmail());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        // create database entry for allowing Password change
        AccountReset resetAccountRequest = new AccountReset(account.getEmail(), ipAddress);
        resetAccountRequest.setValidRequest(true);
        accountResetRequestRepository.save(resetAccountRequest);
        EMailService.sendEmailCustomerSupport(requestData.getEmail(),
                "Password reset", StringUtils.buildResetAccountUrl(account.getEmail(), resetAccountRequest.getUuid(), resetUrl));

        // Entry accepted, after this the user should recieve an email with a confirmation
        // link to the "/confirmPasswordChange" endpoint in this controller
        return ResponseEntity.status(HttpStatus.CREATED).body("Success: Email created!");
    }

    @PostMapping(value = "/confirmPasswordChange", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> confirmPasswordChange(@RequestBody ConfirmResetAccountRequest
                                                                requestData, HttpServletRequest request) {
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
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Failed: Missing important data!");
        }
        // change password
        WowCryptoInfo wowCryptoInfo = CryptographyUtils.calculateVerifierAndSalt(account.getUsername(), new String(DatatypeConverter.parseBase64Binary(new String(requestData.getPasswordBase64()))));
        account.setSalt(wowCryptoInfo.getSalt());
        account.setVerifier(wowCryptoInfo.getVerifier());
        accountRepository.save(account);

        return ResponseEntity.status(HttpStatus.OK).body("Success: Password reset!");
    }

    public boolean stopEmailSpam(String ip) {
        return accountResetRequestRepository.countByIpAddressAndCreatedAtAfter(ip, LocalDateTime.now().minusDays(7)) > 2;
    }

    private static ResponseEntity<String> verifyCreationData(String field, int maxLength, String fieldName, ResponseEntity<String> current, String... mustContain) {
        if (current != null) {
            return current;
        } else if (StringUtils.isEmpty(field)) {
            return verifyCreationDataFailed("Failed: \"" + fieldName + "\" is missing");
        } else if (maxLength > 0 && field.length() > maxLength) {
            return verifyCreationDataFailed("Failed: \"" + fieldName + "\" is too long (>" + maxLength + ")");
        } else if (Arrays.stream(mustContain).allMatch(field::contains)) {
            return verifyCreationDataFailed("Failed: \"" + fieldName + "\" is malformed");
        }

        return null;
    }

    private static ResponseEntity<String> verifyCreationDataFailed(String message) {
        log.debug(message);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(message);
    }
}
