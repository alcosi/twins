package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.attachment.AttachmentCudDTOv1;

@Data
@Accessors(chain = true)
@Schema(name = "CommentUpdateV1")
public class CommentUpdateDTOv1 {
    @Schema(name = "text")
    public String text;

    @Schema(description = "attachments")
    public AttachmentCudDTOv1 attachments;
}
