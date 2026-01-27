package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.domain.DataTimeRange;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinClassSchemaSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> domainIdList;
    private Set<UUID> domainIdExcludeList;
    private Set<String> nameLikeList;
    private Set<String> nameNotLikeList;
    private Set<String> descriptionLikeList;
    private Set<String> descriptionNotLikeList;
    private Set<UUID> createdByUserIdList;
    private Set<UUID> createdByUserIdExcludeList;
    private DataTimeRange createdAt;
}