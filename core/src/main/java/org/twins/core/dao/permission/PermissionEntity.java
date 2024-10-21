package org.twins.core.dao.permission;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.util.Set;
import java.util.UUID;

@Entity
@Data
@FieldNameConstants
@Table(name = "permission")
public class PermissionEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "key")
    private String key;

    @Column(name = "permission_group_id")
    private UUID permissionGroupId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Transient
    private Set<UUID> permissionGroups;

    @ManyToOne
    @JoinColumn(name = "permission_group_id", insertable = false, updatable = false, nullable = false)
    private PermissionGroupEntity permissionGroup;

    public String easyLog(Level level) {
        return "permission[id:" + id + ", key:" + key + "]";
    }
}
