package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryBranchV1")
public class FactoryBranchDTOv1 {
    @Schema(description = "id", example = DTOExamples.FACTORY_BRANCH_ID)
    public UUID id;

    @Schema(description = "factory id", example = DTOExamples.FACTORY_ID)
    public UUID factoryId;

    @Schema(description = "factory condition set id", example = DTOExamples.FACTORY_CONDITION_SET_ID)
    public UUID factoryConditionSetId;

    @Schema(description = "factory condition set invert", example = DTOExamples.BOOLEAN_TRUE)
    public boolean factoryConditionSetInvert;

    @Schema(description = "active", example = DTOExamples.BOOLEAN_TRUE)
    public boolean active;

    @Schema(description = "next factory id", example = DTOExamples.FACTORY_ID)
    public UUID nextFactoryId;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;
}
