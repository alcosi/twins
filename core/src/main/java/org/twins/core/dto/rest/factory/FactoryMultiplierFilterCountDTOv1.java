package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "FactoryMultiplierFilterCountV1")
public class FactoryMultiplierFilterCountDTOv1 extends CountDTOv1 {
    @Schema(description = "factory multiplier id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = FactoryMultiplierDTOv1.class, name = "multiplier")
    public UUID factoryMultiplierId;

    @Schema(description = "input twin class id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "inputTwinClass")
    public UUID inputTwinClassId;

    @Schema(description = "factory condition set id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = FactoryConditionSetDTOv1.class, name = "factoryConditionSet")
    public UUID factoryConditionSetId;

    @Schema(description = "active flag")
    public Boolean active;

    @Schema(description = "factory condition set invert flag")
    public Boolean factoryConditionSetInvert;
}
