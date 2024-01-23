package ventures.of.api.common.jpa.model.custom;

import org.hibernate.annotations.GenericGenerator;
import ventures.of.api.common.jpa.model.acore.Account;

import javax.persistence.*;

@Entity
public class XnoWallet {


    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Column(name = "`uuid`")
    private String uuid;

    @Column(name = "account_id")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    public Account account;
    @Column(name = "address")
    public String address;
    @Column(name = "seed_phrase")
    public String seedPhrase;
    @Column(name = "reserved_money")
    public String reservedMoney;
    @Column(name = "is_created")
    public boolean isCreated;

}
