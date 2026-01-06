package org.twins.core.dao.twinclass;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.hibernate.annotations.Type;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.projection.ProjectionEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.service.SystemEntityService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_class_field")
@FieldNameConstants
public class TwinClassFieldEntity implements EasyLoggable {

    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UUID.nameUUIDFromBytes((key + twinClassId).getBytes());
        }
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "key")
    private String key;

    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18nId;

    @Column(name = "field_typer_featurer_id")
    private Integer fieldTyperFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "field_typer_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldTyperParams;

    @Column(name = "twin_sorter_featurer_id")
    private Integer twinSorterFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "twin_sorter_params", columnDefinition = "hstore")
    private HashMap<String, String> twinSorterParams;

    @Column(name = "view_permission_id")
    private UUID viewPermissionId;

    @Column(name = "edit_permission_id")
    private UUID editPermissionId;

    @Column(name = "required", nullable = false)
    private Boolean required; //not a primitive type because the update logic will break

    @Column(name = "external_id")
    private String externalId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "external_properties", columnDefinition = "hstore")
    private Map<String, String> externalProperties;

    @Column(name = "fe_validation_error_i18n_id")
    private UUID feValidationErrorI18nId;

    @Column(name = "be_validation_error_i18n_id")
    private UUID beValidationErrorI18nId;

    @Column(name = "system")
    private Boolean system;  //not a primitive type because the update logic will break

    @Column(name = "dependent_field")
    private Boolean dependentField;

    @Column(name = "has_dependent_fields")
    private Boolean hasDependentFields;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "projection_field")
    private Boolean projectionField;

    @Column(name = "has_projected_fields")
    private Boolean hasProjectedFields;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity nameI18n;

    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    private I18nEntity descriptionI18n;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "view_permission_id", insertable = false, updatable = false)
    private PermissionEntity viewPermission;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edit_permission_id", insertable = false, updatable = false)
    private PermissionEntity editPermission;

    //needed for specification
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "src_twin_class_field_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Collection<ProjectionEntity> projectionsBySrc;

    //needed for specification
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "dst_twin_class_field_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Collection<ProjectionEntity> projectionsByDst;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TwinFieldStorage fieldStorage;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Kit<TwinClassFieldRuleEntity, UUID> ruleKit;

    public String easyLog(Level level) {
        return "twinClassField[id:" + id + ", key:" + key + "]";
    }

    public boolean isBaseField() {
        return SystemEntityService.isSystemField(id);
    }
}
