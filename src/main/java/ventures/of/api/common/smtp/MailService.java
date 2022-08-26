package ventures.of.api.common.smtp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MailService {

    public final String CUSTOMER_SUPPORT;

    private final JavaMailSenderImpl javaMailSender;

    public MailService(@Autowired JavaMailSenderImpl javaMailSender,
                       @Value("${custom.mail.smtp.customerSupport}") String supportEmail){
        this.javaMailSender = javaMailSender;
        this.CUSTOMER_SUPPORT = supportEmail;
    }

    @Async
    public void sendEmail(String from, String to, String subject, String body) {
        sendEmail(from, new String[]{to}, null, null, subject, body, null, null);
    }

    @Async
    public void sendEmail(String from, String[] to, String subject, String body) {
        sendEmail(from, to, null, null, subject, body, null, null);
    }

    @Async
    public void sendEmail(String from, String[] to, String[] cc, String[] bcc, String subject, String body, Date date, String replyTo) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        msg.setCc(cc);
        msg.setBcc(bcc);
        msg.setSentDate(date);
        msg.setReplyTo(replyTo);

        javaMailSender.send(msg);
    }

}
