package org.twins.core.domain.comment;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.domain.EntityCUD;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class CommentUpdate {
    private UUID id;
    private UUID twinId;
    private String comment;
    private EntityCUD<TwinAttachmentEntity> cudAttachments;
}
