package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.attachment.AttachmentAddDTOv1;
import org.twins.core.dto.rest.attachment.AttachmentUpdateDTOv1;
import org.twins.core.dto.rest.link.TwinLinkAddDTOv1;
import org.twins.core.dto.rest.link.TwinLinkUpdateDTOv1;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinUpdateRqV1")
public class TwinUpdateRqDTOv1 extends TwinUpdateDTOv1 {
    @Schema
    public String comment;
}
