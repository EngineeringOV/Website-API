package io.github.engineeringov.website.api.common.jpa.model.acore;

import io.github.engineeringov.website.api.common.jpa.model.custom.StoreAccountTokens;
import io.github.engineeringov.website.api.model.WowCryptoInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Data
@Entity
@Table(name = "account", schema="acore_auth", catalog="acore_auth")
@NoArgsConstructor
@ToString(exclude = {"characters", "salt", "verifier", "storeAccountTokens"})
public class Account implements Serializable {
    private static final long serialVersionUID = 2405172042350251807L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT", name = "id")
    private int id;

    @Column(columnDefinition = "VARCHAR(32)", name = "username")
    private String username;

    @Column(columnDefinition = "BINARY(32)", name = "salt")
    private byte[] salt;

    @Column(columnDefinition = "BINARY(32)", name = "verifier")
    private byte[] verifier;

    @Column(columnDefinition = "BINARY(40)", name = "session_key")
    private byte[] sessionKey = null;

    @Column(columnDefinition = "VARBINARY(100)", name = "totp_secret")
    private byte[] totpSecret = null;

    @Column(columnDefinition = "VARCHAR(255)", name = "email")
    private String email = "";

    @Column(columnDefinition = "VARCHAR(255)", name = "reg_mail")
    private String regMail = "";

    @Column(columnDefinition = "TIMESTAMP", name = "joindate")
    private LocalDate joinDate = LocalDate.now();

    @Column(columnDefinition = "VARCHAR(15)", name = "last_ip")
    private String lastIp = "127.0.0.1";

    @Column(columnDefinition = "VARCHAR(15)", name = "last_attempt_ip")
    private String lastAttemptIp = "127.0.0.1";

    @Column(columnDefinition = "INT", name = "failed_logins")
    private int failedLogins = 0;

    @Column( columnDefinition = "TINYINT", name = "locked")
    private boolean locked = false;

    @Column(columnDefinition = "VARCHAR(2)", name = "lock_country")
    private String lockCountry = "00";

    @Column(columnDefinition = "TIMESTAMP", name = "last_login")
    private LocalDate lastLogin = null;

    @Column(columnDefinition = "INT", name = "online")
    private boolean online = false;

    @Column(columnDefinition = "TINYINT", name = "expansion")
    private short expansion = 0;

    @Column(columnDefinition = "BIGINT", name = "mutetime")
    private int muteTime = 0;

    @Column(columnDefinition = "VARCHAR(255)", name = "mutereason")
    private String muteReason = "";

    @Column(columnDefinition = "VARCHAR(50)", name = "muteby")
    private String muteBy = "";

    @Column(columnDefinition = "TINYINT", name = "locale")
    private short locale = 0;

    @Column(columnDefinition = "VARCHAR(3)", name = "os")
    private String os = "";

    @Column(columnDefinition = "INT", name = "recruiter")
    private int recruiter = 0;

    @Column(columnDefinition = "INT", name = "totaltime")
    private int totalTime = 0;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "account" )
    private AccountAccess accountAccess;

    @OneToMany(mappedBy="account")
    private Set<Character> characters;

    @OneToOne(fetch = FetchType.LAZY, mappedBy="account")
    private StoreAccountTokens storeAccountTokens;

    public Account(String username, WowCryptoInfo wowCryptoInfo, String email) {
        this.username = username;
        this.salt = wowCryptoInfo.getSalt();
        this.verifier = wowCryptoInfo.getVerifier();
        this.email = email;
        this.regMail = email;
    }

}
