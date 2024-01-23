package ventures.of.api.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ventures.of.api.common.jpa.repositories.acore.AccountRepository;
import ventures.of.api.common.jpa.repositories.acore.CharacterRepository;
import ventures.of.api.common.service.smtp.MailService;
import ventures.of.api.common.utils.CryptographyUtils;
import ventures.of.api.model.WowCryptoInfo;
import ventures.of.api.common.jpa.model.acore.Account;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;

@RestController
@RequestMapping("/youfoundme")
@Log4j2
public class TestingEndpoint {

    @Autowired
    MailService mailService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    CharacterRepository characterRepository;

    @PostMapping(value = "/dev/1")
    @ResponseBody
    public String b() {
//        log.info("before mail");
//        mailService.sendEmail(mailService.CUSTOMER_SUPPORT, "alex.havlund@gmail.com", "Shalom", "<h3>Hello World!</h3>");
//        log.info("after mail");
        return "you found mee";
    }


    @GetMapping(value = "/c")
    @ResponseBody
    public String c(HttpServletRequest request) throws NoSuchAlgorithmException {
        Account account = accountRepository.findByUsername("EINHARJAR2");
        if (account == null) {
            String username = "EINHARJAR2";
            WowCryptoInfo wowCryptoInfo = CryptographyUtils.calculateVerifierAndSalt(username, new String(("shalom")));
            Account newAccount = new Account(username, wowCryptoInfo, null);
            accountRepository.save(newAccount);
        }
        return accountRepository.findByUsername("EINHARJAR2").toString();
    }
    @GetMapping(value = "/d")
    @ResponseBody
    public String d(HttpServletRequest request)  {
        return accountRepository.findByUsername("EINHARJAR").getCharacters().toString();
    }

    @GetMapping(value = "/authenticated/gold")
    @ResponseBody
    public String getAccountGold(Authentication authentication, Principal principal) {
        return ";)!";
    }

    @GetMapping(value = "/2")
    @ResponseBody
    public String a(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Proxy-Client-IP = " + request.getHeader("Proxy-Client-IP"));
        stringBuilder.append("\n");
        stringBuilder.append("WL-Proxy-Client-IP = " + request.getHeader("WL-Proxy-Client-IP"));
        stringBuilder.append("\n");
        stringBuilder.append("X-FORWARDED-FOR = " + request.getHeader("X-FORWARDED-FOR"));
        stringBuilder.append("\n");
        stringBuilder.append("HTTP_CLIENT_IP = " + request.getHeader("HTTP_CLIENT_IP"));
        stringBuilder.append("\n");
        stringBuilder.append("HTTP_X_FORWARDED_FOR = " + request.getHeader("HTTP_X_FORWARDED_FOR"));
        stringBuilder.append("\n");
        stringBuilder.append("request.getRemoteAddr() = " + request.getRemoteAddr());
        stringBuilder.append("\n");
        stringBuilder.append("HTTP_X_FORWARDED_FOR = " + request.getHeader("HTTP_X_FORWARDED_FOR"));
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }
    @GetMapping(value = "/3")
    @ResponseBody
    public String b(HttpServletRequest request) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("X-Forwarded-Host = " + request.getHeader("X-Real-IP"));
        stringBuilder.append("\n");
        stringBuilder.append("X-Forwarded-Server = " + request.getHeader("X-Forwarded-For"));
        stringBuilder.append("<br/>");
        return stringBuilder.toString();
    }

}
