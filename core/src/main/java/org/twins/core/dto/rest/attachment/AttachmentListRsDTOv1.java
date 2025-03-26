package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "AttachmentListRsV1")
public class AttachmentListRsDTOv1 extends Response {
    @Schema(description = "attachment list")
    public List<AttachmentDTOv1> attachments;
}
