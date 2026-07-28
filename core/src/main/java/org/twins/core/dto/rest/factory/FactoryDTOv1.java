package org.twins.core.dto.rest.factory;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryV1")
public class FactoryDTOv1 {
    @Schema(description = "id", example = DTOExamples.FACTORY_ID)
    public UUID id;

    @Schema(description = "key", example = DTOExamples.FACTORY_KEY)
    public String key;

    @Schema(description = "name", example = DTOExamples.NAME)
    public String name;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "created by user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "createdByUser")
    public UUID createdByUserId;

    @Schema(description = "factory usages count", example = DTOExamples.COUNT)
    public Integer factoryUsagesCount;

    @Schema(description = "factory pipelines count", example = DTOExamples.COUNT)
    public Integer factoryPipelinesCount;

    @Schema(description = "factory multipliers count", example = DTOExamples.COUNT)
    public Integer factoryMultipliersCount;

    @Schema(description = "factory branches count", example = DTOExamples.COUNT)
    public Integer factoryBranchesCount;

    @Schema(description = "factory erasers count", example = DTOExamples.COUNT)
    public Integer factoryErasersCount;

    @Schema(description = "pipeline id list.")
    @RelatedObject(type = FactoryPipelineDTOv1.class, name = "pipelines")
    public Set<UUID> pipelineIdList;

    @Schema(description = "branch id list.")
    @RelatedObject(type = FactoryBranchDTOv1.class, name = "branches")
    public Set<UUID> branchIdList;

    @Schema(description = "multiplier id list.")
    @RelatedObject(type = FactoryMultiplierDTOv1.class, name = "multipliers")
    public Set<UUID> multiplierIdList;

    @Schema(description = "condition set id list.")
    @RelatedObject(type = FactoryConditionSetDTOv1.class, name = "conditionSets")
    public Set<UUID> conditionSetIdList;

    @Schema(description = "eraser id list.")
    @RelatedObject(type = FactoryEraserDTOv1.class, name = "erasers")
    public Set<UUID> eraserIdList;

    @Schema(description = "trigger id list.")
    @RelatedObject(type = FactoryTriggerDTOv1.class, name = "triggers")
    public Set<UUID> triggerIdList;
}


