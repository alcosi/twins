package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassSchemaSearchV1")
public class TwinClassSchemaSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "domain id list")
    public Set<UUID> domainIdList;

    @Schema(description = "domain id exclude list")
    public Set<UUID> domainIdExcludeList;

    @Schema(description = "name like list")
    public Set<String> nameLikeList;

    @Schema(description = "name not like list")
    public Set<String> nameNotLikeList;

    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "description not like list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "created by user id list")
    public Set<UUID> createdByUserIdList;

    @Schema(description = "created by user id exclude list")
    public Set<UUID> createdByUserIdExcludeList;

    @Schema(description = "created at")
    public DataTimeRangeDTOv1 createdAt;
}