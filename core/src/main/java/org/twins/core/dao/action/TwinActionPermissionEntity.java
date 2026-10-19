package org.twins.core.dao.action;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Data;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.enums.action.TwinAction;

import java.util.UUID;

@Entity
@Data
@Table(name = "twin_action_permission")
public class TwinActionPermissionEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_action_id")
    @Enumerated(EnumType.STRING)
    private TwinAction twinAction;

    @Column(name = "permission_id")
    private UUID permissionId;

    @Column(name = "action_restriction_reason_id")
    private UUID actionRestrictionReasonId;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TwinClassEntity twinClass;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PermissionEntity permission;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ActionRestrictionReasonEntity actionRestrictionReason;
}
