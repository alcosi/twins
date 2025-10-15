package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinLinkBaseV1")
public class TwinLinkBaseDTOv1 extends Request {
    @Schema(description = "Link id", example = DTOExamples.LINK_ID)
    @RelatedObject(type = LinkDTOv1.class, name = "link")
    public UUID linkId;

    @Schema(description = "Destination twin id", example = DTOExamples.TWIN_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "dstTwin")
    public UUID dstTwinId;
}


