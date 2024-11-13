package org.twins.core.dao.comment;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "twin_comment_action_alien_permission")
public class TwinCommentActionAlienPermissionEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_comment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinCommentAction twinCommentAction;

    @Column(name = "permission_id")
    private UUID permissionId;

}
