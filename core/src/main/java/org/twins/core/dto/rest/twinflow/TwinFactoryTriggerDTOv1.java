package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.trigger.TwinTriggerDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinFactoryTriggerV1")
public class TwinFactoryTriggerDTOv1 {
    @Schema(description = "id", example = DTOExamples.TRIGGER_ID)
    public UUID id;

    @Schema(description = "twin factory id", example = DTOExamples.FACTORY_ID)
    @RelatedObject(type = FactoryDTOv1.class, name = "twinFactory")
    public UUID twinFactoryId;

    @Schema(description = "input twin class id", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "inputTwinClass")
    public UUID inputTwinClassId;

    @Schema(description = "twin factory condition set id")
    public UUID twinFactoryConditionSetId;

    @Schema(description = "twin factory condition invert", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean twinFactoryConditionInvert;

    @Schema(description = "active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "twin trigger id", example = DTOExamples.TRIGGER_ID)
    @RelatedObject(type = TwinTriggerDTOv1.class, name = "twinTrigger")
    public UUID twinTriggerId;

    @Schema(description = "async", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean async;
}
