package org.twins.core.dao.action;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.common.util.UuidUtils;
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
}
