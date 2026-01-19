package org.twins.core.featurer.datalist.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.*;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_5102,
        name = "Finder shared in head",
        description = "find options not used by twins that shared one head")
@RequiredArgsConstructor
public class DataListOptionFinderSharedInHead extends DataListOptionFinder {
    private final DataListService dataListService;
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public void concatSearch(Properties properties, DataListOptionSearch optionSearch, Map<String, String> namedParamsMap) throws ServiceException {
        UUID headTwinId = UUID.fromString(namedParamsMap.get(PARAM_CURRENT_HEAD_TWIN_ID));
        UUID twinClassFieldId = UUID.fromString(namedParamsMap.get(PARAM_CURRENT_TWIN_CLASS_FIELD_ID));

        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.findEntitySafe(twinClassFieldId);
        FieldTyper<?, ?, ?, ?> fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturerId(), FieldTyper.class);
        if (fieldTyper.getStorageType() != TwinFieldStorageDatalist.class) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, twinClassFieldEntity.logNormal() + " does not have data list oriented fieldTyper");
        }

        UUID listId = UUID.fromString(twinClassFieldEntity.getFieldTyperParams().get("listUUID"));

        Set<UUID> optionIds = dataListService.findOptionIdsByDataListIdAndNotUsedInHead(listId, twinClassFieldId, headTwinId);

        optionSearch
                .addDataListId(listId, false)
                .setIdList(optionIds);
    }
}
