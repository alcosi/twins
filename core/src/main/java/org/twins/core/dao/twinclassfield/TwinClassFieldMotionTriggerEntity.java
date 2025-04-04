package org.twins.core.dao.twinclassfield;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.PublicCloneable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.motion.trigger.MotionTrigger;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_class_field_motion_trigger")
@FieldNameConstants
public class TwinClassFieldMotionTriggerEntity implements EasyLoggable, PublicCloneable<TwinClassFieldMotionTriggerEntity> {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_field_motion_id")
    private UUID fieldMotionId;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "motion_trigger_featurer_id")
    private Integer motionTriggerFeaturerId;

    @FeaturerList(type = MotionTrigger.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "motion_trigger_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity motionTriggerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "motion_trigger_params", columnDefinition = "hstore")
    private HashMap<String, String> motionTriggerParams;

    @Column(name = "active")
    private boolean isActive;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "fieldMotionTrigger[" + id + "]";
            case NORMAL ->
                    "fieldMotionTrigger[id:" + id + ", motionId:" + fieldMotionId + ", isActive: " + isActive + "]";
            default ->
                    "fieldMotionTrigger[id:" + id + ", motionId:" + fieldMotionId + ", order:" + order + ", featurer:" + motionTriggerFeaturerId + ", isActive: " + isActive + "]";
        };
    }

    @Override
    public TwinClassFieldMotionTriggerEntity clone() {
        return new TwinClassFieldMotionTriggerEntity()
                .setFieldMotionId(fieldMotionId)
                .setOrder(order)
                .setMotionTriggerFeaturerId(motionTriggerFeaturerId)
                .setMotionTriggerFeaturer(motionTriggerFeaturer)
                .setMotionTriggerParams(motionTriggerParams)
                .setActive(isActive);
    }
}
