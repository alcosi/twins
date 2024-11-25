package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.attachment.AttachmentProblem;
import org.twins.core.dto.rest.Request;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinAttachmentProblemsV1")
public class TwinAttachmentProblemsDTOv1 extends Request {

    @Schema(description = "Twin attachment problems")
    public Set<AttachmentProblem> problems;

}
