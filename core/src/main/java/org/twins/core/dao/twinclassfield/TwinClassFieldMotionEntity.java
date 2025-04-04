package org.twins.core.dao.twinclassfield;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.hibernate.annotations.CreationTimestamp;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.validator.TwinClassFieldMotionValidatorRuleEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_class_field_motion")
@FieldNameConstants
public class TwinClassFieldMotionEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_field_motion_schema_id")
    private UUID fieldMotionSchemaId;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @Column(name = "permission_id")
    private UUID permissionId;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_field_motion_schema_id", insertable = false, updatable = false, nullable = false)
    private TwinClassFieldMotionSchemaEntity motionSchema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    private I18nEntity nameI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    private I18nEntity descriptionI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false, nullable = false)
    private TwinClassFieldEntity twinClassField;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinClassFieldMotionValidatorRuleEntity, UUID> validatorRulesKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinClassFieldMotionTriggerEntity, UUID> triggersKit;

    @Transient // because field can be useful only in admin panel
    @EqualsAndHashCode.Exclude
    private PermissionEntity permission;

    @Transient // because field can be useful only in admin panel
    @EqualsAndHashCode.Exclude
    private UserEntity createdByUser;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "fieldMotion[" + id + "]";
            case NORMAL -> "fieldMotion[id:" + id + ", class:" + twinClassId +  "]";
            default -> "fieldMotion[id:" + id + ", class:" + twinClassId + ", field:" + twinClassFieldId + "]";
        };
    }
}
