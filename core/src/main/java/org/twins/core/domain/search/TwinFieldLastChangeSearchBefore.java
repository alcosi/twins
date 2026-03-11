package org.twins.core.domain.search;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinFieldLastChangeSearchBefore extends TwinFieldLastChangeSearch {

    private long lessThenSecondsAgo;

    @Override
    public boolean isEmptySearch() {
        return lessThenSecondsAgo <= 0;
    }
}

