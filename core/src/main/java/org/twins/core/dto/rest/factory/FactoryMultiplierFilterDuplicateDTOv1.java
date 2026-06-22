package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryMultiplierFilterDuplicateV1")
public class FactoryMultiplierFilterDuplicateDTOv1 {
    @Schema(description = "original factory multiplier filter id")
    public UUID originalFactoryMultiplierFilterId;

    @Schema(description = "[optional] fill if filter should be copied to other multiplier")
    public UUID newTwinFactoryMultiplierId;
}
