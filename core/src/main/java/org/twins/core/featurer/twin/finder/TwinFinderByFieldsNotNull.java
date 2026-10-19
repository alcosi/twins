package org.twins.core.featurer.twin.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinFieldClause;
import org.twins.core.domain.TwinFieldFilter;
import org.twins.core.domain.search.TwinFieldValueSearchDate;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Lazy
@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_2725,
        name = "By fields not null (given)",
        description = "Adds one OR-clause: at least one of the given date/timestamp fields has a value")
public class TwinFinderByFieldsNotNull extends TwinFinder {
    @FeaturerParam(name = "Twin class field ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet twinClassFieldIds = new FeaturerParamUUIDSetTwinsTwinClassFieldId("twinClassFieldIds");

    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        TwinFieldClause clause = new TwinFieldClause();
        for (UUID fieldId : twinClassFieldIds.extract(properties)) {
            TwinClassFieldEntity field = twinClassFieldService.findEntitySafe(fieldId);
            FieldTyper fieldTyper = twinClassFieldService.checkValueType(field, FieldValueDate.class);
            if (!fieldTyper.getTwinFieldSearch().isAssignableFrom(TwinFieldValueSearchDate.class))
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "field[" + field.easyLog(EasyLoggable.Level.SHORT) + "] typer does not support date search");
            // empty=false with no bounds means "is not null" (TwinSpecification.checkFieldTimestamp)
            TwinFieldValueSearchDate condition = new TwinFieldValueSearchDate()
                    .setEmpty(false);
            condition.setTwinClassFieldEntity(field);
            condition.setFieldTyper(fieldTyper);
            clause.addCondition(condition);
        }
        TwinFieldFilter fieldsFilter = twinSearch.getFieldsFilter();
        if (fieldsFilter == null)
            twinSearch.setFieldsFilter(fieldsFilter = new TwinFieldFilter());
        fieldsFilter.addClause(clause);
    }
}
