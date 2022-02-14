package io.github.engineeringov.website.api.common.jpa.model.acore;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "account_access", schema="acore_auth", catalog="acore_auth")
@ToString(exclude = "account")
public class AccountAccess implements Serializable {
    private static final long serialVersionUID = 2405172042350251288L;

    @Id
    @Column(columnDefinition = "INT", name="id")
    private int id;

    @OneToOne
    @PrimaryKeyJoinColumn
    private Account account;

    @Column(columnDefinition = "INT", name = "gmlevel")
    private short gmlevel = 1;

    @Column(columnDefinition = "INT", name = "RealmID")
    private short realmId;

    @Column(columnDefinition = "INT", name = "comment")
    private short comment;

    public String gmLevelToString(){
        switch(gmlevel) {
            case(3):return "ROLE_ADMIN";
            case(2):return "ROLE_GM";
            case(1):
            default: return "ROLE_PLAYER";
        }

    }
}
