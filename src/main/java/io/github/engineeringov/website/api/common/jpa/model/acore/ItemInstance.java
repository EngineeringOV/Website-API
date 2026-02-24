package io.github.engineeringov.website.api.common.jpa.model.acore;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@Entity
@NoArgsConstructor
@Table(name = "item_instance", schema="acore_characters", catalog = "acore_characters")
public class ItemInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT", name = "`guid`")
    private int guid;

    @Column(columnDefinition = "MEDIUMINT", name = "itemEntry")
    private int itemEntry;

    @Column(columnDefinition = "INT", name = "owner_guid")
    private int ownerGuid;

    @Column(columnDefinition = "INT", name = "creatorGuid")
    private int creatorGuid;

    @Column(columnDefinition = "INT", name = "giftCreatorGuid")
    private int giftCreatorGuid;

    @Column(columnDefinition = "INT", name = "`count`")
    private int count;

    @Column(columnDefinition = "INT", name = "duration")
    private int duration;

    @Column(columnDefinition = "TINYINT", name = "charges")
    private short charges;

    @Column(columnDefinition = "MEDIUMINT", name = "`flags`")
    private int flags;

    @Column(columnDefinition = "TEXT", name = "enchantments")
    private String enchantments;

    @Column(columnDefinition = "SMALLINT", name = "randomPropertyId")
    private short randomPropertyId;

    @Column(columnDefinition = "SMALLINT", name = "durability")
    private short durability;

    @Column(columnDefinition = "INT", name = "playedTime")
    private int playedTime;

    @Column(columnDefinition = "TEXT", name = "`text`")
    private String text;

}
