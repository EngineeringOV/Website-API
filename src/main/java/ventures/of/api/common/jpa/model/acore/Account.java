package ventures.of.api.common.jpa.model.acore;

import lombok.Data;
import lombok.NoArgsConstructor;
import ventures.of.api.model.WowCryptoInfo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;


@Data
@Entity
@Table(name = "account", schema="acore_auth", catalog="acore_auth")
@NoArgsConstructor
public class Account implements Serializable {
    private static final long serialVersionUID = 2405172042350251807L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "salt")
    private byte[] salt;

    @Column(name = "verifier")
    private byte[] verifier;

    @Column(name = "session_key")
    private byte[] sessionKey = null;

    @Column(name = "totp_secret")
    private byte[] totpSecret = null;

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
    private boolean locked = false;

    @Column(name = "lock_country")
    private String lockCountry = "00";

    @Column(name = "last_login")
    private LocalDate lastLogin = null;

    @Column(name = "online", columnDefinition = "INT")
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

    // These exist in their own tables
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private AccountAccess accountAccess;

    @OneToMany(mappedBy="account")
    private Set<Character> characters;

    public Account(String username, WowCryptoInfo wowCryptoInfo, String email) {
        this.username = username;
        this.salt = wowCryptoInfo.getSalt();
        this.verifier = wowCryptoInfo.getVerifier();
        this.email = email;
        this.regMail = email;
    }

}
