package org.twins.core.dao.comment;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.annotation.DataScope;
import org.twins.core.enums.comment.TwinCommentAction;
import org.twins.core.enums.datascope.DataScopeType;

import java.util.UUID;

@Data
@Entity
@Table(name = "twin_comment_action_alien_permission")
@DataScope({DataScopeType.SYSTEM, DataScopeType.DOMAIN})
public class TwinCommentActionAlienPermissionEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_comment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinCommentAction twinCommentAction;

    @Column(name = "permission_id")
    private UUID permissionId;

}
