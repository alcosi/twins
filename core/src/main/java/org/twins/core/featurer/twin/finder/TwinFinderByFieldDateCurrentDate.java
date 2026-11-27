package org.twins.core.featurer.twin.finder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamInt;
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
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Lazy
@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_2732,
        name = "By field date (current date)",
        description = "Adds a date/timestamp field condition whose bound is computed from the current server date, so the search needs no date request params")
public class TwinFinderByFieldDateCurrentDate extends TwinFinder {
    public static final String OPERATOR_LESS_THEN_OR_EQUALS = "LESS_OR_EQUALS";
    public static final String OPERATOR_MORE_THEN_OR_EQUALS = "MORE_OR_EQUALS";
    public static final String BOUNDARY_START_OF_DAY = "START_OF_DAY";
    public static final String BOUNDARY_END_OF_DAY = "END_OF_DAY";
    public static final String BOUNDARY_NOW = "NOW";

    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "Operator", description = "LESS_OR_EQUALS: field <= bound; MORE_OR_EQUALS: field >= bound", order = 2, optional = true, defaultValue = OPERATOR_LESS_THEN_OR_EQUALS)
    public static final FeaturerParamString operator = new FeaturerParamString("operator");

    @FeaturerParam(name = "Boundary", description = "START_OF_DAY: 00:00 of the (shifted) current day; END_OF_DAY: 23:59:59.999999999; NOW: current moment", order = 3, optional = true, defaultValue = BOUNDARY_START_OF_DAY)
    public static final FeaturerParamString boundary = new FeaturerParamString("boundary");

    @FeaturerParam(name = "Day shift", description = "Days added to the current date, negative for the past", order = 4, optional = true, defaultValue = "0")
    public static final FeaturerParamInt dayShift = new FeaturerParamInt("dayShift");

    @FeaturerParam(name = "Empty", description = "twins missing the field value match too (OR value IS NULL)", order = 5, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean empty = new FeaturerParamBoolean("empty");

    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public void concatSearch(Properties properties, TwinSearch twinSearch, Map<String, String> namedParamsMap) throws ServiceException {
        TwinClassFieldEntity field = twinClassFieldService.findEntitySafe(twinClassFieldId.extract(properties));
        FieldTyper fieldTyper = twinClassFieldService.checkValueType(field, FieldValueDate.class);
        if (!fieldTyper.getTwinFieldSearch().isAssignableFrom(TwinFieldValueSearchDate.class))
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "field[" + field.easyLog(EasyLoggable.Level.SHORT) + "] typer does not support date search");
        String operatorStr = StringUtils.defaultIfBlank(operator.extract(properties), OPERATOR_LESS_THEN_OR_EQUALS);
        String boundaryStr = StringUtils.defaultIfBlank(boundary.extract(properties), BOUNDARY_START_OF_DAY);
        LocalDateTime bound = resolveBound(LocalDate.now().plusDays(dayShift.extract(properties)), boundaryStr);
        TwinFieldValueSearchDate condition = new TwinFieldValueSearchDate()
                .setEmpty(empty.extract(properties));
        condition.setTwinClassFieldEntity(field);
        condition.setFieldTyper(fieldTyper);
        if (OPERATOR_MORE_THEN_OR_EQUALS.equals(operatorStr))
            condition.setMoreThenOrEquals(bound);
        else if (OPERATOR_LESS_THEN_OR_EQUALS.equals(operatorStr))
            condition.setLessThenOrEquals(bound);
        else
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "operator[" + operatorStr + "] is not supported, use " + OPERATOR_LESS_THEN_OR_EQUALS + " or " + OPERATOR_MORE_THEN_OR_EQUALS);
        TwinFieldFilter fieldsFilter = twinSearch.getFieldsFilter();
        if (fieldsFilter == null)
            twinSearch.setFieldsFilter(fieldsFilter = new TwinFieldFilter());
        fieldsFilter.addClause(new TwinFieldClause().addCondition(condition));
    }

    /**
     * The bound moment for the given day: 00:00, 23:59:59.999999999 or the current time.
     */
    static LocalDateTime resolveBound(LocalDate day, String boundary) throws ServiceException {
        if (BOUNDARY_END_OF_DAY.equals(boundary))
            return day.atTime(LocalTime.MAX);
        if (BOUNDARY_NOW.equals(boundary))
            return LocalDateTime.now();
        if (BOUNDARY_START_OF_DAY.equals(boundary))
            return day.atStartOfDay();
        throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "boundary[" + boundary + "] is not supported, use " + BOUNDARY_START_OF_DAY + ", " + BOUNDARY_END_OF_DAY + " or " + BOUNDARY_NOW);
    }
}
