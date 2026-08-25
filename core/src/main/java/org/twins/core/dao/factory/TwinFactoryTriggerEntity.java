package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.Identifiable;

import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "twin_factory_trigger")
public class TwinFactoryTriggerEntity implements EasyLoggable, Identifiable, ContainsFactoryConditionSet {

    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_factory_id")
    private UUID twinFactoryId;

    @Column(name = "input_twin_class_id")
    private UUID inputTwinClassId;

    @Column(name = "twin_factory_condition_set_id")
    private UUID twinFactoryConditionSetId;

    @Column(name = "twin_factory_condition_invert")
    private Boolean twinFactoryConditionInvert;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "description")
    private String description;

    @Column(name = "twin_trigger_id")
    private UUID twinTriggerId;

    @Column(name = "async")
    private Boolean async;

    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity twinFactorySpecOnly;

    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity inputTwinClassSpecOnly;

    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_condition_set_id", insertable = false, updatable = false)
    private TwinFactoryConditionSetEntity twinFactoryConditionSetSpecOnly;

    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_trigger_id", insertable = false, updatable = false)
    private TwinTriggerEntity twinTriggerSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinFactoryEntity twinFactory;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity twinClass;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinTriggerEntity twinTrigger;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinFactoryConditionSetEntity twinFactoryConditionSet;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryTrigger[" + id + "]";
            default -> "twinFactoryTrigger[id:" + id + ", twinFactoryId:" + twinFactoryId + ", active:" + active + "]";
        };
    }
}
