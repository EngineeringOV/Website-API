package ventures.of.api.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
        public String a() {
        String remoteAddress =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest()
                        .getRemoteAddr();
            return remoteAddress;
        }

}
