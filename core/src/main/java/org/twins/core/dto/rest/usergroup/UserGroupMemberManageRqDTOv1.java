package org.twins.core.dto.rest.usergroup;

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
@Schema(name = "UserGroupMemberManageRqV1")
public class UserGroupMemberManageRqDTOv1 extends Request {
    @Schema()
    public Set<UUID> userGroupEnterList;

    @Schema()
    public Set<UUID> userGroupExitList;
}
