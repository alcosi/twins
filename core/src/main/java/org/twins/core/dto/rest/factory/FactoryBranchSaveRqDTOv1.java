package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryBranchSaveRqV1")
public class FactoryBranchSaveRqDTOv1 extends Request {
    @Schema(description = "factory condition set id", example = DTOExamples.FACTORY_CONDITION_SET_ID)
    public UUID factoryConditionSetId;

    @Schema(description = "factory condition set invert", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean factoryConditionSetInvert;

    @Schema(description = "factory condition set invert", example = DTOExamples.BOOLEAN_TRUE)
    public boolean active;

    @Schema(description = "next factory id", example = DTOExamples.FACTORY_ID)
    public UUID nextFactoryId;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;
}
