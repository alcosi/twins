
package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.attachment.AttachmentBaseDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinLinkUpdateV1")
public class TwinLinkUpdateDTOv1 {
    @Schema(description = "id", example = DTOExamples.LINK_ID)
    public UUID id;

    @Schema(description = "Destination twin id", example = DTOExamples.TWIN_ID)
    public UUID dstTwinId;
}
