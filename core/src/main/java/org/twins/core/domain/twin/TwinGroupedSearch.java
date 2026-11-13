package org.twins.core.domain.twin;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.domain.search.BasicSearch;

import java.util.List;

@Data
@Accessors(chain = true)
public class TwinGroupedSearch {
    private BasicSearch search;
    private List<TwinGroupCriterion> levels;
    private List<TwinMemberSort> memberSorts;
}
