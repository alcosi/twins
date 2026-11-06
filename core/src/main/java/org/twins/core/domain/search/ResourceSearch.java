package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.resource.ResourceEntity;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class ResourceSearch extends EntitySearch<ResourceEntity> {
    private Set<UUID> idList;
    private Set<String> originalFileNameLikeList;
    private Set<UUID> uploadedByUserIdList;
}
