package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowRsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryBranchCreateRsV1")
public class FactoryBranchCreateRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - factory branch")
    public FactoryBranchDTOv2 factoryBranch;
}
