package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.attachment.AttachmentCudDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "CommentUpdateV1")
public class CommentUpdateDTOv1 extends CommentSaveDTOv1 {
    @Schema(description = "AttachmentCudV1")
    public AttachmentCudDTOv1 attachments;
}
