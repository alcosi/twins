package org.twins.core.dao.view;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.twins.core.dao.widget.WidgetEntity;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Table(name = "view_widget")
public class ViewWidgetEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "view_tab_id")
    private UUID viewTabId;

    @Column(name = "view_layout_position_id")
    private UUID viewLayoutPositionId;

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
    @JoinColumn(name = "view_tab_id", insertable = false, updatable = false, nullable = false)
    private ViewTabEntity viewTab;

    @ManyToOne
    @JoinColumn(name = "view_layout_position_id", insertable = false, updatable = false, nullable = false)
    private ViewLayoutPositionEntity viewLayoutPosition;

    @ManyToOne
    @JoinColumn(name = "widget_id", insertable = false, updatable = false, nullable = false)
    private WidgetEntity widget;
}
