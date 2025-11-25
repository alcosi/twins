package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryMultiplierRsV1")
public class FactoryMultiplierRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - factory multiplier")
    public FactoryMultiplierDTOv1 factoryMultiplier;
}
