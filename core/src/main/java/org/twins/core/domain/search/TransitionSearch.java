package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.enums.twinflow.TwinflowTransitionType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TransitionSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<String> nameLikeList;
    private Set<String> nameNotLikeList;
    private Set<String> descriptionLikeList;
    private Set<String> descriptionNotLikeList;
    private Map<UUID, Boolean> twinClassIdMap;
    private Map<UUID, Boolean> twinClassIdExcludeMap;
    private Set<UUID> twinflowIdList;
    private Set<UUID> twinflowIdExcludeList;
    private Set<UUID> srcStatusIdList;
    private Set<UUID> srcStatusIdExcludeList;
    private Set<UUID> dstStatusIdList;
    private Set<UUID> dstStatusIdExcludeList;
    private Set<String> aliasLikeList;
    private Set<UUID> permissionIdList;
    private Set<UUID> permissionIdExcludeList;
    private Set<UUID> inbuiltTwinFactoryIdList;
    private Set<UUID> inbuiltTwinFactoryIdExcludeList;
    private Set<UUID> draftingTwinFactoryIdList;
    private Set<UUID> draftingTwinFactoryIdExcludeList;
    private Set<TwinflowTransitionType> twinflowTransitionTypeList;
    private Set<TwinflowTransitionType> twinflowTransitionTypeExcludeList;
}
