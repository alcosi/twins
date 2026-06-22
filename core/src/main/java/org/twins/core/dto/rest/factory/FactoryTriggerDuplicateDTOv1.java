package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryTriggerDuplicateV1")
public class FactoryTriggerDuplicateDTOv1 {
    @Schema(description = "original factory trigger id")
    public UUID originalFactoryTriggerId;

    @Schema(description = "[optional] fill if trigger should be copied to other factory")
    public UUID newTwinFactoryId;
}
