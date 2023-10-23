package org.twins.core.dto.rest.link;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinLinkBaseV1")
public class TwinLinkBaseDTOv1 extends Request {
    @Schema(description = "Link id", example = DTOExamples.LINK_ID)
    public UUID linkId;

    @Schema(description = "Source twin id", example = DTOExamples.TWIN_ID)
    public UUID srcTwinID;

    @Schema(description = "Destination twin id", example = DTOExamples.TWIN_ID)
    public UUID dstTwinId;
}
