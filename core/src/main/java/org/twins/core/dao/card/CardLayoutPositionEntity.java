package org.twins.core.dao.card;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Data
@Table(name = "card_layout_position")
public class CardLayoutPositionEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "card_layout_id")
    private UUID cardLayoutId;

    @Column(name = "key")
    private String key;

    @ManyToOne
    @JoinColumn(name = "card_layout_id", insertable = false, updatable = false, nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private CardLayoutEntity cardLayout;
}
