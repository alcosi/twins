package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.attachment.AttachmentAddDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "CommentCreateV1")
public class CommentCreateDTOv1 extends CommentSaveDTOv1 {
    @Schema(description = "attachments")
    public List<AttachmentAddDTOv1> attachments;
}
