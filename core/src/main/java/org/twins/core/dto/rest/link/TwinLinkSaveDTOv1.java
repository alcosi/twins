package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinLinkSaveV1")
public class TwinLinkSaveDTOv1 {
    @Schema(description = "link id", example = DTOExamples.LINK_ID)
    public UUID linkId;

    @Schema(description = "dst twin id", example = DTOExamples.TWIN_ID)
    public UUID dstTwinId;
}
