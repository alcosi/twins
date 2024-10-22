package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionSearchRqV1")
public class PermissionSearchRqDTOv1 extends Request {
    @Schema(description = "user id list")
    public Set<UUID> idList;

    @Schema(description = "user id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "key like list")
    public Set<String> keyLikeList;

    @Schema(description = "ley not like list")
    public Set<String> keyNotLikeList;

    @Schema(description = "name like list")
    public Set<String> nameLikeList;

    @Schema(description = "name not like list")
    public Set<String> nameNotLikeList;

    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;

    @Schema(description = "description not like list")
    public Set<String> descriptionNotLikeList;

    @Schema(description = "group id list")
    public Set<UUID> groupIdList;

    @Schema(description = "group id exclude list")
    public Set<UUID> groupIdExcludeList;
}
