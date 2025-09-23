package org.twins.core.dto.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.enums.user.UserStatus;
import org.twins.core.dto.rest.twin.TwinSearchListDTOv1;

import java.util.*;

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

    @Schema(description = "email list")
    public Set<String> userEmailLikeList;

    @Schema(description = "email exclude list")
    public Set<String> userEmailLikeExcludeList;

    @Schema(description = "status id list")
    public Set<UserStatus> statusIdList;

    @Schema(description = "status id exclude list")
    public Set<UserStatus> statusIdExcludeList;

    @Schema(description = "space list")
    public List<SpaceSearchDTOv1> spaceList;

    @Schema(description = "space exclude list")
    public List<SpaceSearchDTOv1> spaceExcludeList;

    @Schema(description = "child twins searches")
    public TwinSearchListDTOv1 childTwinSearches;

    public UserSearchDTOv1 addUserIdListItem(UUID item) {
        CollectionUtils.safeAdd(userIdList, item);
        return this;
    }

    public UserSearchDTOv1 addUserIdExcludeListItem(UUID item) {
        CollectionUtils.safeAdd(userIdExcludeList, item);
        return this;
    }

    public UserSearchDTOv1 addUserNameLikeListItem(String item) {
        CollectionUtils.safeAdd(userNameLikeList, item);
        return this;
    }

    public UserSearchDTOv1 addUserNameLikeExcludeListItem(String item) {
        CollectionUtils.safeAdd(userNameLikeExcludeList, item);
        return this;
    }

    public UserSearchDTOv1 addUserEmailLikeListItem(String item) {
        CollectionUtils.safeAdd(userEmailLikeList, item);
        return this;
    }

    public UserSearchDTOv1 addUserEmailLikeExcludeListItem(String item) {
        CollectionUtils.safeAdd(userEmailLikeExcludeList, item);
        return this;
    }

    public UserSearchDTOv1 addStatusIdListItem(UserStatus item) {
        CollectionUtils.safeAdd(statusIdList, item);
        return this;
    }

    public UserSearchDTOv1 addStatusIdExcludeListItem(UserStatus item) {
        CollectionUtils.safeAdd(statusIdExcludeList, item);
        return this;
    }

    public UserSearchDTOv1 addSpaceListItem(SpaceSearchDTOv1 item) {
        CollectionUtils.safeAdd(spaceList, item);
        return this;
    }

    public UserSearchDTOv1 addSpaceExcludeListItem(SpaceSearchDTOv1 item) {
        CollectionUtils.safeAdd(spaceExcludeList, item);
        return this;
    }

}
