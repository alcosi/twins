package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinFactoryTriggerSaveV1")
public class FactoryTriggerSaveDTOv1 {
    @Schema(description = "twin factory id", example = DTOExamples.FACTORY_ID)
    public UUID twinFactoryId;

    @Schema(description = "input twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID inputTwinClassId;

    @Schema(description = "twin factory condition set id")
    public UUID twinFactoryConditionSetId;

    @Schema(description = "twin factory condition invert")
    public Boolean twinFactoryConditionInvert;

    @Schema(description = "active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "twin trigger id", example = DTOExamples.TRIGGER_ID)
    public UUID twinTriggerId;

    @Schema(description = "async")
    public Boolean async;
}
