package org.twins.core.dao.widget;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(fluent = true)
@Table(name = "widget")
public class WidgetEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "key")
    private String key;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "widget_data_grabber_featurer_id")
    private Integer widgetDataGrabberFeaturerId;

    @Column(name = "widget_accessor_featurer_id")
    private Integer widgetAccessorFeaturerId;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity widgetDataGrabberFeaturer;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity widgetAccessorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "widget_accessor_params", columnDefinition = "hstore")
    private HashMap<String, String> widgetAccessorParams;
}
