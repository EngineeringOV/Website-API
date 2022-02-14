package io.github.engineeringov.website.api.common.jpa.model.acore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@ToString(exclude = {"sender", "receiver"})
@Table(name = "mail", schema="acore_characters", catalog = "acore_characters")
public class GameMail {

    // The wiki says "Don't have autoincrement !!!" but doesn't give any suggestions, amazing
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    private int id;

    @Column(columnDefinition = "TINYINT", name = "messageType")
    //@Type(type = "org.hibernate.type.BinaryType")
    private byte messageType;

    @Column(columnDefinition = "TINYINT", name = "stationary")
    //@Type(type = "org.hibernate.type.BinaryType")
    private byte stationary;

    @Column(columnDefinition = "SMALLINT", name = "mailTemplateId")
    @Type(type = "org.hibernate.type.ShortType")
    private short mailTemplateId;

    //@Column(columnDefinition = "INT", name = "sender")
    @Type(type = "org.hibernate.type.IntegerType")
    @JoinColumn(name = "sender")
    @ManyToOne
    private Character sender;

    //@Column(columnDefinition = "INT", name = "receiver")
    @Type(type = "org.hibernate.type.IntegerType")
    @JoinColumn(name = "receiver")
    @ManyToOne
    private Character receiver;

    @Column(columnDefinition = "LONGTEXT", name = "subject")
    @Type(type = "org.hibernate.type.TextType")
    private String subject;

    @Column(columnDefinition = "LONGTEXT", name = "`body`")
    @Type(type = "org.hibernate.type.TextType")
    private String body;

    @Column(columnDefinition = "TINYINT", name = "has_items")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean hasItems;

    @Column(columnDefinition = "INT", name = "expire_time")
    @Type(type = "org.hibernate.type.IntegerType")
    private int expireTime;

    @Column(columnDefinition = "INT", name = "deliver_time")
    @Type(type = "org.hibernate.type.IntegerType")
    private int deliverTime;

    @Column(columnDefinition = "INT", name = "money")
    @Type(type = "org.hibernate.type.IntegerType")
    private int money;

    @Column(columnDefinition = "INT", name = "cod")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean cod;

    @Column(columnDefinition = "TINYINT", name = "checked")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private boolean checked;

    @Column(columnDefinition = "INT", name = "auctionId")
    @Type(type = "org.hibernate.type.IntegerType")
    private int auctionId;

}
