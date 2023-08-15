package org.twins.core.dao.view;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import lombok.Data;
import org.hibernate.annotations.Type;

import jakarta.persistence.*;
import org.twins.core.dao.Channel;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Table(name = "view_widget_override")
public class ViewWidgetOverrideEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "override_view_widget_id")
    private UUID overrideViewWidgetId;

    @Enumerated(EnumType.STRING)
    private Channel overrideForChannel;

    @Column(name = "override_eclipse")
    private Boolean overrideEclipse;

    @Column(name = "view_layout_position_id")
    private UUID viewLayoutPositionId;

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
    @JoinColumn(name = "override_view_widget_id", insertable = false, updatable = false, nullable = false)
    private ViewWidgetEntity overrideViewWidget;

    @ManyToOne
    @JoinColumn(name = "view_layout_position_id", insertable = false, updatable = false, nullable = false)
    private ViewLayoutPositionEntity viewLayoutPosition;
}
