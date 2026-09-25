package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class BasicSearch extends TwinSearch {
    TwinSearch headSearch;
    TwinSearch childrenSearch;
    // in some cases we need to disable view-permission check for featurer MultiplierIsolatedChildrenInStatuses.class
    // when user cant load twins created by other user, but logic required it.
    boolean checkViewPermission = true;
    // when set, TwinSearchService short-circuits before any DB query: the search is guaranteed to return an empty result
    boolean emptyResult = false;
    @Deprecated
    List<TwinSort> sorts;
}
