package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryConditionSetDuplicateV1")
public class FactoryConditionSetDuplicateDTOv1 {
    @Schema(description = "original factory condition set id")
    public UUID originalFactoryConditionSetId;

    @Schema(description = "[optional] fill if condition set should be copied to other factory")
    public UUID newTwinFactoryId;

    @Schema(description = "[optional] duplicate condition set conditions")
    public boolean duplicateConditions = true;
}
