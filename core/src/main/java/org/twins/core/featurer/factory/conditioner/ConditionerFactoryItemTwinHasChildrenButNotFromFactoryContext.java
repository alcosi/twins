package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.service.twin.TwinSearchService;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2455,
        name = "Has children but not from factory context",
        description = "")
@Slf4j
public class ConditionerFactoryItemTwinHasChildrenButNotFromFactoryContext extends Conditioner {
    @FeaturerParam(name = "Status ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        Set<UUID> excludeIds = new HashSet<>();
        for (var factoryItemObj : factoryItem.getFactoryContext().getAllFactoryItemList()) {
            if (factoryItemObj.getOutput() instanceof TwinUpdate twinUpdate) {
                excludeIds.add(twinUpdate.getTwinId());
            }
        }
        BasicSearch search = new BasicSearch();
        search
                .addHeadTwinId(factoryItem.getOutput().getTwinEntity().getId())
                .setTwinIdExcludeList(excludeIds)
                .addStatusId(statusIds.extract(properties), false);
        long count = twinSearchService.count(search);
        return count > 0;
    }
}
