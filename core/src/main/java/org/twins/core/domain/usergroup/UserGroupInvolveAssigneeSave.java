package org.twins.core.domain.usergroup;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class UserGroupInvolveAssigneeSave {
    private UUID userGroupId;
    private UUID propagationByTwinClassId;
    private UUID propagationByTwinStatusId;
}
