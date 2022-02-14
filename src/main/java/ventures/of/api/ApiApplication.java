package ventures.of.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ApiApplication extends SpringBootServletInitializer /*Needed to be used with an external tomcat instance*/{


	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
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
