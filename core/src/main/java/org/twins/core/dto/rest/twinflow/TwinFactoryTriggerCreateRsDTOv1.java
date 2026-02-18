package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinFactoryTriggerCreateRsV1")
public class TwinFactoryTriggerCreateRsDTOv1 extends TwinFactoryTriggerListRsDTOv1 {
}
