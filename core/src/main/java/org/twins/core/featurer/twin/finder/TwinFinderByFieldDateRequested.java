package org.twins.core.featurer.twin.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
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
import org.twins.core.featurer.fieldtyper.FieldTyperTimestamp;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Lazy
@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_2724,
        name = "By field date (requested)",
        description = "Adds a date/timestamp field condition whose bounds are read from named request params (ISO-8601, e.g. 2026-10-04T00:00:00)")
public class TwinFinderByFieldDateRequested extends TwinFinder {
    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "Less then or equals param key", description = "named param key holding the upper bound", order = 2, optional = true)
    public static final FeaturerParamString lessThenOrEqualsParamKey = new FeaturerParamString("lessThenOrEqualsParamKey");

    @FeaturerParam(name = "More then or equals param key", description = "named param key holding the lower bound", order = 3, optional = true)
    public static final FeaturerParamString moreThenOrEqualsParamKey = new FeaturerParamString("moreThenOrEqualsParamKey");

    @FeaturerParam(name = "Equals param key", description = "named param key holding the exact value", order = 4, optional = true)
    public static final FeaturerParamString equalsParamKey = new FeaturerParamString("equalsParamKey");

    @FeaturerParam(name = "Empty", description = "twins missing the field value match too (OR value IS NULL)", order = 5, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean empty = new FeaturerParamBoolean("empty");

    @FeaturerParam(name = "Required", description = "fail the search when a configured param is missing from the request", order = 6, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean required = new FeaturerParamBoolean("required");

    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        TwinClassFieldEntity field = twinClassFieldService.findEntitySafe(twinClassFieldId.extract(properties));
        FieldTyper fieldTyper = featurerService.getFeaturer(field.getFieldTyperFeaturerId(), FieldTyper.class);
        if (!(fieldTyper instanceof FieldTyperTimestamp))
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "field[" + field.easyLog(EasyLoggable.Level.SHORT) + "] typer does not support date search");
        TwinFieldValueSearchDate condition = new TwinFieldValueSearchDate()
                .setEmpty(empty.extract(properties));
        condition.setTwinClassFieldEntity(field);
        condition.setFieldTyper(fieldTyper);
        condition.setLessThenOrEquals(resolveBound(lessThenOrEqualsParamKey, properties, namedParamsMap));
        condition.setMoreThenOrEquals(resolveBound(moreThenOrEqualsParamKey, properties, namedParamsMap));
        condition.setEquals(resolveBound(equalsParamKey, properties, namedParamsMap));
        TwinFieldFilter fieldsFilter = twinSearch.getFieldsFilter();
        if (fieldsFilter == null)
            twinSearch.setFieldsFilter(fieldsFilter = new TwinFieldFilter());
        fieldsFilter.addClause(new TwinFieldClause().addCondition(condition));
    }

    /**
     * Reads the bound from the named param. Configured but missing param fails when required,
     * otherwise the bound is treated as absent.
     */
    static LocalDateTime resolveBound(FeaturerParamString paramKey, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        String paramKeyStr = paramKey.extract(properties);
        if (StringUtils.isBlank(paramKeyStr))
            return null;
        String paramValue = namedParamsMap == null ? null : namedParamsMap.get(paramKeyStr);
        if (StringUtils.isBlank(paramValue)) {
            if (required.extract(properties))
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED, "search param[" + paramKeyStr + "] missed");
            return null;
        }
        try {
            return LocalDateTime.parse(paramValue);
        } catch (DateTimeParseException e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "search param[" + paramKeyStr + "] is not ISO-8601 date-time: [" + paramValue + "]");
        }
    }
}
