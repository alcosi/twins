package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleDTOv1;
import org.twins.core.dto.rest.permission.PermissionSchemaDTOv1;
import org.twins.core.dto.rest.space.SpaceRoleDTOv2;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;


@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FactoryPipelineV2")
public class FactoryPipelineDTOv2 extends FactoryPipelineDTOv1 {
    @Schema(description = "factory")
    public FactoryDTOv1 factory;

    @Schema(description = "input twin class")
    public TwinClassBaseDTOv1 inputTwinClass;

    @Schema(description = "factory condition set")
    public FactoryConditionSetDTOv1 factoryConditionSet;

    @Schema(description = "output twin status")
    public TwinStatusDTOv1 outputTwinStatus;

    @Schema(description = "next factory")
    public FactoryDTOv1 nextFactory;
}
