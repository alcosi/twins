package org.twins.core.dao.card;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.twins.core.dao.widget.WidgetEntity;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "card_widget")
public class CardWidgetEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "card_id")
    private UUID cardId;

    @Column(name = "card_layout_position_id")
    private UUID cardLayoutPositionId;

    @Column(name = "in_position_order")
    private Integer inPositionOrder;

    @Column(name = "name")
    private String name;

    @Column(name = "color")
    private String color;

    @Column(name = "widget_id")
    private UUID widgetId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "widget_data_grabber_params", columnDefinition = "hstore")
    private HashMap<String, String> widgetDataGrabberParams;

    @ManyToOne
    @JoinColumn(name = "card_id", insertable = false, updatable = false, nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private CardEntity card;

    @ManyToOne
    @JoinColumn(name = "card_layout_position_id", insertable = false, updatable = false, nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private CardLayoutPositionEntity cardLayoutPosition;

    @ManyToOne
    @JoinColumn(name = "widget_id", insertable = false, updatable = false, nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private WidgetEntity widget;
}
