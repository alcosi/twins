package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Properties;
import java.util.stream.Collectors;

@Component
@Featurer(id = FeaturerTwins.ID_2407,
        name = "Factory item twin has children in status",
        description = "")
@Slf4j
public class ConditionerFactoryItemTwinHasChildrenInStatus extends Conditioner {
    @FeaturerParam(name = "Status ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @FeaturerParam(name = "Exclude factory input", description = "", order = 2)
    public static final FeaturerParamBoolean excludeFactoryInput = new FeaturerParamBoolean("excludeFactoryInput");

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        BasicSearch search = new BasicSearch();
        search
                .addHeadTwinId(factoryItem.getOutput().getTwinEntity().getId())
                .addStatusId(statusIds.extract(properties), false);
        if (excludeFactoryInput.extract(properties))
            search.setTwinIdExcludeList(factoryItem.getFactoryContext().getInputTwinList().stream().map(TwinEntity::getId).collect(Collectors.toSet()));
        long count = twinSearchService.count(search);
        return count > 0;
    }
}
