package io.github.engineeringov.website.api.common.jpa.model.custom;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name = "store_package_base", schema="acore_custom", catalog="acore_custom")
public class StorePackageBase {

    @Id
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy = "uuid")
    @Column(name = "`uuid`")
    private String uuid;

    @OneToMany(mappedBy="itemBase")
    public List<StorePackageItem> items;

    @Column(name = "`name_package`")
    public String namePackage;
    @Column(name = "`subtext`")
    public String subtext;
    @Column(name = "`type`")
    public String type;
    //Used for frontend to calculate discounts ETC
    @Column(name = "`full_price`")
    public long fullPrice;
    @Column(name = "`price_units`")
    public String priceUnits;
    @Column(name = "`image_url`")
    public String imageUrl;
    @Column(name = "copper")
    public int copper;
}
