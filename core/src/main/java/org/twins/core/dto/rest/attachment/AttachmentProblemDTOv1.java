package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
//@Schema(name = "AttachmentProblemDTOv1")
public abstract class AttachmentProblemDTOv1 {

    @Schema(description = "Problem message")
    public String message;

}
