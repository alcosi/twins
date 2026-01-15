package org.twins.core.dao.card;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cambium.common.util.UuidUtils;

import java.util.UUID;

@Entity
@Data
@Table(name = "card_layout_position")
public class CardLayoutPositionEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
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
