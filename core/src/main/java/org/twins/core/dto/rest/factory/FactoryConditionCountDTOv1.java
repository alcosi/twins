package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "FactoryConditionCountV1")
public class FactoryConditionCountDTOv1 extends CountDTOv1 {
    @Schema(description = "factory condition set id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = FactoryConditionSetDTOv1.class, name = "factoryConditionSet")
    public UUID factoryConditionSetId;

    @Schema(description = "conditioner featurer id")
    @RelatedObject(type = FeaturerDTOv1.class, name = "conditionerFeaturer")
    public Integer conditionerFeaturerId;

    @Schema(description = "invert flag")
    public Boolean invert;

    @Schema(description = "active flag")
    public Boolean active;
}
