package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserGroupMemberManageRqV1")
public class UserGroupMemberManageRqDTOv1 extends Request {
    @Schema()
    public Set<UUID> userGroupEnterList;

    @Schema()
    public Set<UUID> userGroupExitList;

    public UserGroupMemberManageRqDTOv1 addUserGroupEnterListItem(UUID item) {
        this.userGroupEnterList = CollectionUtils.safeAdd(this.userGroupEnterList, item);
        return this;
    }

    public UserGroupMemberManageRqDTOv1 addUserGroupExitListItem(UUID item) {
        this.userGroupExitList = CollectionUtils.safeAdd(this.userGroupExitList, item);
        return this;
    }

}
