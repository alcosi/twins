package org.twins.core.featurer.twin.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamDouble;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinFieldClause;
import org.twins.core.domain.TwinFieldFilter;
import org.twins.core.domain.search.TwinFieldValueSearchNumeric;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Lazy
@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_2728,
        name = "By field numeric (given)",
        description = "Adds a numeric field condition with the bounds given in the params")
public class TwinFinderByFieldNumericGiven extends TwinFinder {
    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "More then", description = "strictly greater", order = 2, optional = true)
    public static final FeaturerParamDouble moreThen = new FeaturerParamDouble("moreThen");

    @FeaturerParam(name = "Less then", description = "strictly less", order = 3, optional = true)
    public static final FeaturerParamDouble lessThen = new FeaturerParamDouble("lessThen");

    @FeaturerParam(name = "Equals", description = "", order = 4, optional = true)
    public static final FeaturerParamDouble equals = new FeaturerParamDouble("equals");

    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        TwinClassFieldEntity field = twinClassFieldService.findEntitySafe(twinClassFieldId.extract(properties));
        FieldTyper fieldTyper = featurerService.getFeaturer(field.getFieldTyperFeaturerId(), FieldTyper.class);
        if (!fieldTyper.getTwinFieldSearch().isAssignableFrom(TwinFieldValueSearchNumeric.class))
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "field[" + field.easyLog(EasyLoggable.Level.SHORT) + "] typer does not support numeric search");
        TwinFieldValueSearchNumeric condition = new TwinFieldValueSearchNumeric()
                .setMoreThen(moreThen.extract(properties))
                .setLessThen(lessThen.extract(properties))
                .setEquals(equals.extract(properties));
        condition.setTwinClassFieldEntity(field);
        condition.setFieldTyper(fieldTyper);
        TwinFieldFilter fieldsFilter = twinSearch.getFieldsFilter();
        if (fieldsFilter == null)
            twinSearch.setFieldsFilter(fieldsFilter = new TwinFieldFilter());
        fieldsFilter.addClause(new TwinFieldClause().addCondition(condition));
    }
}
