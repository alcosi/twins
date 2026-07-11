package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class FactoryMultiplierSearch extends EntitySearch<TwinFactoryMultiplierEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> factoryIdList;
    private Set<UUID> factoryIdExcludeList;
    private Set<UUID> inputTwinClassIdList;
    private Set<UUID> inputTwinClassIdExcludeList;
    private Set<Integer> multiplierFeaturerIdList;
    private Set<Integer> multiplierFeaturerIdExcludeList;
    private Set<String> descriptionLikeList;
    private Set<String> descriptionNotLikeList;
    private Ternary active;
}
