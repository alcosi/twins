package org.twins.core.domain.space;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.service.pagination.PaginationResult;

import java.util.List;

@Data
@Accessors(chain = true)
public class UsersRefSpaceRolePageable extends PaginationResult {
    List<UserRefSpaceRole> usersRefRoles;
}
