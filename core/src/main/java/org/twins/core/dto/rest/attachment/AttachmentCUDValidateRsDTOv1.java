package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "AttachmentCUDValidateRsV1")
public class AttachmentCUDValidateRsDTOv1 extends Response {

    @Schema(description = "Twin attachment problems")
    public AttachmentCUDProblemsDTOv1 cudProblems;

    @Schema(description = "Attachment entities for update and delete operations")
    public List<AttachmentViewDTOv2> attachmentsForUD;
}
