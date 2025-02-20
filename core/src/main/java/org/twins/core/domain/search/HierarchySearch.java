package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class HierarchySearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Integer depth;

    public Boolean isEmpty() {
        return (idList == null || idList.isEmpty()) && (idExcludeList == null || idExcludeList.isEmpty());
    }

    public static final HierarchySearch EMPTY = new HierarchySearch()
            .setIdList(Collections.EMPTY_SET)
            .setIdExcludeList(Collections.EMPTY_SET);
}
