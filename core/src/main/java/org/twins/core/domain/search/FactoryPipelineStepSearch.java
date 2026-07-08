package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class FactoryPipelineStepSearch extends EntitySearch<TwinFactoryPipelineStepEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> factoryIdList;
    private Set<UUID> factoryIdExcludeList;
    private Set<UUID> factoryPipelineIdList;
    private Set<UUID> factoryPipelineIdExcludeList;
    private Set<UUID> factoryConditionSetIdList;
    private Set<UUID> factoryConditionSetIdExcludeList;
    private Set<String> descriptionLikeList;
    private Set<String> descriptionNotLikeList;
    private Set<Integer> fillerFeaturerIdList;
    private Set<Integer> fillerFeaturerIdExcludeList;
    private Ternary conditionInvert;
    private Ternary active;
    private Ternary optional;
}
