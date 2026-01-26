package org.twins.core.featurer.classfield.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_3208,
        name = "Field finder by exclude head match",
        description = "")
public class FieldFinderByIdExcludeHeadMatched extends FieldFinder {
    private final TwinService twinService;
    private final TwinClassFieldService twinClassFieldService;

    @FeaturerParam(name = "Exclude", description = "", optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    @Override
    protected void concatSearch(Properties properties, TwinClassFieldSearch fieldSearch, Map<String, String> namedParamsMap) throws ServiceException {
        String paramValue = namedParamsMap.get(PARAM_CURRENT_TWIN_ID);
        if (StringUtils.isBlank(paramValue))
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED, "search param[" + PARAM_CURRENT_TWIN_ID + "] missed");
        if (!UuidUtils.isUUID(paramValue)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED, "search param[" + PARAM_CURRENT_TWIN_ID + "] is not UUID");
        }
        var twinId = UUID.fromString(namedParamsMap.get(PARAM_CURRENT_TWIN_ID));
        var twin = twinService.findEntitySafe(twinId);
        twinService.loadHeadForTwin(twin);
        if (twin.getHeadTwin() == null)
            throw new ServiceException(ErrorCodeTwins.HEAD_TWIN_NOT_SPECIFIED, "{} does not have head", twin.logNormal());
        twinClassFieldService.loadTwinClassFields(twin.getTwinClass());
        twinClassFieldService.loadTwinClassFields(twin.getHeadTwin().getTwinClass());
        Set<String> headTwinFieldMatchSet = new HashSet<>();
        Set<UUID> excludeFields = new HashSet<>();
        for (var headTwinField : twin.getHeadTwin().getTwinClass().getTwinClassFieldKit()) {
            headTwinFieldMatchSet.add(getMatchCode(headTwinField));
        }
        for (var currentTwinField : twin.getTwinClass().getTwinClassFieldKit()) {
            if (headTwinFieldMatchSet.contains(getMatchCode(currentTwinField))) {
                excludeFields.add(currentTwinField.getId());
            }
        }
        fieldSearch.addId(excludeFields, exclude.extract(properties));
    }

    private static String getMatchCode(TwinClassFieldEntity classField) {
        return classField.getKey() + classField.getFieldTyperFeaturerId() + (classField.getFieldTyperParams() == null ? "" : classField.getFieldTyperParams().hashCode());
    }
}