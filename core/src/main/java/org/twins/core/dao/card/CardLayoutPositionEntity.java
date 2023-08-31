package org.twins.core.dao.card;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "card_layout_position")
public class CardLayoutPositionEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "card_layout_id")
    private UUID cardLayoutId;

    @Column(name = "key")
    private String key;

    @ManyToOne
    @JoinColumn(name = "card_layout_id", insertable = false, updatable = false, nullable = false)
    private CardLayoutEntity cardLayout;
}
