package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinflowBaseV3")
public class TwinflowBaseDTOv3 extends TwinflowBaseDTOv2 {
    @Schema(description = "transitions map")
    public Map<UUID, TwinflowTransitionBaseDTOv2> transitions;
}
