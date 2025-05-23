package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.attachment.AttachmentCudDTOv1;

@Data
@Accessors(chain = true)
@Schema(name = "CommentSaveV1")
public class CommentSaveDTOv1 {
    @Schema(name = "text")
    public String text;
}
