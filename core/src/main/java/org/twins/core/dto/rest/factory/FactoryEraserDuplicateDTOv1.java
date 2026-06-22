package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryEraserDuplicateV1")
public class FactoryEraserDuplicateDTOv1 {
    @Schema(description = "original factory eraser id")
    public UUID originalFactoryEraserId;

    @Schema(description = "[optional] fill if eraser should be copied to other factory")
    public UUID newTwinFactoryId;
}
