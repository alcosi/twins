package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.attachment.AttachmentCreateDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "CommentCreateV1")
public class CommentCreateDTOv1 {
    @NotNull
    @Schema(name = "text")
    public String text;

    @Schema(description = "attachments")
    public List<AttachmentCreateDTOv1> attachments;
}
