package org.twins.core.dto.rest.featurer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;

@Data
@Accessors(chain = true)
@Schema(name = "FeaturerSearchRqV1")
public class FeaturerSearchRqDTOv1 {
    @Schema(description = "ids list")
    public Set<Integer> idList;

    @Schema(description = "type ids list")
    public Set<String> typeIdList;

    @Schema(description = "names list")
    public Set<String> nameLikeList;
}
