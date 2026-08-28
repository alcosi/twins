package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "FactoryBranchCountV1")
public class FactoryBranchCountDTOv1 extends CountDTOv1 {
    @Schema(description = "factory id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = FactoryDTOv1.class, name = "factory")
    public UUID factoryId;

    @Schema(description = "factory condition set id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = FactoryConditionSetDTOv1.class, name = "factoryConditionSet")
    public UUID factoryConditionSetId;

    @Schema(description = "next factory id", example = DTOExamples.UUID_ID)
    @RelatedObject(type = FactoryDTOv1.class, name = "nextFactory")
    public UUID nextFactoryId;

    @Schema(description = "active flag")
    public Boolean active;

    @Schema(description = "factory condition set invert flag")
    public Boolean factoryConditionSetInvert;
}
