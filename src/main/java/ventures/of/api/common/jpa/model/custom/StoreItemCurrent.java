package ventures.of.api.common.jpa.model.custom;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
public class StoreItemCurrent {


    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Column(name = "`uuid`")
    private String uuid;

    @ManyToOne
    @Column(name = "item_base")
    @JoinColumn(columnDefinition = "id")
    public StoreItemBase itemBase;
    @Column(name = "current_price")
    public long currentPrice;
    public long endsAt;
    public long startsAt;

}
