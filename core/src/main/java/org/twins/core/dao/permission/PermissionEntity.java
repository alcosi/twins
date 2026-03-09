package org.twins.core.dao.permission;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.i18n.I18nEntity;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "permission")
public class PermissionEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "key")
    private String key;

    @Column(name = "permission_group_id")
    private UUID permissionGroupId;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity nameI18n;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    private I18nEntity descriptionI18n;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "permission_group_id", insertable = false, updatable = false, nullable = false)
    private PermissionGroupEntity permissionGroup;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = PermissionGrantTwinRoleEntity.Fields.permission, fetch = FetchType.LAZY)
    private Set<PermissionGrantTwinRoleEntity> permissionGrantTwinRoles;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = PermissionMaterGlobalEntity.Fields.permission, fetch = FetchType.LAZY)
    private Set<PermissionMaterGlobalEntity> permissionMaterGlobals;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = PermissionMaterUserGroupEntity.Fields.permission, fetch = FetchType.LAZY)
    private Set<PermissionMaterUserGroupEntity> permissionMaterUserGroups;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = PermissionMaterSpaceUserEntity.Fields.permission, fetch = FetchType.LAZY)
    private Set<PermissionMaterSpaceUserEntity> permissionMaterSpaceUsers;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = PermissionMaterSpaceUserGroupEntity.Fields.permission, fetch = FetchType.LAZY)
    private Set<PermissionMaterSpaceUserGroupEntity> permissionMaterSpaceUserGroups;

    public String easyLog(Level level) {
        return "permission[id:" + id + ", key:" + key + "]";
    }
}
