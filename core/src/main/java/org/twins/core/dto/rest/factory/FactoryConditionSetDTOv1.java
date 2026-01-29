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

    @Schema(description = "created by user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "createdByUser")
    public UUID createdByUserId;

    @Schema(description = "twin factory id")
    public UUID twinFactoryId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "updated at", example = DTOExamples.INSTANT)
    public LocalDateTime updatedAt;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "count in factory pipeline usages", example = DTOExamples.COUNT)
    public Integer inFactoryPipelineUsagesCount;

    @Schema(description = "count in factory pipeline step usages", example = DTOExamples.COUNT)
    public Integer inFactoryPipelineStepUsagesCount;

    @Schema(description = "count in factory multiplier filter usages", example = DTOExamples.COUNT)
    public Integer inFactoryMultiplierFilterUsagesCount;

    @Schema(description = "count in factory branch usages", example = DTOExamples.COUNT)
    public Integer inFactoryBranchUsagesCount;

    @Schema(description = "count in factory eraser usages", example = DTOExamples.COUNT)
    public Integer inFactoryEraserUsagesCount;
}


