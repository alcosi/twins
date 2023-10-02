package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserGroupMemberManageRqV1")
public class UserGroupMemberManageRqDTOv1 extends Request {
    @Schema()
    public List<UUID> userGroupEnterList;

    @Schema()
    public List<UUID> userGroupExitList;
}
