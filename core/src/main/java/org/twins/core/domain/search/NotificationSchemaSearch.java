package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class NotificationSchemaSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<String> nameLikeList;
    private Set<String> nameNotLikeList;
    private Set<UUID> createdByUserIdList;
    private Set<UUID> createdByUserIdExcludeList;
}
