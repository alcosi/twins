package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.IntegerRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactorySearchDTOv1")
public class FactorySearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "key like list")
    public Set<String> keyLikeList;

    @Schema(description = "key not like list")
    public Set<String> keyNotLikeList;

    @Schema(description = "name like list")
    public Set<String> nameLikeList;

    @Schema(description = "name not like list")
    public Set<String> nameNotLikeList;

    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "description not like list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "Filter by factory pipelines count (range: from, to)")
    public IntegerRangeDTOv1 factoryPipelinesCountRange;

    @Schema(description = "Filter by factory multipliers count (range: from, to)")
    public IntegerRangeDTOv1 factoryMultipliersCountRange;

    @Schema(description = "Filter by factory branches count (range: from, to)")
    public IntegerRangeDTOv1 factoryBranchesCountRange;

    @Schema(description = "Filter by factory erasers count (range: from, to)")
    public IntegerRangeDTOv1 factoryErasersCountRange;
}
