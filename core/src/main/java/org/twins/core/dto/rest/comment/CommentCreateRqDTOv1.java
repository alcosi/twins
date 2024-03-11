package org.twins.core.dto.rest.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.attachment.AttachmentAddDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "CommentCreateRqV1")
public class CommentCreateRqDTOv1 extends CommentBaseDTOv1 {
    public List<AttachmentAddDTOv1> attachments;
}
