package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class FactoryConditionSearch {
    Set<UUID> idList;
    Set<UUID> idExcludeList;
    Set<UUID> factoryConditionSetIdList;
    Set<UUID> factoryConditionSetIdExcludeList;
    Set<Integer> conditionerFeaturerIdList;
    Set<Integer> conditionerFeaturerIdExcludeList;
    Set<String> descriptionLikeList;
    Set<String> descriptionNotLikeList;
    Ternary invert;
    Ternary active;
}
