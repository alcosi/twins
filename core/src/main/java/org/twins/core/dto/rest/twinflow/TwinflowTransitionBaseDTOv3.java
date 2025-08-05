package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "TwinflowTransitionBaseV3")
public class TwinflowTransitionBaseDTOv3 extends TwinflowTransitionBaseDTOv2 {
}
