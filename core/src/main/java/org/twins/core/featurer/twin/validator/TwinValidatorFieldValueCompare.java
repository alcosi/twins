package org.twins.core.featurer.twin.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.enums.twinclass.TwinClassFieldConditionOperator;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldrule.conditionevaluator.ConditionEvaluator;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamStringTwinsConditionOperatorType;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

/**
 * Validates a twin's field value against an expected value with an operator (eq/neq/lt/gt/contains/in).
 * Reuses {@link ConditionEvaluator#normalizeValue(Object)} + {@link ConditionEvaluator#evaluateOperator} so the
 * comparison semantics are identical to the field-rule condition evaluator (numeric-aware, else
 * case-insensitive string; {@code in} splits on {@code ,;}).
 */
@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1623,
        name = "Twin field value compare",
        description = "Validates a twin's field value against an expected value (eq/neq/lt/gt/contains/in)")
@RequiredArgsConstructor
public class TwinValidatorFieldValueCompare extends TwinValidator {
    @FeaturerParam(name = "Twin class field id", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "ValueToCompareWith", description = "", order = 2)
    public static final FeaturerParamString valueToCompareWith = new FeaturerParamString("valueToCompareWith");

    @FeaturerParam(name = "ConditionOperator", description = "", order = 3)
    public static final FeaturerParamStringTwinsConditionOperatorType conditionOperator = new FeaturerParamStringTwinsConditionOperatorType("conditionOperator");

    private final TwinService twinService;

    @Override
    protected CollectionValidationResult isValid(Properties properties, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        UUID fieldId = twinClassFieldId.extract(properties);
        String expected = valueToCompareWith.extract(properties);
        TwinClassFieldConditionOperator operator = conditionOperator.extract(properties);

        twinService.loadFieldsValues(twinEntityCollection);

        CollectionValidationResult result = new CollectionValidationResult();
        for (var twinEntity : twinEntityCollection) {
            FieldValue fieldValue = twinEntity.getFieldValuesKit().get(fieldId);
            String actual = ConditionEvaluator.normalizeValue(fieldValue);
            boolean isValid = ConditionEvaluator.evaluateOperator(actual, operator, expected);
            result.getTwinsResults().put(twinEntity.getId(),
                    buildResult(
                            isValid,
                            invert,
                            twinEntity.logShort() + " field[" + fieldId + "] actual[" + actual + "] " + operator + " expected[" + expected + "] failed",
                            twinEntity.logShort() + " field[" + fieldId + "] actual[" + actual + "] " + operator + " expected[" + expected + "] ok"));
        }
        return result;
    }
}
