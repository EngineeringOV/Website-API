package io.github.engineeringov.website.api.common.jpa.model.custom;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "store_package_availability", schema="acore_custom", catalog="acore_custom")
public class StorePackageAvailability {


    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Column(name = "`uuid`")
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "itemBase")
    public StorePackageBase itemBase;
    @Column(name = "current_price")
    public long currentPrice;
    @Column(name = "`current_price_units`")
    public String currentPriceUnits;
    @Column(name = "ends_at")
    public LocalDateTime endsAt;
    @Column(name = "starts_at")
    public LocalDateTime startsAt;

}
