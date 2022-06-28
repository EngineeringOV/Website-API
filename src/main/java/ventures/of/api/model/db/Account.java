package ventures.of.api.model.db;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;

import java.time.LocalDate;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Entity
@Table(name = "account", schema="acore_auth")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "salt")
    @Type(type = "org.hibernate.type.BinaryType")
    private byte[] salt;

    @Column(name = "verifier")
    @Type(type = "org.hibernate.type.BinaryType")
    private byte[] verifier;

    @Column(name = "session_key")
    @Type(type = "org.hibernate.type.BinaryType")
    private byte[] sessionKey = null;

    @Column(name = "totp_secret")
    @Type(type = "org.hibernate.type.BinaryType")
    private byte[] totpSecret = null;;

    @Column(name = "email")
    private String email = "";

    @Column(name = "reg_mail")
    private String regMail = "";

    @Column(name = "joindate")
    private LocalDate joinDate = LocalDate.now();

    @Column(name = "last_ip")
    private String lastIp = "127.0.0.1";

    @Column(name = "last_attempt_ip")
    private String lastAttemptIp = "127.0.0.1";

    @Column(name = "failed_logins")
    private int failedLogins = 0;

    @Column(name = "locked", columnDefinition = "TINYINT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean locked = false;

    @Column(name = "lock_country")
    private String lockCountry = "00";

    @Column(name = "last_login")
    private LocalDate lastLogin = null;

    @Column(name = "online", columnDefinition = "INT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean online = false;

    @Column(name = "expansion")
    private short expansion = 0;

    @Column(name = "mutetime")
    private int muteTime = 0;

    @Column(name = "mutereason")
    private String muteReason = "";

    @Column(name = "muteby")
    private String muteBy = "";

    @Column(name = "locale")
    private short locale = 0;

    @Column(name = "os")
    private String os = "";

    @Column(name = "recruiter")
    private int recruiter = 0;

    @Column(name = "totaltime")
    private int totalTime = 0;

    public Account() {
    }

    public Account(String username, byte[] salt, byte[] verifier, String email) {
        this.username = username;
        this.salt = salt;
        this.verifier = verifier;
        this.email = email;
    }

}
