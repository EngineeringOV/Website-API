package io.github.engineeringov.website.api.common.jpa.model.acore;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@Table(name = "item_instance", schema="acore_characters", catalog = "acore_characters")
public class ItemInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT", name = "`guid`")
    @Type(type = "org.hibernate.type.IntegerType")
    private int guid;

    @Column(columnDefinition = "MEDIUMINT", name = "itemEntry")
    @Type(type = "org.hibernate.type.IntegerType")
    private int itemEntry;

    @Column(columnDefinition = "INT", name = "owner_guid")
    @Type(type = "org.hibernate.type.IntegerType")
    private int ownerGuid;

    @Column(columnDefinition = "INT", name = "creatorGuid")
    @Type(type = "org.hibernate.type.IntegerType")
    private int creatorGuid;

    @Column(columnDefinition = "INT", name = "giftCreatorGuid")
    @Type(type = "org.hibernate.type.IntegerType")
    private int giftCreatorGuid;

    @Column(columnDefinition = "INT", name = "`count`")
    @Type(type = "org.hibernate.type.IntegerType")
    private int count;

    @Column(columnDefinition = "INT", name = "duration")
    @Type(type = "org.hibernate.type.IntegerType")
    private int duration;

    @Column(columnDefinition = "TINYINT", name = "charges")
    private short charges;

    @Column(columnDefinition = "MEDIUMINT", name = "`flags`")
    @Type(type = "org.hibernate.type.IntegerType")
    private int flags;

    @Column(columnDefinition = "TEXT", name = "enchantments")
    @Type(type = "org.hibernate.type.TextType")
    private String enchantments;

    @Column(columnDefinition = "SMALLINT", name = "randomPropertyId")
    private short randomPropertyId;

    @Column(columnDefinition = "SMALLINT", name = "durability")
    private short durability;

    @Column(columnDefinition = "INT", name = "playedTime")
    @Type(type = "org.hibernate.type.IntegerType")
    private int playedTime;

    @Column(columnDefinition = "TEXT", name = "`text`")
    @Type(type = "org.hibernate.type.TextType")
    private String text;

}
