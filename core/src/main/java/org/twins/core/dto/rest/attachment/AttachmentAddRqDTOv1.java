package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "AttachmentAddRqV1")
public class AttachmentAddRqDTOv1 extends Request {
    @Schema(description = "Attachments list")
    public List<AttachmentAddDTOv1> attachments;
}
