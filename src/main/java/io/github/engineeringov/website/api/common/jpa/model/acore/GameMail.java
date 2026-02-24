package io.github.engineeringov.website.api.common.jpa.model.acore;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;

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
    private byte messageType;

    @Column(columnDefinition = "TINYINT", name = "stationary")
    private byte stationary;

    @Column(columnDefinition = "SMALLINT", name = "mailTemplateId")
    private short mailTemplateId;

    @JoinColumn(name = "sender")
    @ManyToOne
    private Character sender;

    @JoinColumn(name = "receiver")
    @ManyToOne
    private Character receiver;

    @Column(columnDefinition = "LONGTEXT", name = "subject")
    private String subject;

    @Column(columnDefinition = "LONGTEXT", name = "`body`")
    private String body;

    @Column(columnDefinition = "TINYINT", name = "has_items")
    private boolean hasItems;

    @Column(columnDefinition = "INT", name = "expire_time")
    private int expireTime;

    @Column(columnDefinition = "INT", name = "deliver_time")
    private int deliverTime;

    @Column(columnDefinition = "INT", name = "money")
    private int money;

    @Column(columnDefinition = "INT", name = "cod")
    private boolean cod;

    @Column(columnDefinition = "TINYINT", name = "checked")
    private boolean checked;

    @Column(columnDefinition = "INT", name = "auctionId")
    private int auctionId;

}
