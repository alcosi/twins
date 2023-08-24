package org.twins.core.dao.widget;

import jakarta.persistence.*;
import lombok.Data;
import org.twins.core.dao.AccessRule;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Entity
@Data
@Table(name = "widget_access")
public class WidgetAccessEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "widget_id")
    private UUID widgetId;

    @Column(name = "access_rule")
    @Enumerated(EnumType.STRING)
    private AccessRule accessRule;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @ManyToOne
    @JoinColumn(name = "widget_id", insertable = false, updatable = false, nullable = false)
    private WidgetEntity widget;

    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;
}
