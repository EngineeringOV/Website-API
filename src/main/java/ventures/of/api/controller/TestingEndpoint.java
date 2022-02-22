package ventures.of.api.controller;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/youfoundme")
public class TestingEndpoint {
    @GetMapping(value = "")
    @ResponseBody
    public String b() {

        return "you found me";
    }

        @GetMapping(value = "/2")
        @ResponseBody
        public String a(HttpServletRequest request) {
                String ipAddress=request.getHeader("X-FORWARDED-FOR"); // Due Apache redirection, the real client ip-address will be in this header value

                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("Proxy-Client-IP");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("WL-Proxy-Client-IP");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("HTTP_CLIENT_IP");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
                }
                if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = request.getRemoteAddr();
                }
                if(ipAddress.contains(",")){
                    ipAddress=ipAddress.substring(0, ipAddress.indexOf(","));
                }

                return ipAddress;
            }

}
