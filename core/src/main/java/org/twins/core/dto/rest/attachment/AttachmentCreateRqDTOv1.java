package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "AttachmentCreateRqV1")
public class AttachmentCreateRqDTOv1 extends Request {
    @Schema(description = "attachments list")
    public List<AttachmentCreateDTOv1> attachments;
}
