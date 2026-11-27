package org.twins.core.featurer.twin.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinFieldClause;
import org.twins.core.domain.TwinFieldFilter;
import org.twins.core.domain.search.TwinFieldLastChangeSearchBefore;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Lazy
@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_2733,
        name = "By field last change (before given)",
        description = "Restricts twins to those whose given field was last changed within the given number of seconds before now")
public class TwinFinderByFieldLastChangeBeforeGiven extends TwinFinder {
    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "Seconds ago", description = "the field must have been changed within this many seconds before now", order = 2)
    public static final FeaturerParamInt secondsAgo = new FeaturerParamInt("secondsAgo");

    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public void concatSearch(Properties properties, TwinSearch twinSearch, Map<String, String> namedParamsMap) throws ServiceException {
        TwinClassFieldEntity field = twinClassFieldService.findEntitySafe(twinClassFieldId.extract(properties));
        TwinFieldLastChangeSearchBefore condition = new TwinFieldLastChangeSearchBefore()
                .setLessThenSecondsAgo(secondsAgo.extract(properties));
        condition.setTwinClassFieldEntity(field);
        TwinFieldFilter fieldsFilter = twinSearch.getFieldsFilter();
        if (fieldsFilter == null)
            twinSearch.setFieldsFilter(fieldsFilter = new TwinFieldFilter());
        fieldsFilter.addClause(new TwinFieldClause().addCondition(condition));
    }
}
