package io.github.engineeringov.website.api.common.jpa.model.custom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "account_reset_request", schema="acore_custom", catalog="acore_custom")
public class AccountReset {

    public AccountReset(String email) {
        this.email = email;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "`uuid`")
    private String uuid;

    @Column(name = "email")
    private String email;

    @Column(name = "ip_address")
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "valid_request", columnDefinition = "TINYINT")
    private boolean validRequest = false;

    public AccountReset(String email, String ipAddress) {
        this.email = email;
        this.ipAddress = ipAddress;
    }
}
