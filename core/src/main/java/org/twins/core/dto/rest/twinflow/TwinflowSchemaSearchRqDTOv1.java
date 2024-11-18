package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinflowSchemaSearchRqV1")
public class TwinflowSchemaSearchRqDTOv1 extends Request {

    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "name like list")
    public Set<String> nameLikeList;

    @Schema(description = "name not like list")
    public Set<String> nameNotLikeList;

    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "description not like list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "business account id list")
    public Set<UUID> businessAccountIdList;

    @Schema(description = "business account id exclude list")
    public Set<UUID> businessAccountIdExcludeList;

    @Schema(description = "user id list")
    public Set<UUID> createdByUserIdList;

    @Schema(description = "user id exclude list")
    public Set<UUID> createdByUserIdExcludeList;
}
