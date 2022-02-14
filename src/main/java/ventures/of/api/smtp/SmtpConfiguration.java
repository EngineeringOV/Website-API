package ventures.of.api.smtp;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = /*todo*/"aaaa.bbbb.cccc")
public class SmtpConfiguration {
    private String host;
    private Integer port;
    private String user;
    private String password;
    private Boolean auth;
    private Boolean stls;
    private Boolean ssl;
}
