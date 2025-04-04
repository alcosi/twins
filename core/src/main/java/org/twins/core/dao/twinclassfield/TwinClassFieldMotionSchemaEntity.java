package org.twins.core.dao.twinclassfield;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_class_field_motion_schema")
@FieldNameConstants
public class TwinClassFieldMotionSchemaEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    private UUID eraseflowId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;

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

    @Override
    public String easyLog(Level level) {
        return "fieldMotionSchema[id:" + id + "]";
    }
}
