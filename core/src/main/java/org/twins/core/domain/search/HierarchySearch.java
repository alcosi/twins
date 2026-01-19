package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class HierarchySearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<String> hierarchyList;
    private Integer depth;
    public static final int INCLUDE_SELF = -1;

    public Boolean isEmpty() {
        return (idList == null || idList.isEmpty()) && (idExcludeList == null || idExcludeList.isEmpty());
    }

    public static final HierarchySearch EMPTY = new HierarchySearch()
            .setIdList(Collections.EMPTY_SET)
            .setIdExcludeList(Collections.EMPTY_SET);

    public HierarchySearch addTwinClassId(Collection<UUID> twinClassIdSet, boolean exclude) {
        if (exclude)
            idExcludeList = CollectionUtils.safeAdd(idExcludeList, twinClassIdSet);
        else
            idList = CollectionUtils.safeAdd(idList, twinClassIdSet);
        return this;
    }

    public HierarchySearch addTwinClassId(UUID twinClassId, boolean exclude) {
        if (exclude)
            idExcludeList = CollectionUtils.safeAdd(idExcludeList, twinClassId);
        else
            idList = CollectionUtils.safeAdd(idList, twinClassId);
        return this;
    }
}
