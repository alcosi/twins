package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "ProjectionTypeSearchV1")
public class ProjectionTypeSearchDTOv1 {
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

    @Schema(description = "projection type group id list")
    public Set<UUID> projectionTypeGroupIdList;

    @Schema(description = "projection type group id exclude list")
    public Set<UUID> projectionTypeGroupIdExcludeList;

    @Schema(description = "membership twin class id list")
    public Set<UUID> membershipTwinClassIdList;

    @Schema(description = "membership twin class id exclude list")
    public Set<UUID> membershipTwinClassIdExcludeList;
}
