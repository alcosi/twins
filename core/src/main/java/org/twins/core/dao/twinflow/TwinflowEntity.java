package org.twins.core.dao.twinflow;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.eraseflow.EraseflowEntity;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.enums.factory.FactoryLauncher;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twinflow")
@FieldNameConstants
public class TwinflowEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "inheritable")
    private Boolean inheritable;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "initial_twin_status_id")
    private UUID initialTwinStatusId;

    @Column(name = "eraseflow_id")
    private UUID eraseflowId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "initial_sketch_twin_status_id")
    private UUID initialSketchTwinStatusId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table
    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", referencedColumnName = "name_i18n_id", insertable = false, updatable = false)
    private List<I18nTranslationEntity> nameI18nTranslationsSpecOnly;

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table
    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", referencedColumnName = "description_i18n_id", insertable = false, updatable = false)
    private List<I18nTranslationEntity> descriptionI18nTranslationsSpecOnly;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "initial_twin_status_id", insertable = false, updatable = false, nullable = false)
    private TwinStatusEntity initialTwinStatus;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "initial_sketch_twin_status_id", insertable = false, updatable = false, nullable = false)
    private TwinStatusEntity initialSketchTwinStatus;

    //    needed for specification
    @Deprecated
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twinflow_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Collection<TwinflowSchemaMapEntity> schemaMappings;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinflowTransitionEntity, UUID> transitionsKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinflowFactoryEntity, FactoryLauncher> factoriesKit;

    // only for manual load (needed only for deletion)
    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EraseflowEntity eraseflow;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinflow[" + id + "]";
            case NORMAL -> "twinflow[id:" + id + ", twinClassId:" + twinClassId + "]";
            default -> "twinflow[id:" + id +
                    ", twinClassId:" + twinClassId +
                    ", inheritable:" + inheritable +
                    ", initialTwinStatusId:" + initialTwinStatusId +
                    ", initialSketchTwinStatusId:" + initialSketchTwinStatusId +
                    ", createdByUserId:" + createdByUserId +
                    ", eraseflowId:" + eraseflowId + "]";
        };
    }
}
