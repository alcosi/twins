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
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;

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
    private TwinService twinService;
    @Lazy
    @Autowired
    private TwinSearchService twinSearchService;

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        List<FactoryItem> ret = new ArrayList<>();
        UUID extractedTwinClassId = twinClassId.extract(properties);
        UUID headClassId = headTwinClassId.extract(properties);

        for (FactoryItem inputItem : inputFactoryItemList) {
            TwinEntity inputTwin = inputItem.getTwin();
            if (inputTwin == null) {
                log.info("Input twin is empty, multiplier step skipped");
                continue;
            }

            UUID headId = resolveHeadTwinId(inputTwin, headClassId);
            UUID assigneeUserId = inputTwin.getAssignerUserId();
            if (headId == null) {
                log.info("{} has no head, multiplier step skipped", inputTwin.logShort());
                continue;
            }
            if (assigneeUserId == null) {
                log.info("{} has no assignee, multiplier step skipped", inputTwin.logShort());
                continue;
            }

            BasicSearch search = new BasicSearch().setCheckViewPermission(false);
            search
                    .addOwnerBusinessAccountId(factoryContext.getRunLimitedByOwnerBusinessAccount())
                    .addTwinClassId(extractedTwinClassId, false)
                    .addHeadTwinId(headId)
                    .addAssigneeUserId(assigneeUserId, false);

            List<TwinEntity> twins = twinSearchService.findTwins(search);
            if (CollectionUtils.isEmpty(twins)) {
                log.info("{} no twins of class[{}] by head[{}] and assignee[{}]",
                        inputTwin.logShort(), extractedTwinClassId, headId, assigneeUserId);
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

    private UUID resolveHeadTwinId(TwinEntity inputTwin, UUID headTwinClassId) throws ServiceException {
        if (headTwinClassId == null) {
            return inputTwin.getHeadTwinId() != null ? inputTwin.getHeadTwinId() : inputTwin.getId();
        }
        TwinEntity current = inputTwin;
        for (int depth = 0; depth < 10; depth++) {
            if (current.getHeadTwinId() == null) {
                return null;
            }
            twinService.loadHead(current);
            if (headTwinClassId.equals(current.getHeadTwin().getTwinClassId())) {
                return current.getHeadTwinId();
            }
            current = current.getHeadTwin();
        }
        return null;
    }
}
