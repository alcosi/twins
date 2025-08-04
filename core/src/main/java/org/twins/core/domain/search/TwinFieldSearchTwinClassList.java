package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinFieldSearchTwinClassList extends TwinFieldSearch {

    public Set<UUID> idIncludeAllSet;
    public Set<UUID> idExcludeAllSet;
    public Set<UUID> idIncludeAnySet;
    public Set<UUID> idExcludeAnySet;

    public boolean isEmptySearch() {
        return CollectionUtils.isEmpty(idIncludeAllSet) &&
                CollectionUtils.isEmpty(idExcludeAllSet) &&
                CollectionUtils.isEmpty(idIncludeAnySet) &&
                CollectionUtils.isEmpty(idExcludeAnySet);
    }
}
