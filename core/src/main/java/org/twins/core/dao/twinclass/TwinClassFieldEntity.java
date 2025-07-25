package org.twins.core.dao.twinclass;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.service.SystemEntityService;

import java.util.HashMap;
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

    @Column(name = "view_permission_id")
    private UUID viewPermissionId;

    @Column(name = "edit_permission_id")
    private UUID editPermissionId;

    @Column(name = "required", nullable = false)
    private Boolean required; //not a primitive type because the update logic will break

    @Column(name = "external_id")
    private String externalId;

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

    @FeaturerList(type = FieldTyper.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "field_typer_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity fieldTyperFeaturer;

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

    @Transient
    @EqualsAndHashCode.Exclude
    private TwinFieldStorage fieldStorage;

    public String easyLog(Level level) {
        return "twinClassField[id:" + id + ", key:" + key + "]";
    }

    public boolean isBaseField() {
        return SystemEntityService.isSystemField(id);
    }
}
