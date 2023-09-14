package org.twins.core.dao.widget;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.widget.accessor.WidgetAccessor;
import org.twins.core.featurer.widget.datagrabber.DataGrabber;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(fluent = true)
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

    @Column(name = "widget_accessor_featurer_id")
    private int widgetAccessorFeaturerId;

    @FeaturerList(type = DataGrabber.class)
    @ManyToOne
    @JoinColumn(name = "widget_data_grabber_featurer_id", insertable = false, updatable = false, nullable = false)
    private FeaturerEntity widgetDataGrabberFeaturer;

    @FeaturerList(type = WidgetAccessor.class)
    @ManyToOne
    @JoinColumn(name = "widget_accessor_featurer_id", insertable = false, updatable = false, nullable = false)
    private FeaturerEntity widgetAccessorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "widget_accessor_params", columnDefinition = "hstore")
    private HashMap<String, String> widgetAccessorParams;
}
