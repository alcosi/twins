package org.twins.core.domain.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.DTOExamples;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class FactoryMultiplierSearch {
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
