package io.github.engineeringov.website.api.minecraft;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Every Minecraft bean carries this: with {@code minecraft.auth.enabled} unset or false
 * none of them exist, no endpoint answers and no table is touched.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ConditionalOnProperty(prefix = "minecraft.auth", name = "enabled", havingValue = "true")
public @interface ConditionalOnMinecraftAuth {
}
