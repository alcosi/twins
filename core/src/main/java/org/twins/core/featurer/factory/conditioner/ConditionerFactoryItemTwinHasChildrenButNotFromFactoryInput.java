package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
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

@Deprecated
@Component
@Featurer(id = FeaturerTwins.ID_2408,
        name = "HasChildrenButNotFromFactoryInput",
        description = "")
@Slf4j
public class ConditionerFactoryItemTwinHasChildrenButNotFromFactoryInput extends Conditioner {
    @FeaturerParam(name = "Status ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        BasicSearch search = new BasicSearch();
        search
                .addHeaderTwinId(factoryItem.getOutput().getTwinEntity().getId())
                .setTwinIdExcludeList(factoryItem.getFactoryContext().getInputTwinList().stream().map(TwinEntity::getId).collect(Collectors.toSet()))
                .addStatusId(statusIds.extract(properties), false);
        long count = twinSearchService.count(search);
        return count > 0;
    }
}
