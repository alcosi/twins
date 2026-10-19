package org.twins.core.featurer.factory.multiplier;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.twin.TwinHeadService;
import org.twins.core.service.twin.TwinSearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2217,
        name = "Isolated twins by head and assignee",
        description = "For each input twin, load twins of the given class with the same head twin id and assigner_user_id")
public class MultiplierIsolatedTwinsByHeadAndAssignee extends Multiplier {

    @FeaturerParam(name = "Twin class id", description = "Twin class to search", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    @FeaturerParam(name = "Head of twin class id", description = "Walk up head hierarchy until twin of this class is found; used as search head", order = 2, optional = true)
    public static final FeaturerParamUUID headTwinClassId = new FeaturerParamUUIDTwinsTwinClassId("headTwinClassId");

    @Lazy
    @Autowired
    private TwinHeadService twinHeadService;
    @Lazy
    @Autowired
    private TwinSearchService twinSearchService;

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        List<FactoryItem> ret = new ArrayList<>();
        UUID extractedTwinClassId = twinClassId.extract(properties);

        for (FactoryItem inputItem : inputFactoryItemList) {
            TwinEntity contextTwin = inputItem.getTwin();
            if (contextTwin == null) {
                log.info("Input twin is empty, multiplier step skipped");
                continue;
            }

            UUID headTwinId = twinHeadService.resolveHeadTwinId(contextTwin, headTwinClassId.extract(properties));
            UUID assigneeUserId = contextTwin.getAssignerUserId();
            if (headTwinId == null) {
                log.info("{} has no head, multiplier step skipped", contextTwin.logShort());
                continue;
            }
            if (assigneeUserId == null) {
                log.info("{} has no assignee, multiplier step skipped", contextTwin.logShort());
                continue;
            }

            BasicSearch search = new BasicSearch().setCheckViewPermission(false);
            search
                    .addOwnerBusinessAccountId(factoryContext.getRunLimitedByOwnerBusinessAccount())
                    .addTwinClassId(extractedTwinClassId, false)
                    .addHeadTwinId(headTwinId)
                    .addAssigneeUserId(assigneeUserId, false);

            List<TwinEntity> twins = twinSearchService.findTwins(search);
            if (CollectionUtils.isEmpty(twins)) {
                log.info("{} no twins of class[{}] by head[{}] and assignee[{}]",
                        contextTwin.logShort(), extractedTwinClassId, headTwinId, assigneeUserId);
                continue;
            }

            for (TwinEntity twin : twins) {
                TwinUpdate twinUpdate = new TwinUpdate();
                twinUpdate
                        .setDbTwinEntity(twin)
                        .setTwinEntity(twin.clone());
                ret.add(new FactoryItem()
                        .setOutput(twinUpdate)
                        .setContextFactoryItemList(List.of(inputItem)));
            }
        }
        return ret;
    }
}
