package io.github.engineeringov.website.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.engineeringov.website.api.common.security.SRP6PasswordEncoder;
import io.github.engineeringov.website.api.common.service.smtp.SmtpConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ApiApplication extends SpringBootServletInitializer /*Needed to be used with an external tomcat instance*/{

	@Autowired
	SmtpConfiguration smtpConfiguration;

	@Bean
	SRP6PasswordEncoder srp6Passwordencoder() { return new SRP6PasswordEncoder();}
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
	@Bean
	ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public JavaMailSenderImpl javaMailSenderImpl() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setUsername(smtpConfiguration.getUsername());
		mailSender.setPassword(smtpConfiguration.getPassword());
		mailSender.setHost(smtpConfiguration.getHost());
		mailSender.setDefaultEncoding("UTF-8");

		return mailSender;
	}

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
