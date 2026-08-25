package org.twins.core.dto.rest.featurer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@Accessors(chain = true)
@Schema(name = "FeaturerTypeSearchV1")
public class FeaturerTypeSearchDTOv1 {
    @Schema(description = "featurer type id list")
    public Set<Integer> idList;

    @Schema(description = "featurer type id exclude list")
    public Set<Integer> idExcludeList;

    @Schema(description = "featurer type name like list")
    public Set<String> nameLikeList;

    @Schema(description = "featurer type name not like list")
    public Set<String> nameNotLikeList;

    @Schema(description = "featurer type description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "featurer type description not like list")
    public Set<String> descriptionNotLikeList;
}
