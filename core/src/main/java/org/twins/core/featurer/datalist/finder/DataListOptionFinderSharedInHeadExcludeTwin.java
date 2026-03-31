package org.twins.core.featurer.datalist.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.DataListOptionSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperList;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_5103,
        name = "Finder shared in head exclude twin",
        description = "find options not used by twins that shared one head exclude twin")
@RequiredArgsConstructor
public class DataListOptionFinderSharedInHeadExcludeTwin extends DataListOptionFinder {
    private final DataListService dataListService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinService twinService;

    @Override
    public void concatSearch(Properties properties, DataListOptionSearch optionSearch, Map<String, String> namedParamsMap) throws ServiceException {
        UUID twinId = UUID.fromString(namedParamsMap.get(PARAM_CURRENT_TWIN_ID));
        UUID twinClassFieldId = UUID.fromString(namedParamsMap.get(PARAM_CURRENT_TWIN_CLASS_FIELD_ID));

        TwinEntity twinEntity = twinService.findEntitySafe(twinId);
        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.findEntitySafe(twinClassFieldId);

        FieldTyper<?, ?, ?, ?> fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturerId(), FieldTyper.class);
        if (fieldTyper.getStorageType() != TwinFieldStorageDatalist.class) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, twinClassFieldEntity.logNormal() + " does not have data list oriented fieldTyper");
        }
        FieldTyperList fieldTyperList = (FieldTyperList) fieldTyper;
        Properties fieldTyperProperties = fieldTyper.extractProperties(twinClassFieldEntity.getFieldTyperParams());
        UUID listId = fieldTyperList.getDataListId(fieldTyperProperties);

        Set<UUID> optionIds = dataListService.findOptionIdsByDataListIdAndNotUsedInHeadExcludingTwin(listId, twinClassFieldId, twinEntity.getHeadTwinId(), twinEntity.getId());

        optionSearch
                .addDataListId(listId, false)
                .setIdList(optionIds);
    }
}
