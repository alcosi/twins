package org.twins.core.featurer.fieldrule.conditionevaluator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.params.FeaturerParamString;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.enums.twinclass.TwinClassFieldConditionOperator;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptor;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.featurer.params.FeaturerParamStringTwinsConditionOperatorType;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@FeaturerType(id = FeaturerTwins.TYPE_45,
        name = "ConditionEvaluator",
        description = "Evaluates conditions on twin class fields")
@Slf4j
public abstract class ConditionEvaluator<D extends ConditionDescriptor> extends FeaturerTwins {
    public static final String CONDITION_OPERATOR = "conditionOperator";
    public static final String VALUE_TO_COMPARE_WITH = "valueToCompareWith";

    private Class<D> descriptorType = null;

    @FeaturerParam(name = "ValueToCompareWith", description = "", order = 1)
    public static final FeaturerParamString valueToCompareWith = new FeaturerParamString(VALUE_TO_COMPARE_WITH);

    @FeaturerParam(name = "ConditionOperator", description = "", order = 2)
    public static final FeaturerParamStringTwinsConditionOperatorType conditionOperator = new FeaturerParamStringTwinsConditionOperatorType(CONDITION_OPERATOR);

    public ConditionEvaluator() {
        List<Type> collected = collectParameterizedTypes(getClass(), new ArrayList<>());
        for (Type ptType : collected) {
            if (!(ptType instanceof Class<?> cl))
                continue;
            if (ConditionDescriptor.class.isAssignableFrom(cl) && descriptorType == null)
                //noinspection unchecked
                descriptorType = (Class<D>) cl;
        }
        if (descriptorType == null)
            throw new RuntimeException("Can not initialize ConditionEvaluator: descriptor type not resolved for " + getClass().getSimpleName());
    }

    public Class<D> getDescriptorType() {
        return descriptorType;
    }

    public D getConditionDescriptor(TwinClassFieldConditionEntity twinClassFieldConditionEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinClassFieldConditionEntity.getConditionEvaluatorParams());
        return getConditionDescriptor(twinClassFieldConditionEntity, properties);
    }

    protected abstract D getConditionDescriptor(TwinClassFieldConditionEntity twinClassFieldConditionEntity, Properties properties) throws ServiceException;

    public boolean evaluate(TwinClassFieldConditionEntity twinClassFieldConditionEntity, FieldValue currentValue) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinClassFieldConditionEntity.getConditionEvaluatorParams());
        return evaluate(twinClassFieldConditionEntity, properties, currentValue);
    }

    protected abstract boolean evaluate(TwinClassFieldConditionEntity twinClassFieldConditionEntity, Properties properties, FieldValue currentValue) throws ServiceException;

    protected static boolean evaluateOperator(String actualValue, TwinClassFieldConditionOperator operator, String expected) {
        if (expected == null)
            expected = "";
        else
            expected = expected.trim();

        boolean isNullish = StringUtils.isEmpty(actualValue);

        switch (operator) {
            case eq:
                if ("null".equalsIgnoreCase(expected))
                    return isNullish;
                return Strings.CI.equals(actualValue, expected);
            case neq:
                if ("null".equalsIgnoreCase(expected))
                    return !isNullish;
                if (actualValue == null)
                    return false;
                return !Strings.CI.equals(actualValue, expected);
            case lt:
                Integer ltCompare = compareNumbers(actualValue, expected);
                return ltCompare != null && ltCompare < 0;
            case gt:
                Integer gtCompare = compareNumbers(actualValue, expected);
                return gtCompare != null && gtCompare > 0;
            case contains:
                if (actualValue == null)
                    return false;
                return Strings.CI.equals(actualValue, expected);
            case in:
                Set<String> expectedOptions = splitValues(expected);
                if (expectedOptions.isEmpty())
                    return false;
                if (isNullish)
                    return expectedOptions.contains("null");
                Set<String> actualOptions = splitValues(actualValue);
                for (String actualOption : actualOptions) {
                    if (expectedOptions.contains(actualOption))
                        return true;
                }
                return false;
            default:
                return false;
        }
    }

    protected static String normalizeValue(Object value) {
        if (value == null)
            return null;
        if (value instanceof FieldValue fieldValue && fieldValue.isEmpty())
            return null;
        if (value instanceof FieldValueText t)
            return normalizeValue(t.getValue());
        if (value instanceof FieldValueColorHEX t)
            return normalizeValue(t.getValue());
        if (value instanceof FieldValueBoolean b)
            return normalizeValue(b.getValue());
        if (value instanceof FieldValueDate d)
            return normalizeValue(d.getDateStr());
        if (value instanceof FieldValueSelect s)
            return normalizeValue(s.getItems());
        if (value instanceof FieldValueUser u)
            return normalizeValue(u.getItems());
        if (value instanceof FieldValueLink l)
            return normalizeValue(l.getItems());

        if (value instanceof DataListOptionEntity o)
            return o.getId() != null ? o.getId().toString() : null;
        if (value instanceof UserEntity u)
            return u.getId() != null ? u.getId().toString() : null;
        if (value instanceof TwinLinkEntity l)
            return l.getDstTwinId() != null ? l.getDstTwinId().toString() : null;

        if (value instanceof Collection<?> c) {
            return c.stream()
                    .map(ConditionEvaluator::normalizeValue)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.joining(","));
        }

        if (value instanceof Boolean b)
            return b ? "true" : "false";

        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            Object id = map.get("id");
            if (id != null)
                return normalizeValue(id);
        }

        return value.toString().trim();
    }

    protected static Integer compareNumbers(String actual, String expected) {
        if (StringUtils.isBlank(actual) || StringUtils.isBlank(expected))
            return null;
        try {
            BigDecimal ac = new BigDecimal(actual.trim());
            BigDecimal ex = new BigDecimal(expected.trim());
            return ac.compareTo(ex);
        } catch (NumberFormatException e) {
            log.warn("Cannot compare numbers: {} vs {}", actual, expected, e);
            return null;
        }
    }

    protected static Set<String> splitValues(String value) {
        if (StringUtils.isBlank(value))
            return Collections.emptySet();

        Set<String> result = new LinkedHashSet<>();
        String[] parts = value.split("[;,]");
        for (String part : parts) {
            String normalized = part.trim();
            if (StringUtils.isNotEmpty(normalized))
                result.add(normalized.toLowerCase(Locale.ROOT));
        }
        return result;
    }
}
