package ventures.of.api.controller;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/youfoundme")
public class TestingEndpoint {
    @GetMapping(value = "")
    @ResponseBody
    public String b() {

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

}
