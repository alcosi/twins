package org.twins.core.dao.card;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.Type;
import org.twins.core.dao.Channel;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Table(name = "card_widget_override")
public class CardWidgetOverrideEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "override_card_widget_id")
    private UUID overrideCardWidgetId;

    @Column(name = "override_for_channel_id")
    @Enumerated(EnumType.STRING)
    private Channel overrideForChannel;

    @Column(name = "override_eclipse")
    private Boolean overrideEclipse;

    @Column(name = "card_layout_position_id")
    private UUID cardLayoutPositionId;

    @Column(name = "in_position_order")
    private Integer inPositionOrder;

    @Column(name = "name")
    private String name;

    @Column(name = "color")
    private String color;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "widget_data_grabber_params", columnDefinition = "hstore")
    private HashMap<String, String> widgetDataGrabberParams;

    @ManyToOne
    @JoinColumn(name = "override_card_widget_id", insertable = false, updatable = false, nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private CardWidgetEntity overrideCardWidget;

    @ManyToOne
    @JoinColumn(name = "card_layout_position_id", insertable = false, updatable = false, nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private CardLayoutPositionEntity cardLayoutPosition;
}
