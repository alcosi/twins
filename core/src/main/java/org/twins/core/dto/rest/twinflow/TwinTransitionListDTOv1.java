package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.link.TwinLinkViewDTOv1;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinTransitionListV1")
public class TwinTransitionListDTOv1{
    @Schema(description = "links")
    public Map<UUID, TwinLinkViewDTOv1> forwardLinks;

    @Schema(description = "links")
    public Map<UUID, TwinLinkViewDTOv1> backwardLinks;
}
