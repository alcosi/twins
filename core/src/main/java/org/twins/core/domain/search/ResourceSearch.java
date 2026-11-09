package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.math.LongRange;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.domain.DataTimeRange;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class ResourceSearch extends EntitySearch<ResourceEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<String> originalFileNameLikeList;
    private Set<String> originalFileNameNotLikeList;
    private Set<UUID> uploadedByUserIdList;
    private Set<UUID> uploadedByUserIdExcludeList;
    private Set<UUID> storageIdList;
    private Set<UUID> storageIdExcludeList;
    private Set<String> storageFileKeyLikeList;
    private Set<String> storageFileKeyNotLikeList;
    private LongRange sizeInBytes;
    private DataTimeRange createdAt;
}
