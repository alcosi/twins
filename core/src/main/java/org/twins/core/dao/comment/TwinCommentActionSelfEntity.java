package org.twins.core.dao.comment;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.common.util.UuidUtils;
import org.twins.core.enums.comment.TwinCommentAction;

import java.util.UUID;

@Entity
@Data
@Table(name = "twin_comment_action_self")
public class TwinCommentActionSelfEntity {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "restrict_twin_comment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinCommentAction restrictTwinCommentAction;
}
