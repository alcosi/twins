package org.twins.core.dto.rest.featurer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Set;

@Data
@Accessors(chain = true)
@Schema(name = "FeaturerSearchV1")
public class FeaturerSearchDTOv1 {
    @Schema(description = "featurer id list")
    public Set<Integer> idList;

    @Schema(description = "featurer id exclude list")
    public Set<Integer> idExcludeList;

    @Schema(description = "featurer type id list")
    public Set<Integer> typeIdList;

    @Schema(description = "featurer type id exclude list")
    public Set<Integer> typeIdExcludeList;

    @Schema(description = "featurer name like list")
    public Set<String> nameLikeList;

    @Schema(description = "featurer name or id like list")
    public Set<String> nameOrIdLikeList;

    @Schema(description = "featurer name not like list")
    public Set<String> nameNotLikeList;

    @Schema(description = "featurer description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "featurer description not like list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "deprecated", example = DTOExamples.TERNARY)
    public Ternary deprecated;
}
