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
public class TwinFieldValueSearchId extends TwinFieldValueSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;

    @Override
    public boolean isEmptySearch() {
        return CollectionUtils.isEmpty(idList)
                && CollectionUtils.isEmpty(idExcludeList);
    }
}

