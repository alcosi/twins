package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsLinkId;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Keeps twins linked to the requested twin in either direction (twin_link),
 * e.g. task dependencies for a task page widget: tasks linked to the open task
 * regardless of which side of the link the open task is on.
 * Configured {@code twinClassIds} restrict the linked twins (the opposite side
 * of the link) to the given classes, e.g. TASK for TASK-TASK links only.
 */
@Slf4j
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_2731,
        name = "Linked twins of requested twin",
        description = "Keeps twins linked to the requested twin in either direction (twin_link)")
public class TwinFinderByLinkedTwinsRequested extends TwinFinderRequested {
    @FeaturerParam(name = "Param key", description = "", order = 1, optional = true, defaultValue = PARAM_TWIN_ID)
    public static final FeaturerParamString paramKey = new FeaturerParamString("paramKey");

    @FeaturerParam(name = "Twin class ids", description = "Restricts the linked twins (the opposite link side) to these classes, e.g. TASK for TASK-TASK links; empty = any class", order = 2, optional = true)
    public static final FeaturerParamUUIDSet twinClassIds = new FeaturerParamUUIDSetTwinsClassId("twinClassIds");

    @FeaturerParam(name = "Link ids", description = "Restricts the links to these types, e.g. the TASK-TASK dependency links; empty = any link", order = 3, optional = true)
    public static final FeaturerParamUUIDSet linkIds = new FeaturerParamUUIDSetTwinsLinkId("linkIds");

    @FeaturerParam(name = "Linked twins limit", description = "Max linked twins to resolve; overflow is warned and truncated", order = 4, optional = true, defaultValue = "1000")
    public static final FeaturerParamInt linkedTwinsLimit = new FeaturerParamInt("linkedTwinsLimit");

    @Autowired
    private TwinRepository twinRepository;

    @Override
    protected void concatSearch(Properties properties, TwinSearch twinSearch, Map<String, String> namedParamsMap) throws ServiceException {
        UUID twinId = getRequestedId(paramKey, properties, namedParamsMap);
        if (twinId == null)
            return;
        int limit = linkedTwinsLimit.extract(properties);
        if (limit < 1)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "linkedTwinsLimit must be positive");
        Set<UUID> classIds = twinClassIds.extract(properties);
        Set<UUID> requestedLinkIds = linkIds.extract(properties);
        boolean hasClasses = CollectionUtils.isNotEmpty(classIds);
        boolean hasLinks = CollectionUtils.isNotEmpty(requestedLinkIds);
        int queryLimit = limit + 1; // one extra row detects overflow
        Set<UUID> linkedTwinIds;
        if (hasClasses && hasLinks)
            linkedTwinIds = twinRepository.findLinkedTwinIdsByTwinIdAndTwinClassIdsAndLinkIds(twinId, classIds, requestedLinkIds, queryLimit);
        else if (hasClasses)
            linkedTwinIds = twinRepository.findLinkedTwinIdsByTwinIdAndTwinClassIds(twinId, classIds, queryLimit);
        else if (hasLinks)
            linkedTwinIds = twinRepository.findLinkedTwinIdsByTwinIdAndLinkIds(twinId, requestedLinkIds, queryLimit);
        else
            linkedTwinIds = twinRepository.findLinkedTwinIdsByTwinId(twinId, queryLimit);
        if (CollectionUtils.isEmpty(linkedTwinIds)) { // no linked twins must show no twins, not the whole class
            twinSearch.addTwinId(UuidUtils.NULLIFY_MARKER, false);
            return;
        }
        if (linkedTwinIds.size() > limit) {
            log.warn("Linked twins of twin[{}] exceeded limit[{}], capped - twins beyond the cap are missed", twinId, limit);
            linkedTwinIds = linkedTwinIds.stream().limit(limit).collect(Collectors.toSet());
        }
        twinSearch.addTwinId(linkedTwinIds, false);
    }
}
