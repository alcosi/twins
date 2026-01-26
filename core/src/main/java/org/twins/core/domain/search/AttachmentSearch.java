package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.math.LongRange;
import org.twins.core.domain.DataTimeRange;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class AttachmentSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> twinIdList;
    private Set<UUID> twinIdExcludeList;
    private Set<UUID> twinflowTransitionIdList;
    private Set<UUID> twinflowTransitionIdExcludeList;
    private Set<UUID> commentIdList;
    private Set<UUID> commentIdExcludeList;
    private Set<UUID> twinClassFieldIdList;
    private Set<UUID> twinClassFieldIdExcludeList;
    private Set<String> storageLinkLikeList;
    private Set<String> storageLinkNotLikeList;
    private Set<UUID> viewPermissionIdList;
    private Set<UUID> viewPermissionIdExcludeList;
    private Set<UUID> createdByUserIdList;
    private Set<UUID> createdByUserIdExcludeList;
    private Set<String> externalIdLikeList;
    private Set<String> externalIdNotLikeList;
    private Set<String> titleLikeList;
    private Set<String> titleNotLikeList;
    private Set<String> descriptionLikeList;
    private Set<String> descriptionNotLikeList;
    private DataTimeRange createdAt;
    private LongRange order;
}
