package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class FactoryBranchSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> factoryIdList;
    private Set<UUID> factoryIdExcludeList;
    private Set<UUID> factoryConditionSetIdList;
    private Set<UUID> factoryConditionSetIdExcludeList;
    private Set<UUID> nextFactoryIdList;
    private Set<UUID> nextFactoryIdExcludeList;
    private Set<String> descriptionLikeList;
    private Set<String> descriptionNotLikeList;
    private Ternary conditionInvert;
    private Ternary active;
}
