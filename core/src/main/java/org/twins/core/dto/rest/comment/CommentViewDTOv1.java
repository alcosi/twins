package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.comment.TwinCommentAction;
import org.twins.core.dto.rest.attachment.AttachmentDTOv1;

import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "CommentViewV1")
public class CommentViewDTOv1 extends CommentBaseDTOv2 {
    @Schema(description = "attachments")
    public List<AttachmentDTOv1> attachments;

    @Schema(description = "comment actions")
    public Set<TwinCommentAction> commentActions;

}
