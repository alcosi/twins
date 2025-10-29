package org.twins.core.dto.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dto.rest.twin.TwinSearchListDTOv1;
import org.twins.core.enums.user.UserStatus;

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

    @Schema(description = "email list")
    public Set<String> userEmailLikeList;

    @Schema(description = "email exclude list")
    public Set<String> userEmailLikeExcludeList;

    @Schema(description = "name or email list")
    public Set<String> userNameOrEmailLikeList;

    @Schema(description = "name or email exclude list")
    public Set<String> userNameOrEmailLikeExcludeList;

    @Schema(description = "user group id list")
    public Set<UUID> userGroupIdList;

    @Schema(description = "user group id exclude list")
    public Set<UUID> userGroupIdExcludeList;

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
        this.userIdList = CollectionUtils.safeAdd(this.userIdList, item);
        return this;
    }

    public UserSearchDTOv1 addUserIdExcludeListItem(UUID item) {
        this.userIdExcludeList = CollectionUtils.safeAdd(this.userIdExcludeList, item);
        return this;
    }

    public UserSearchDTOv1 addUserNameLikeListItem(String item) {
        this.userNameLikeList = CollectionUtils.safeAdd(this.userNameLikeList, item);
        return this;
    }

    public UserSearchDTOv1 addUserNameLikeExcludeListItem(String item) {
        this.userNameLikeExcludeList = CollectionUtils.safeAdd(this.userNameLikeExcludeList, item);
        return this;
    }

    public UserSearchDTOv1 addUserEmailLikeListItem(String item) {
        this.userEmailLikeList = CollectionUtils.safeAdd(this.userEmailLikeList, item);
        return this;
    }

    public UserSearchDTOv1 addUserEmailLikeExcludeListItem(String item) {
        this.userEmailLikeExcludeList = CollectionUtils.safeAdd(this.userEmailLikeExcludeList, item);
        return this;
    }

    public UserSearchDTOv1 addUserNameOrEmailLikeListItem(String item) {
        this.userNameOrEmailLikeList = CollectionUtils.safeAdd(this.userNameOrEmailLikeList, item);
        return this;
    }

    public UserSearchDTOv1 addUserNameOrEmailLikeExcludeListItem(String item) {
        this.userNameOrEmailLikeExcludeList = CollectionUtils.safeAdd(this.userNameOrEmailLikeExcludeList, item);
        return this;
    }

    public UserSearchDTOv1 addUserGroupIdListItem(UUID item) {
        this.userGroupIdList = CollectionUtils.safeAdd(this.userGroupIdList, item);
        return this;
    }

    public UserSearchDTOv1 addUserGroupIdExcludeListItem(UUID item) {
        this.userGroupIdExcludeList = CollectionUtils.safeAdd(this.userGroupIdExcludeList, item);
        return this;
    }

    public UserSearchDTOv1 addStatusIdListItem(UserStatus item) {
        this.statusIdList = CollectionUtils.safeAdd(this.statusIdList, item);
        return this;
    }

    public UserSearchDTOv1 addStatusIdExcludeListItem(UserStatus item) {
        this.statusIdExcludeList = CollectionUtils.safeAdd(this.statusIdExcludeList, item);
        return this;
    }

    public UserSearchDTOv1 addSpaceListItem(SpaceSearchDTOv1 item) {
        this.spaceList = CollectionUtils.safeAdd(this.spaceList, item);
        return this;
    }

    public UserSearchDTOv1 addSpaceExcludeListItem(SpaceSearchDTOv1 item) {
        this.spaceExcludeList = CollectionUtils.safeAdd(this.spaceExcludeList, item);
        return this;
    }

}
