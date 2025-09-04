package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinflowFactoryCreateRqV1")
public class TwinflowFactoryCreateRqDTOv1 extends TwinflowFactorySaveRqDTOv1 {}
