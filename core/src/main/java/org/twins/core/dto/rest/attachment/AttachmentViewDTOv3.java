package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.comment.CommentBaseDTOv2;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "AttachmentViewV3")
public class AttachmentViewDTOv3 extends AttachmentViewDTOv2 {

    @Schema(description = "link to the comment to which attachment was added (if any)")
    public UUID commentId;

    @Schema(description = "comment")
    public CommentBaseDTOv2 comment;

    @Schema(name = "fieldKey", description = "key link to the field to which attachment was added (if any)")
    public String fieldKey;

}
