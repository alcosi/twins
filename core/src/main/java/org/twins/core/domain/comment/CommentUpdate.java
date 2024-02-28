package org.twins.core.domain.comment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinCommentEntity;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class CommentUpdate extends TwinCommentEntity {
    public AttachmentCud attachments;
}
