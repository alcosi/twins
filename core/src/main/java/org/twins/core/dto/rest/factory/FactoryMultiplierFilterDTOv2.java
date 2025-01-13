package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "FactoryV2")
public class FactoryMultiplierFilterDTOv2 extends FactoryMultiplierFilterDTOv1 {
    @Schema(description = "multiplier")
    public FactoryMultiplierDTOv2 multiplier;

    @Schema(description = "factory condition set")
    public FactoryConditionSetDTOv1 factoryConditionSet;
}
