package org.twins.core.dao.card;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.common.util.UuidUtils;
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
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "channel_id")
    @Enumerated(EnumType.STRING)
    private Channel channel;

    @Column(name = "key")
    private String key;
}
