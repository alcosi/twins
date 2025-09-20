package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinflowFactoryUpdatesV1")
public class TwinflowFactoryUpdateRsDTOv1 extends TwinflowFactoryRsDTOv1 {}
