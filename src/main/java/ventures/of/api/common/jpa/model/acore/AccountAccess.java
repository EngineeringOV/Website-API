package ventures.of.api.common.jpa.model.acore;

import lombok.Data;
import jakarta.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "account_access", schema="acore_auth", catalog="acore_auth")
public class AccountAccess implements Serializable {
    private static final long serialVersionUID = 2405172042350251288L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "gmlevel")
    private short gmlevel;

    @Column(name = "RealmID")
    private short realmId;

    @Column(name = "comment")
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
