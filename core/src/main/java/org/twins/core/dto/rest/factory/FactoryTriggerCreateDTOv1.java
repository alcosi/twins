package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinFactoryTriggerCreateV1")
public class FactoryTriggerCreateDTOv1 extends FactoryTriggerSaveDTOv1 {
}
