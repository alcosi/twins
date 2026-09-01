package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ProjectionTypeGroupSearchV1")
public class ProjectionTypeGroupSearchDTOv1 {
    @Schema(description = "projection type group id list")
    public Set<UUID> idList;

    @Schema(description = "projection type group id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "projection type group key like list")
    public Set<String> keyLikeList;

    @Schema(description = "projection type group key not like list")
    public Set<String> keyNotLikeList;
}
