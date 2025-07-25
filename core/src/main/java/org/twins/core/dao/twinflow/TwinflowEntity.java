package org.twins.core.dao.twinflow;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.eraseflow.EraseflowEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.Collection;
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

    @Column(name = "eraseflow_id")
    private UUID eraseflowId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "before_sketch_twin_factory_id")
    private UUID beforeSketchTwinFactoryId;

    @Column(name = "before_create_twin_factory_id")
    private UUID beforeCreateTwinFactoryId;

    @Column(name = "before_update_twin_factory_id")
    private UUID beforeUpdateTwinFactoryId;

    @Column(name = "after_sketch_twin_factory_id")
    private UUID afterSketchTwinFactoryId;

    @Column(name = "after_create_twin_factory_id")
    private UUID afterCreateTwinFactoryId;

    @Column(name = "after_update_twin_factory_id")
    private UUID afterUpdateTwinFactoryId;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity nameI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity descriptionI18n;

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @JoinColumn(name = "initial_twin_status_id", insertable = false, updatable = false, nullable = false)
    private TwinStatusEntity initialTwinStatus;

    //    needed for specification
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twinflow_id", referencedColumnName = "id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Collection<TwinflowSchemaMapEntity> schemaMappings;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "before_create_twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity beforeCreateTwinFactory;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "before_update_twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity beforeUpdateTwinFactory;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "before_sketch_twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity beforeSketchTwinFactory;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "after_create_twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity afterCreateTwinFactory;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "after_update_twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity afterUpdateTwinFactory;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "after_sketch_twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity afterSketchTwinFactory;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinflowTransitionEntity, UUID> transitionsKit;

    // only for manual load (needed only for deletion)
    @Transient
    @EqualsAndHashCode.Exclude
    private EraseflowEntity eraseflow;

    @Override
    public String easyLog(Level level) {
        return "twinflow[id:" + id + "]";
    }
}
