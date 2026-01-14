package org.twins.core.dao.card;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import org.twins.core.dao.Channel;

import java.util.UUID;

@Entity
@Data
@Table(name = "card_layout")
public class CardLayoutEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "channel_id")
    @Enumerated(EnumType.STRING)
    private Channel channel;

    @Column(name = "key")
    private String key;
}
