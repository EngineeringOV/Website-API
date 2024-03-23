package io.github.engineeringov.website.api.common.jpa.model.custom;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import io.github.engineeringov.website.api.common.jpa.model.acore.Account;

import javax.persistence.*;

@Data
@Entity
@Table(name = "store_account_tokens", schema="acore_custom", catalog="acore_custom")
@ToString(exclude = "account")
@NoArgsConstructor
public class StoreAccountTokens {

    public StoreAccountTokens(Account account){
        this.account = account;
    }

    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Column(name = "`uuid`")
    private String uuid;

    @OneToOne
    public Account account;
    @Column(name = "vote_token")
    public int voteToken = 0;
    @Column(name = "premium_token")
    public int premiumToken = 0;
    @Column(name = "free_token")
    public int freeToken = 0;

}
