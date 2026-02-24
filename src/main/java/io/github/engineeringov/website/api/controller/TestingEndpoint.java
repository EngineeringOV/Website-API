package io.github.engineeringov.website.api.controller;


//Compile with this class uncommented if you want to test REST endpoint locations
// and client headers for your setup

/*
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/youfoundme")
@Log4j2
public class TestingEndpoint {

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
        stringBuilder.append("X-Real-IP = " + request.getHeader("X-Real-IP"));
        stringBuilder.append("\n");
        stringBuilder.append("X-Forwarded-For = " + request.getHeader("X-Forwarded-For"));
        stringBuilder.append("\n");
        stringBuilder.append("X-Forwarded-Host = " + request.getHeader("X-Forwarded-Host"));
        stringBuilder.append("\n");
        stringBuilder.append("X-Forwarded-Server = " + request.getHeader("X-Forwarded-Server"));
        return stringBuilder.toString();
    }
}


 */