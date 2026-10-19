package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryMultiplierDuplicateV1")
public class FactoryMultiplierDuplicateDTOv1 {
    @Schema(description = "original factory multiplier id")
    public UUID originalFactoryMultiplierId;

    @Schema(description = "[optional] fill if multiplier should be copied to other factory")
    public UUID newTwinFactoryId;

    @Schema(description = "[optional] duplicate filters")
    public boolean duplicateFilters = true;
}
