package io.github.engineeringov.website.api.common.jpa.model.acore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mail_items", schema="acore_characters", catalog = "acore_characters")
public class GameMailItems implements Serializable {

    @ManyToOne
    @JoinColumn(columnDefinition = "INT", name = "mail_id")
    @Type(type = "org.hibernate.type.IntegerType")
    private GameMail mailId;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Type(type = "org.hibernate.type.IntegerType")
    @Column(columnDefinition = "INT", name = "item_guid")
    @Id
    private int itemGuid;

    @ManyToOne
    @JoinColumn(columnDefinition = "INT", name = "receiver")
    @Type(type = "org.hibernate.type.IntegerType")
    private Character receiver;
}
