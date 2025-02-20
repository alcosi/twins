package org.twins.core.dao.permission;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "permission_grant_assignee_propagation")
public class PermissionGrantAssigneePropagationEntity {

    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "permission_schema_id")
    private UUID permissionSchemaId;

    @Column(name = "permission_id")
    private UUID permissionId;

    @Column(name = "propagation_by_twin_class_id")
    private UUID propagationByTwinClassId;

    @Column(name = "propagation_by_twin_status_id")
    private UUID propagationByTwinStatusId;

    @Column(name = "in_space_only")
    private boolean inSpaceOnly;

    @Column(name = "granted_by_user_id")
    private UUID grantedByUserId;

    @Column(name = "granted_at")
    private Timestamp grantedAt;

    @ManyToOne
    @JoinColumn(name = "permission_schema_id", insertable = false, updatable = false)
    private PermissionSchemaEntity permissionSchema;

    @ManyToOne
    @JoinColumn(name = "propagation_by_twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity twinClass;

    @ManyToOne
    @JoinColumn(name = "propagation_by_twin_status_id", insertable = false, updatable = false)
    private TwinStatusEntity twinStatus;

    @ManyToOne
    @JoinColumn(name = "permission_id", insertable = false, updatable = false, nullable = false)
    private PermissionEntity permission;

    @ManyToOne
    @JoinColumn(name = "granted_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity grantedByUser;
}
