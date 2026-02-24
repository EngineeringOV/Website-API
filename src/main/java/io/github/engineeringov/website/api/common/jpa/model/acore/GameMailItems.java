package io.github.engineeringov.website.api.common.jpa.model.acore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mail_items", schema="acore_characters", catalog = "acore_characters")
public class GameMailItems implements Serializable {

    @ManyToOne
    @JoinColumn(columnDefinition = "INT", name = "mail_id")
    private GameMail mailId;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT", name = "item_guid")
    @Id
    private int itemGuid;

    @ManyToOne
    @JoinColumn(columnDefinition = "INT", name = "receiver")
    private Character receiver;
}
