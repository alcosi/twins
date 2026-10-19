package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.IntegerRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryConditionSetSearchDTOv1")
public class FactoryConditionSetSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twin factory id list")
    public Set<UUID> twinFactoryIdList;

    @Schema(description = "twin factory id exclude list")
    public Set<UUID> twinFactoryIdExcludeList;

    @Schema(description = "name like list")
    public Set<String> nameLikeList;

    @Schema(description = "name like exclude list")
    public Set<String> nameNotLikeList;

    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "description like exclude list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "cachable", example = DTOExamples.TERNARY)
    public Ternary cachable;

    @Schema(description = "Filter by count in factory pipeline usages (range: from, to)")
    public IntegerRangeDTOv1 usageCountPipelineRange;

    @Schema(description = "Filter by count in factory pipeline step usages (range: from, to)")
    public IntegerRangeDTOv1 usageCountPipelineStepRange;

    @Schema(description = "Filter by count in factory multiplier filter usages (range: from, to)")
    public IntegerRangeDTOv1 usageCountMultiplierFilterRange;

    @Schema(description = "Filter by count in factory branch usages (range: from, to)")
    public IntegerRangeDTOv1 usageCountBranchRange;

    @Schema(description = "Filter by count in factory eraser usages (range: from, to)")
    public IntegerRangeDTOv1 usageCountEraserRange;

    @Schema(description = "Filter by count in factory trigger usages (range: from, to)")
    public IntegerRangeDTOv1 usageCountTriggerRange;
}
