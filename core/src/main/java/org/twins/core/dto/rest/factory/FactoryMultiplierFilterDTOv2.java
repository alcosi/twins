package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "FactoryMultiplierFilterV2")
public class FactoryMultiplierFilterDTOv2 extends FactoryMultiplierFilterDTOv1 {
    @Schema(description = "multiplier")
    public FactoryMultiplierDTOv2 multiplier;

    @Schema(description = "factory condition set")
    public FactoryConditionSetDTOv1 factoryConditionSet;

    @Schema(description = "input twin class")
    public TwinClassBaseDTOv1 inputTwinClass;
}
