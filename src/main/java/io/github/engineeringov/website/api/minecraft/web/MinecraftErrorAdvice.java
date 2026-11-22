package io.github.engineeringov.website.api.minecraft.web;

import io.github.engineeringov.website.api.minecraft.ConditionalOnMinecraftAuth;
import io.github.engineeringov.website.api.minecraft.service.YggdrasilException;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Errors from the Minecraft endpoints, as {error, errorMessage} like Mojang's. */
@Log4j2
@ConditionalOnMinecraftAuth
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = MinecraftErrorAdvice.class)
public class MinecraftErrorAdvice {

    @ExceptionHandler(YggdrasilException.class)
    ResponseEntity<Map<String, String>> yggdrasil(YggdrasilException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getError(), "errorMessage", e.getMessage()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    ResponseEntity<Map<String, String>> badRequest(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", "IllegalArgumentException", "errorMessage", "Malformed request."));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> other(Exception e) {
        log.error("Minecraft endpoint failed", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "InternalServerError", "errorMessage", "Something went wrong, try again."));
    }
}
