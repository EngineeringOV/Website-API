package ventures.of.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.client.RestTemplate;
import ventures.of.api.smtp.SmtpConfiguration;

import java.util.Properties;

@SpringBootApplication
public class ApiApplication extends SpringBootServletInitializer /*Needed to be used with an external tomcat instance*/{

	@Autowired
	SmtpConfiguration smtpConfiguration;

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public JavaMailSenderImpl javaMailSenderImpl() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		mailSender.setUsername(smtpConfiguration.getUser());
		mailSender.setPassword(smtpConfiguration.getPassword());
		mailSender.setHost(smtpConfiguration.getHost());
		mailSender.setDefaultEncoding("UTF-8");
		Properties pros = new Properties();
		pros.put("mail.smtp.auth", smtpConfiguration.getAuth());
		pros.put("mail.smtp.timeout", 25000);
		pros.put("mail.smtp.port", smtpConfiguration.getPort());
		pros.put("mail.smtp.socketFactory.port", smtpConfiguration.getPort());
		pros.put("mail.smtp.socketFactory.fallback", false);
			pros.put("mail.smtp.socketFactory.port = 465", 465);
			pros.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		mailSender.setJavaMailProperties(pros);
		return mailSender;
	}

	@SuppressWarnings("java:S3011")
	public static void main(String[] args) {
	/*
	1
		System.setProperty("java.library.path", "./lib/GMP-java/");
		MethodHandles.Lookup cl = MethodHandles.privateLookupIn(ClassLoader.class, MethodHandles.lookup());
		VarHandle sys_paths = cl.findStaticVarHandle(ClassLoader.class, "sys_paths", String[].class);
		sys_paths.set((Object) null);

2
		final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
		sysPathsField.setAccessible(true);
		sysPathsField.set(null, null);
*/
		SpringApplication.run(ApiApplication.class, args);

	}

}
