package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.enums.factory.FactoryEraserAction;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class FactoryEraserSearch extends EntitySearch<TwinFactoryEraserEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> factoryIdList;
    private Set<UUID> factoryIdExcludeList;
    private Set<UUID> inputTwinClassIdList;
    private Set<UUID> inputTwinClassIdExcludeList;
    private Set<UUID> factoryConditionSetIdList;
    private Set<UUID> factoryConditionSetIdExcludeList;
    private Ternary conditionInvert;
    private Set<String> descriptionLikeList;
    private Set<String> descriptionNotLikeList;
    private Set<FactoryEraserAction> eraseActionLikeList;
    private Set<FactoryEraserAction> eraseActionNotLikeList;
    private Ternary active;
}
