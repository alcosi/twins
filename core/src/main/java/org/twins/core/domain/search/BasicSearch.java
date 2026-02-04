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
    // if true, status check will consider freeze status from twin class (freeze status has priority over native twin status)
    boolean checkFreezeStatus = true;
    List<TwinSort> sorts;
}
