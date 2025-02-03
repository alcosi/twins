package org.twins.core.dao.comment;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.kit.Kit;
import org.hibernate.annotations.UpdateTimestamp;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@Table(name = "twin_comment")
@Accessors(chain = true)
@FieldNameConstants
public class TwinCommentEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "text")
    private String text;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "changed_at")
    private Timestamp changedAt;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinAttachmentEntity, UUID> attachmentKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Set<TwinCommentAction> commentActions;
}
