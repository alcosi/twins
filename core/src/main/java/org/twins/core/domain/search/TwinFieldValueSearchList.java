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
public class TwinFieldValueSearchList extends TwinFieldValueSearch {
    private Set<UUID> optionsAllOfList;
    private Set<UUID> optionsAnyOfList;
    private Set<UUID> optionsNoAllOfList;
    private Set<UUID> optionsNoAnyOfList;

    @Override
    public boolean isEmptySearch() {
        return CollectionUtils.isEmpty(optionsAllOfList)
                && CollectionUtils.isEmpty(optionsAnyOfList)
                && CollectionUtils.isEmpty(optionsNoAllOfList)
                && CollectionUtils.isEmpty(optionsNoAnyOfList);
    }
}

