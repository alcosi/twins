package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinReorderMoveV1")
public class TwinReorderMoveDTOv1 {
    @NotNull
    @Schema(description = "moved twin id: without linkId it is a head child (level is resolved from its headTwinId); "
            + "with linkId it is a src twin (twin_link row is resolved by (src, link))", example = DTOExamples.TWIN_ID)
    public UUID twinId;

    @Schema(description = "anchor twin id 'insert after', must belong to the same level; null/omitted means insert at the beginning of the level",
            example = DTOExamples.TWIN_ID)
    public UUID afterTwinId;

    @Schema(description = "scope discriminator: absent - head children level (twin.sort_order); "
            + "present - src twins of this link type (twin_link.sort_order, level resolved by (src, link))",
            example = DTOExamples.LINK_ID)
    public UUID linkId;
}
