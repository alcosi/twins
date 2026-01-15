package org.twins.core.dao.attachment;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.common.util.UuidUtils;
import org.twins.core.enums.attachment.TwinAttachmentAction;

import java.util.UUID;

@Data
@Entity
@Table(name = "twin_attachment_action_alien_permission")
public class TwinAttachmentActionAlienPermissionEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
            id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_attachment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinAttachmentAction twinAttachmentAction;

    @Column(name = "permission_id")
    private UUID permissionId;
}
