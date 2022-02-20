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
            return request.getRemoteAddr();
        }

}
