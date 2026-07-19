package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.twin.TwinSearchServiceV2;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Featurer(id = FeaturerTwins.ID_2454,
        name = "Twin exists by head and assignee",
        description = "True if twin exists with same head twin id and assigner_user_id as the factory item (or context) twin")
@Slf4j
public class ConditionerTwinExistsByHeadAndAssignee extends Conditioner {

    @FeaturerParam(name = "Twin class id", description = "Twin class to search", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    @FeaturerParam(name = "Head of twin class id", description = "Walk up head hierarchy until twin of this class is found; used as search head", order = 2, optional = true)
    public static final FeaturerParamUUID headTwinClassId = new FeaturerParamUUIDTwinsTwinClassId("headTwinClassId");

    @FeaturerParam(name = "Resolve head root", description = "Resolve head/assignee from factory item, else from context twin", order = 3, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean factoryItemElseContext = new FeaturerParamBoolean("factoryItemElseContext");

    @FeaturerParam(name = "Exclude factory input twin", description = "Exclude context and factory input twins from search", order = 4, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean excludeFactoryInputTwin = new FeaturerParamBoolean("excludeFactoryInputTwin");

    @Lazy
    @Autowired
    private TwinService twinService;
    @Lazy
    @Autowired
    private TwinSearchServiceV2 twinSearchService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        TwinEntity contextTwin;
        if (factoryItemElseContext.extract(properties)) {
            contextTwin = factoryItem.getTwin();
        } else {
            contextTwin = factoryItem.checkSingleContextTwin();
        }
        if (contextTwin == null) {
            log.debug("Root twin is empty, twin exists by head and assignee skipped");
            return false;
        }

        UUID headTwinId = resolveHeadTwinId(contextTwin, headTwinClassId.extract(properties));
        UUID assigneeUserId = contextTwin.getAssignerUserId();
        if (headTwinId == null) {
            log.debug("Root twin has no head, twin exists by head and assignee skipped");
            return false;
        }
        if (assigneeUserId == null) {
            log.debug("Root twin has no assignee, twin exists by head and assignee skipped");
            return false;
        }

        BasicSearch search = new BasicSearch().setCheckViewPermission(false);
        search
                .addTwinClassId(twinClassId.extract(properties), false)
                .addHeadTwinId(headTwinId)
                .addAssigneeUserId(assigneeUserId, false);

        if (excludeFactoryInputTwin.extract(properties)) {
            Set<UUID> excludeIds = factoryItem.getFactoryContext().getInputTwinList().stream()
                    .map(TwinEntity::getId)
                    .collect(Collectors.toSet());
            if (contextTwin.getId() != null) {
                excludeIds.add(contextTwin.getId());
            }
            if (!excludeIds.isEmpty()) {
                search.setTwinIdExcludeList(excludeIds);
            }
        }

        return twinSearchService.exists(search);
    }

    protected UUID resolveHeadTwinId(TwinEntity contextTwin, UUID headTwinClassId) throws ServiceException {
        if (headTwinClassId == null) {
            return contextTwin.getHeadTwinId() != null ? contextTwin.getHeadTwinId() : contextTwin.getId();
        }
        TwinEntity current = contextTwin;
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
