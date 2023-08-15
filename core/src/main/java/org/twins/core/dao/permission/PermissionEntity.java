package org.twins.core.dao.permission;

import lombok.Data;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Data
@Table(name = "permission")
public class PermissionEntity {
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

    @ManyToOne
    @JoinColumn(name = "permission_group_id", insertable = false, updatable = false, nullable = false)
    private PermissionGroupEntity permissionGroupByPermissionGroupId;
}
