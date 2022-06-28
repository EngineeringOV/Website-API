package ventures.of.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ventures.of.api.smtp.MailSender;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/youfoundme")
public class TestingEndpoint {

    @Autowired
    MailSender mailSender;

    @GetMapping(value = "")
    @ResponseBody
    public String b() {
        mailSender.send("fuck you", "ok im sorry", null, null, "cs.world.of.ventures", "alex.havlund@gmail.com");
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
        stringBuilder.append("X-Real-IP = " + request.getHeader("X-Real-IP"));
        stringBuilder.append("\n");
        stringBuilder.append("X-Forwarded-For = " + request.getHeader("X-Forwarded-For"));
        return stringBuilder.toString();
    }

}
