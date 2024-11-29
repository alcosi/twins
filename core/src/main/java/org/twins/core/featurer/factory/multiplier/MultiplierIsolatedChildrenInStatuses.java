package org.twins.core.featurer.factory.multiplier;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2209, name = "MultiplierIsolatedChildrenInStatuses", description = "Output list of twin relatives for each input. Output twin will be loaded by head and filtered by statusIds")
public class MultiplierIsolatedChildrenInStatuses extends Multiplier {

    @FeaturerParam(name = "statusIds", description = "")
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @FeaturerParam(name = "exclude", description = "")
    public static final FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        List<FactoryItem> ret = new ArrayList<>();
        for (FactoryItem inputItem : inputFactoryItemList) {
            TwinEntity inputTwin = inputItem.getTwin();
            BasicSearch search = new BasicSearch();
            if (exclude.extract(properties))
                search
                        .setTwinIdExcludeList(factoryContext.getInputTwinList().stream().map(TwinEntity::getId).collect(Collectors.toSet()));
            search
                    .addHeaderTwinId(inputTwin.getId())
                    .addStatusId(statusIds.extract(properties), false);
            List<TwinEntity> relativesTwinEntityList = twinSearchService.findTwins(search);
            if (CollectionUtils.isEmpty(relativesTwinEntityList)) {
                log.error(inputTwin.logShort() + " no relatives twins by head[" + inputTwin.getId() + "] in statuses[" + statusIds.extract(properties) + "]");
                continue;
            }
            for (TwinEntity relativeTwinEntity : relativesTwinEntityList) {
                TwinUpdate twinUpdate = new TwinUpdate();
                twinUpdate.setDbTwinEntity(relativeTwinEntity) // original twin
                        .setTwinEntity(relativeTwinEntity.clone()); // collecting updated in new twin
                ret.add(new FactoryItem().setOutput(twinUpdate).setContextFactoryItemList(List.of(inputItem)));
            }
        } return ret;
    }
}
