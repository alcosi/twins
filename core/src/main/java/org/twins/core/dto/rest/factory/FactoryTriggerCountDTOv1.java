package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.trigger.TwinTriggerDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "TwinFactoryTriggerCountV1")
public class FactoryTriggerCountDTOv1 extends CountDTOv1 {
    @Schema(description = "twin factory id", example = DTOExamples.FACTORY_ID)
    @RelatedObject(type = FactoryDTOv1.class, name = "twinFactory")
    public UUID twinFactoryId;

    @Schema(description = "input twin class id", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "inputTwinClass")
    public UUID inputTwinClassId;

    @Schema(description = "twin trigger id", example = DTOExamples.TRIGGER_ID)
    @RelatedObject(type = TwinTriggerDTOv1.class, name = "twinTrigger")
    public UUID twinTriggerId;

    @Schema(description = "is active")
    public Boolean active;

    @Schema(description = "is async")
    public Boolean async;

    @Schema(description = "twin factory condition invert")
    public Boolean twinFactoryConditionInvert;
}
