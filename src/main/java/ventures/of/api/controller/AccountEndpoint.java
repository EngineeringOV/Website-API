package ventures.of.api.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ventures.of.api.common.jpa.model.acore.Account;
import ventures.of.api.common.jpa.model.acore.Character;
import ventures.of.api.common.jpa.model.custom.AccountReset;
import ventures.of.api.common.jpa.repositories.acore.AccountRepository;
import ventures.of.api.common.jpa.repositories.acore.CharacterRepository;
import ventures.of.api.common.jpa.repositories.custom.AccountResetRequestRepository;
import ventures.of.api.common.service.captcha.CaptchaService;
import ventures.of.api.common.service.smtp.MailService;
import ventures.of.api.common.utils.CryptographyUtils;
import ventures.of.api.common.utils.StringUtils;
import ventures.of.api.model.WowCryptoInfo;
import ventures.of.api.model.api.requests.ConfirmResetAccountRequest;
import ventures.of.api.model.api.requests.CreateAccountRequest;
import ventures.of.api.model.api.requests.ResetAccountRequest;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

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
    @Value("${api.customization.website.resetPasswordUrl}")
    private String resetUrl;

    public AccountEndpoint(@Autowired AccountRepository accountRepository,
                           @Autowired CharacterRepository characterRepository,
                           @Autowired AccountResetRequestRepository accountResetRequestRepository,
                           @Autowired CaptchaService captchaService,
                           @Autowired MailService mailService) {
        this.accountRepository = accountRepository;
        this.characterRepository = characterRepository;
        this.accountResetRequestRepository = accountResetRequestRepository;
        this.captchaService = captchaService;
        this.mailService = mailService;
    }

    //fixme add blacklist for account creation
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
        if (!isEmpty(recruiterName)) {
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

    @GetMapping(value = "/authenticated/gold")
    @ResponseBody
    public String getAccountGold(Authentication authentication,
                                 Principal principal) {
        Set<Character> characterList = accountRepository.findByUsername(authentication.getName()).getCharacters();
        return String.valueOf(characterList.stream().mapToLong(Character::getMoney).sum());
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
        mailService.sendEmailCustomerSupport(requestData.getEmail(),
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

    //fixme not optimal, can still result in email spam
    public boolean stopEmailSpam(String ip) {
        return accountResetRequestRepository.countByIpAddressAndCreatedAtAfter(ip, LocalDateTime.now().minusDays(3)) > 3;
    }

    private static ResponseEntity<String> verifyCreationData(String field, int maxLength, String fieldName, ResponseEntity<String> current, String... mustContain) {
        if (current != null) {
            return current;
        } else if (isEmpty(field)) {
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
