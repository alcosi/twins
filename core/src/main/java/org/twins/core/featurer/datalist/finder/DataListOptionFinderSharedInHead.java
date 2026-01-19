package org.twins.core.featurer.datalist.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.datalist.DataListService;

import java.util.*;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_5102,
        name = "Finder shared in head",
        description = "find options not used by twins that shared one head")
@RequiredArgsConstructor
public class DataListOptionFinderSharedInHead extends DataListOptionFinder {
    private final DataListService dataListService;

    @Override
    public void concatSearch(Properties properties, DataListOptionSearch optionSearch, Map<String, String> namedParamsMap) throws ServiceException {
        UUID listId = UUID.fromString(namedParamsMap.get(PARAM_CURRENT_DATA_LIST_ID));
        UUID headTwinId = UUID.fromString(namedParamsMap.get(PARAM_CURRENT_HEAD_TWIN_ID));
        UUID twinClassFieldId = UUID.fromString(namedParamsMap.get(PARAM_CURRENT_TWIN_CLASS_FIELD_ID));

        Set<UUID> optionIds = dataListService.findOptionIdsByDataListIdAndNotUsedInHead(listId, twinClassFieldId, headTwinId);

        optionSearch
                .addDataListId(listId, false)
                .setIdList(optionIds);
    }
}
