package org.twins.core.featurer.factory.multiplier;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2208,
        name = "Isolated by link",
        description = "Output list of twin relatives for each input. Output twin list will be loaded by link and filtered by statusIds")
public class MultiplierIsolatedByLink extends Multiplier {

    @FeaturerParam(name = "Link id", description = "Link from sought twin to factory input twin", order = 1)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "Status ids", description = "Statuses of src(fwd) linked twin. If empty - twins with any status will be found", order = 2)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @FeaturerParam(name = "Exclude statues", description = "Exclude(true)/Include(false) child Twin.Status.IDs from query result", order = 3)
    public static final FeaturerParamBoolean excludeStatuses = new FeaturerParamBoolean("excludeStatuses");

    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    LinkService linkService;

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        List<FactoryItem> ret = new ArrayList<>();
        for (FactoryItem inputItem : inputFactoryItemList) {
            TwinEntity inputTwin = inputItem.getTwin();
            LinkEntity link = linkService.findEntitySafe(linkId.extract(properties));
            if (null == link) {
                log.error(linkId + " no link found. Skipped by multiplier");
                continue;
            }
            BasicSearch search = new BasicSearch();
            search
                    .addOwnerBusinessAccountId(factoryContext.getRunLimitedByOwnerBusinessAccount())
                    .addLinkDstTwinsId(linkId.extract(properties), List.of(inputTwin.getId()), false, true)
                    .addStatusId(statusIds.extract(properties), excludeStatuses.extract(properties));
            List<TwinEntity> relativesTwinEntityList = twinSearchService.findTwins(search);
            if (CollectionUtils.isEmpty(relativesTwinEntityList)) {
                log.error(inputTwin.logShort() + " no relatives twins by head[" + inputTwin.getHeadTwinId() + "]");
                continue;
            }
            for (TwinEntity relativeTwinEntity : relativesTwinEntityList) {
                TwinUpdate twinUpdate = new TwinUpdate();
                twinUpdate
                        .setDbTwinEntity(relativeTwinEntity) // original twin
                        .setTwinEntity(relativeTwinEntity.clone()); // collecting updated in new twin
                ret.add(new FactoryItem()
                        .setOutput(twinUpdate)
                        .setContextFactoryItemList(List.of(inputItem)));
            }
        }
        return ret;
    }
}
