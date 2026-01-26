package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryMultiplierViewRsV1")
public class FactoryMultiplierViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "result - factory multiplier")
    public FactoryMultiplierDTOv1 multiplier;
}
