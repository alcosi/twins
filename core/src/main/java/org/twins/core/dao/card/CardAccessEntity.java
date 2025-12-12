package org.twins.core.dao.card;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Entity
@Data
@Table(name = "card_access")
public class CardAccessEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "`order`")
    @Basic
    private int order;

    @Column(name = "card_id")
    private UUID cardId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity twinClass;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "card_id", insertable = false, updatable = false, nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private CardEntity card;
}
