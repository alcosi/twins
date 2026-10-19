package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryBranchDuplicateV1")
public class FactoryBranchDuplicateDTOv1 {
    @Schema(description = "original factory branch id")
    public UUID originalFactoryBranchId;

    @Schema(description = "[optional] fill if branch should be copied to other factory")
    public UUID newTwinFactoryId;
}
