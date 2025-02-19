package org.twins.core.domain.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@FieldNameConstants
@AllArgsConstructor
public class HierarchySearch {
    private Set<UUID> twinClassIdList;
    private Set<UUID> twinClassIdExcludeList;
    private Integer depth;

    public Boolean isEmpty() {
        return (twinClassIdList == null || twinClassIdList.isEmpty()) && (twinClassIdExcludeList == null || twinClassIdExcludeList.isEmpty());
    }
}
