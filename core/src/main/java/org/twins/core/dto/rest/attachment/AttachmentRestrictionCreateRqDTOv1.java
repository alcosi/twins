package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "AttachmentRestrictionCreateRqV1")
public class AttachmentRestrictionCreateRqDTOv1 extends Request {
    @Valid
    @Schema(description = "attachment restrictions")
    public List<AttachmentRestrictionCreateDTOv1> attachmentRestrictions;
}
