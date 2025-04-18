package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.attachment.AttachmentCreateDTOv1;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "TwinCreateRqV1")
public class TwinCreateRqDTOv1 extends Request {
    @Schema(description = "Class Id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID classId;

    @Schema(description = "Head twin id, if selected class had to be linked to some head twin", example = DTOExamples.HEAD_TWIN_ID)
    public UUID headTwinId;

    @Schema(description = "name", example = "Oak")
    public String name;

    @Schema(description = "assigner user id", example = DTOExamples.USER_ID)
    public UUID assignerUserId;

    @Schema(description = "description", example = "The biggest tree")
    public String description;

    @Schema(description = "external id")
    public String externalId;

    @Schema(description = "fields")
    public Map<String, TwinFieldValueDTO> fields;

    @Schema(description = "Attachments")
    public List<AttachmentCreateDTOv1> attachments;

    @Schema(description = "Links list")
    public List<TwinLinkAddDTOv1> links;
}
