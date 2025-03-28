package org.twins.core.dto.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.user.UserStatus;
import org.twins.core.dto.rest.twin.TwinSearchDTOv1;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UserSearchV1")
public class UserSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> userIdList;

    @Schema(description = "id exclude list")
    public Set<UUID> userIdExcludeList;

    @Schema(description = "name list")
    public Set<String> userNameLikeList;

    @Schema(description = "name exclude list")
    public Set<String> userNameLikeExcludeList;

    @Schema(description = "status id list")
    public Set<UserStatus> statusIdList;

    @Schema(description = "status id exclude list")
    public Set<UserStatus> statusIdExcludeList;

    @Schema(description = "space list")
    public List<SpaceSearchDTOv1> spaceList;

    @Schema(description = "space exclude list")
    public List<SpaceSearchDTOv1> spaceExcludeList;

    @Schema(description = "child twins")
    public TwinSearchDTOv1 childTwins;
}
