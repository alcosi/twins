package org.twins.core.dao.card;

import jakarta.persistence.*;
import lombok.Data;
import org.twins.core.dao.Channel;

import java.util.UUID;

@Entity
@Data
@Table(name = "card_layout")
public class CardLayoutEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "channel_id")
    @Enumerated(EnumType.STRING)
    private Channel channel;

    @Column(name = "key")
    private String key;
}
