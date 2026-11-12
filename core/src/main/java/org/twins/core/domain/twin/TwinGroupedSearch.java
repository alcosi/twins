package org.twins.core.domain.twin;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.enums.twin.TwinGroupSort;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinGroupedSearch {
    private BasicSearch search;
    private UUID groupFieldId;
    private TwinGroupSort groupSort;
    private List<TwinMemberSort> memberSorts;
}
