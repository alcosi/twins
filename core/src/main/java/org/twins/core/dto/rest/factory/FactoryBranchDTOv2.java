package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "FactoryBranchV2")
public class FactoryBranchDTOv2 extends FactoryBranchDTOv1 {
    @Schema(description = "factory")
    public FactoryDTOv1 factory;

    @Schema(description = "factory condition set")
    public FactoryConditionSetDTOv1 factoryConditionSet;

    @Schema(description = "nex factory")
    public FactoryDTOv1 nextFactory;
}
