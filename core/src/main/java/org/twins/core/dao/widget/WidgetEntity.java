package org.twins.core.dao.widget;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.twins.core.dao.AccessOrder;
import org.twins.core.featurer.widget.datagrabber.DataGrabber;

import java.util.UUID;

@Entity
@Data
@Table(name = "widget")
public class WidgetEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "key")
    private String key;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "widget_data_grabber_featurer_id")
    private int widgetDataGrabberFeaturerId;

    @Column(name = "access_order")
    @Enumerated(EnumType.STRING)
    private AccessOrder accessOrder;

    @FeaturerList(type = DataGrabber.class)
    @ManyToOne
    @JoinColumn(name = "widget_data_grabber_featurer_id", insertable = false, updatable = false, nullable = false)
    private FeaturerEntity widgetDataGrabberFeaturer;
}
