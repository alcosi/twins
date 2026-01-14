package org.twins.core.dao.attachment;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
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
        if (id == null) {
            this.id = UuidCreator.getTimeOrderedEpoch();
        }
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_attachment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinAttachmentAction twinAttachmentAction;

    @Column(name = "permission_id")
    private UUID permissionId;
}
