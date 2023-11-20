package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinLinkListV1")
public class TwinLinkListDTOv1  {
    @Schema(description = "links")
    public Map<UUID, TwinLinkViewDTOv1> forwardLinks;

    @Schema(description = "links")
    public Map<UUID, TwinLinkViewDTOv1> backwardLinks;
}
