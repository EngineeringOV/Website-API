package ventures.of.api.smtp;

import java.io.File;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class MailSender extends JavaMailSenderImpl {

    @Autowired
    private SmtpConfiguration smtpConfiguration;

    @Autowired
    public MailSender(SmtpConfiguration smtpConfiguration) {
        this.smtpConfiguration = smtpConfiguration;
    }

    @PostConstruct
    private void init() {
        log.debug("Setting MailSender properties from database.");

        setHost(smtpConfiguration.getHost());
        setPort(smtpConfiguration.getPort());
        setUsername(smtpConfiguration.getUser());
        setPassword(smtpConfiguration.getPassword());

        Properties properties = new Properties();
        propertiesPutIfExists(properties, "mail.smtp.auth", smtpConfiguration.getAuth());
        propertiesPutIfExists(properties, "mail.smtp.starttls.enable", smtpConfiguration.getStls());
        propertiesPutIfExists(properties, "mail.smtp.ssl.trust", smtpConfiguration.getSsl());

        log.debug("Mail host: {}", smtpConfiguration.getHost());
        log.debug("Mail port: {}", smtpConfiguration.getPort());
        log.debug("Mail username: {}", smtpConfiguration.getUser());
        log.debug("Mail password: REDACTED");
        log.debug("Mail auth: {}", smtpConfiguration.getAuth());
        log.debug("Mail stls: {}", smtpConfiguration.getStls());
        log.debug("Mail ssl: {}", smtpConfiguration.getSsl());

        setJavaMailProperties(properties);
    }

    /**
     * Sends an asynchronous E-mail containing the plain, HTML text and attachment.
     * </p>
     */
    @Async
    public void send(String subject, String plainText, String htmlText, List<File> attachments, String from, String to) {
        try {
            MimeMessage message = createMimeMessage(subject, plainText, htmlText, attachments, from, to);
            this.send(message);
        } catch (MailException | MessagingException ex) {
            log.warn("E-mail failed!", ex);
        }
    }

    private void propertiesPutIfExists(Properties properties, String key, Boolean value) {
        if (value != null) {
            properties.put(key, value);
        }
    }

    /**
     * Creates a {@code MimeMessage} and copies the template variables.
     * </p>
     */
    private MimeMessage createMimeMessage(String subject, String plainText, String htmlText, List<File> attachments, String from, String to)
            throws MessagingException {
        boolean isMultipart = attachments != null && !attachments.isEmpty();

        MimeMessage message = this.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, isMultipart);
        helper.setTo(to);
        helper.setFrom(from);
        helper.setSubject(subject);

        if (plainText != null && htmlText != null) {
            helper.setText(plainText, htmlText);
            log.info("Message plain text: {}", plainText);
            log.info("Message HTML text: {}", htmlText);
        } else if (htmlText != null) {
            helper.setText(htmlText, true);
            log.info("Message HTML text: {}", htmlText);
        } else if (plainText != null) {
            helper.setText(plainText, false);
            log.info("Message plain text: {}", plainText);
        }

        if (isMultipart) {
            log.debug("Message is multipart: true");
            for (File attachment : attachments) {
                FileSystemResource file = new FileSystemResource(attachment);
                helper.addAttachment(attachment.getName(), file);
                log.info("Adding attachment: {}", attachment.getName());
            }
        }

        return message;
    }
}
