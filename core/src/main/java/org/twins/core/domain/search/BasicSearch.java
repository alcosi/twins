package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Twin search object. Sort-free on purpose: the same object drives both page fetches and count
 * queries (where ORDER BY is meaningless), so sorting is always an explicit parameter of the
 * search execution methods, never part of the filter state.
 */
@Data
@Accessors(chain = true)
public class BasicSearch extends TwinSearch {
    TwinSearch headSearch;
    TwinSearch childrenSearch;
    // in some cases we need to disable view-permission check for featurer MultiplierIsolatedChildrenInStatuses.class
    // when user cant load twins created by other user, but logic required it.
    boolean checkViewPermission = true;
}
