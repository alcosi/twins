package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinLinkAddV2")
public class TwinLinkAddDTOv2 {
    @Schema(description = "Link id", example = DTOExamples.LINK_ID)
    public UUID linkId;

    @Schema(description = "Destination twin id (can be UUID or temporalId:XXX reference)", example = DTOExamples.TWIN_ID)
    public String dstTwinId;
}
