package ventures.of.api.common.jpa.model.custom;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
public class StoreItemCurrent {


    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "`uuid`")
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "item_base", referencedColumnName = "uuid")
    public StoreItemBase itemBase;
    @Column(name = "current_price")
    public long currentPrice;
    public long endsAt;
    public long startsAt;

}
