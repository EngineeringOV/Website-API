package io.github.engineeringov.website.api.common.jpa.model.custom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "store_package_item", schema="acore_custom", catalog="acore_custom")
@NoArgsConstructor
@AllArgsConstructor
public class StorePackageItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "`uuid`")
    private String uuid;

    @ManyToOne
    @JoinColumn(columnDefinition = "id")
    public StorePackageBase itemBase;
    @Column(name = "item_id_alliance")
    public int itemIdAlliance;
    @Column(name = "item_id_horde")
    public int itemIdHorde;
    @Column(name = "quantity_alliance")
    public int quantityAlliance;
    @Column(name = "quantity_horde")
    public int quantityHorde;

}
