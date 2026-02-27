package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinStatusTransitionTriggerCreateRsV1")
public class TwinStatusTransitionTriggerCreateRsDTOv1 extends TwinStatusTransitionTriggerListRsDTOv1 {
}
