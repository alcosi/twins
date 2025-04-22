package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.attachment.AttachmentCudDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "CommentUpdateV1")
public class CommentUpdateDTOv1 extends CommentSaveDTOv1 {
    @Schema(description = "comment id", example = DTOExamples.COMMENT_ID)
    public UUID id;

    @Schema(description = "attachments")
    public AttachmentCudDTOv1 attachments;
}
