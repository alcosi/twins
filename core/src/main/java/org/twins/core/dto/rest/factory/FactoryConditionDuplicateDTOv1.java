package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryConditionDuplicateV1")
public class FactoryConditionDuplicateDTOv1 {
    @Schema(description = "original factory condition id")
    public UUID originalFactoryConditionId;

    @Schema(description = "[optional] fill if condition should be copied to other condition set")
    public UUID newTwinFactoryConditionSetId;
}
