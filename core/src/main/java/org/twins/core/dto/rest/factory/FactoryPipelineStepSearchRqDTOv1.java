package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "FactoryPipelineStepSearchRqV1")
public class FactoryPipelineStepSearchRqDTOv1 extends Request {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "factory id list")
    public Set<UUID> factoryIdList;

    @Schema(description = "factory id exclude list")
    public Set<UUID> factoryIdExcludeList;

    @Schema(description = "factory pipeline id list")
    public Set<UUID> factoryPipelineIdList;

    @Schema(description = "factory pipeline id exclude list")
    public Set<UUID> factoryPipelineIdExcludeList;

    @Schema(description = "factory condition set id list")
    public Set<UUID> factoryConditionSetIdList;

    @Schema(description = "factory condition set id exclude list")
    public Set<UUID> factoryConditionSetIdExcludeList;

    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "description not like list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "filler featurer id list")
    public Set<Integer> fillerFeaturerIdList;

    @Schema(description = "filler featurer id exclude list")
    public Set<Integer> fillerFeaturerIdExcludeList;

    @Schema(description = "condition invert", example = DTOExamples.TERNARY)
    public Ternary conditionInvert;

    @Schema(description = "active", example = DTOExamples.TERNARY)
    public Ternary active;

    @Schema(description = "optional", example = DTOExamples.TERNARY)
    public Ternary optional;
}
