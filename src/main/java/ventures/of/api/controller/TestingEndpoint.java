package ventures.of.api.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ventures.of.api.smtp.MailSender;
import ventures.of.api.smtp.SmtpConfiguration;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/youfoundme")
@Log4j2
public class TestingEndpoint {

    @Autowired
    private SmtpConfiguration smtpConfiguration;

    @Autowired
    MailSender mailSender;

    @GetMapping(value = "")
    @ResponseBody
    public String b() {
        log.info("before mail");
        mailSender.send("fuck you", "ok im sorry", null, null, "cs.world@of.ventures", "alex.havlund@gmail.com");
        log.info("after mail");
        return "you found mee";
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
        stringBuilder.append("\n");
        stringBuilder.append("smtpConfiguration = " + smtpConfiguration.getHost());
        System.out.println("request.getRemoteAddr() = " + request.getRemoteAddr());
        return stringBuilder.toString();
    }

}
