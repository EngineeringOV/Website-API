package io.github.engineeringov.website.api.common.service.smtp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class EMailService {

    public final String CUSTOMER_SUPPORT;
    private final JavaMailSenderImpl javaMailSender;

    public EMailService(@Autowired JavaMailSenderImpl javaMailSender,
                        @Value("${custom.mail.smtp.customerSupport}") String supportEmail) {
        this.javaMailSender = javaMailSender;
        this.CUSTOMER_SUPPORT = supportEmail;
    }

    @Async
    public void sendEmailCustomerSupport(String to, String subject, String body) {
        sendEmail(CUSTOMER_SUPPORT, to, subject, body);
    }

    @Async
    public void sendEmail(String from, String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);

        javaMailSender.send(msg);
    }

}
