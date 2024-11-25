package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.attachment.AttachmentProblem;

import java.util.Set;

@Data
@Accessors(chain = true)
@Schema(name = "TwinAttachmentProblemsV1")
public class TwinAttachmentProblemsDTOv1 {

    @Schema(description = "Twin attachment problems")
    public Set<AttachmentProblem> problems;

}
