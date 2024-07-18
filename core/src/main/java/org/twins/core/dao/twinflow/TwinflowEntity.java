package org.twins.core.dao.twinflow;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twinflow")
@FieldNameConstants
public class TwinflowEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "initial_twin_status_id")
    private UUID initialTwinStatusId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "initial_twin_status_id", insertable = false, updatable = false, nullable = false)
    private TwinStatusEntity initialTwinStatus;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinflowTransitionEntity, UUID> transitionsKit;

    @Override
    public String easyLog(Level level) {
        return "twinflow[id:" + id + "]";
    }
}
