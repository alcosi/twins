package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinLinkAddV1")
public class TwinLinkAddDTOv1 {
    @Schema(description = "Link id", example = DTOExamples.LINK_ID)
    public UUID linkId;

    @Schema(description = "Destination twin id", example = DTOExamples.TWIN_ID)
    public UUID dstTwinId;

    @Schema(description = "Initial field values for the relation twin (relation attributes). "
            + "Only applicable when the link has relation_twin_class_id configured; "
            + "keys are relation-twin-class field ids or keys, same format as TwinCreate fields")
    public Map<String, String> relationTwinFields;
}
