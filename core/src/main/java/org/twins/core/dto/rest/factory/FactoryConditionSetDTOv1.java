package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "FactoryConditionSetV1")
public class FactoryConditionSetDTOv1 {
    @Schema(description = "id", example = DTOExamples.FACTORY_CONDITION_SET_ID)
    public UUID id;

    @Schema(description = "name", example = DTOExamples.NAME)
    public String name;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "count pipeline usages", example = DTOExamples.COUNT)
    public Integer inFactoryPipelineUsagesCount;

    @Schema(description = "count pipeline step usages", example = DTOExamples.COUNT)
    public Integer inFactoryPipelineStepUsagesCount;

    @Schema(description = "count multiplier usages", example = DTOExamples.COUNT)
    public Integer inFactoryMultiplierUsagesCount;

    @Schema(description = "count multiplier filter usages", example = DTOExamples.COUNT)
    public Integer inFactoryMultiplierFilterUsagesCount;

    @Schema(description = "count branch usages", example = DTOExamples.COUNT)
    public Integer inFactoryBranchUsagesCount;

    @Schema(description = "count eraser usages", example = DTOExamples.COUNT)
    public Integer inFactoryEraserUsagesCount;
}
