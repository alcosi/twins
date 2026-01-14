package org.twins.core.dao.attachment;

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
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_attachment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinAttachmentAction twinAttachmentAction;

    @Column(name = "permission_id")
    private UUID permissionId;
}
