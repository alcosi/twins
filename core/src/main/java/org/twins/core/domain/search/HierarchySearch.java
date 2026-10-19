package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.SetUtils;

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
    private Integer depth;
    public static final int INCLUDE_SELF = -1;

    public Boolean isEmpty() {
        return (idList == null || idList.isEmpty()) && (idExcludeList == null || idExcludeList.isEmpty());
    }

    public static final HierarchySearch EMPTY = new HierarchySearch()
            .setIdList(Collections.EMPTY_SET)
            .setIdExcludeList(Collections.EMPTY_SET);

    public HierarchySearch addTwinClassId(Collection<UUID> twinClassIdSet, boolean exclude) {
        return SetUtils.safeAddAll(this, twinClassIdSet, exclude,
                this::getIdList, this::setIdList,
                this::getIdExcludeList, this::setIdExcludeList);
    }

    public HierarchySearch addTwinClassId(UUID twinClassId, boolean exclude) {
        return SetUtils.safeAdd(this, twinClassId, exclude,
                this::getIdList, this::setIdList,
                this::getIdExcludeList, this::setIdExcludeList);
    }
}
