package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class UserGroupByAssigneePropagationSearch {
   Set<UUID> idList;
   Set<UUID> idExcludeList;
   Set<UUID> permissionSchemaIdList;
   Set<UUID> permissionSchemaIdExcludeList;
   Set<UUID> userGroupIdList;
   Set<UUID> userGroupIdExcludeList;
   Set<UUID> propagationTwinClassIdList;
   Set<UUID> propagationTwinClassIdExcludeList;
   Set<UUID> propagationTwinStatusIdList;
   Set<UUID> propagationTwinStatusIdExcludeList;
   Set<UUID> createdByUserIdList;
   Set<UUID> createdByUserIdExcludeList;
}
