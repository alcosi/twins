package org.twins.core.featurer.fieldvalidator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamDouble;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.twinclass.FieldValidateBatch;
import org.twins.core.domain.twinclass.FieldValidateItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsDataListOptionId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.UUID;

/**
 * When a data-list field equals a given option, requires this numeric field to satisfy optional
 * inclusive {@code min}/{@code max} bounds. A null bound is not checked.
 * Optionally reads the data-list field from the head (parent) twin.
 */
@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_5604,
        name = "Numeric range when data list",
        description = "When a data-list field equals a given option, requires this numeric field to satisfy optional min/max bounds")
public class FieldValidatorNumericRangeWhenDataList extends FieldValidatorAtomic {

    @FeaturerParam(name = "Data list twin class field", description = "uuid of the data-list twin class field (same twin or parent when onParent=true)", order = 1)
    public static final FeaturerParamUUID dataListTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("dataListTwinClassFieldId");

    @FeaturerParam(name = "Data list option", description = "uuid of the data-list option that activates the range check", order = 2)
    public static final FeaturerParamUUID dataListOptionId = new FeaturerParamUUIDTwinsDataListOptionId("dataListOptionId");

    @FeaturerParam(name = "Min", description = "inclusive lower bound; omit to skip lower check", order = 3, optional = true)
    public static final FeaturerParamDouble min = new FeaturerParamDouble("min");

    @FeaturerParam(name = "Max", description = "inclusive upper bound; omit to skip upper check", order = 4, optional = true)
    public static final FeaturerParamDouble max = new FeaturerParamDouble("max");

    @FeaturerParam(name = "On parent", description = "resolve data-list field from head twin instead of the current twin", order = 5, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean onParent = new FeaturerParamBoolean("onParent");

    @Override
    protected void beforeValidate(FieldValidateBatch batch, Properties properties) throws ServiceException {
        if (Boolean.TRUE.equals(onParent.extract(properties))) {
            twinService.loadHead(batch.getTwins());
            batch.invalidateHeadTwinsCache();
            twinService.loadFieldsValues(batch.getHeadTwins());
        } else {
            twinService.loadFieldsValues(batch.getTwins());
        }
    }

    @Override
    protected ValidationResult isValid(Properties properties, FieldValidateItem item) {
        UUID dataListFieldId = dataListTwinClassFieldId.extract(properties);
        UUID expectedOptionId = dataListOptionId.extract(properties);
        Double minValue = min.extract(properties);
        Double maxValue = max.extract(properties);
        boolean useParent = Boolean.TRUE.equals(onParent.extract(properties));

        FieldValue value = item.getValue();
        if (value == null || value.isEmpty())
            return ValidationResult.VALID;

        FieldValue dataListValue = resolveDataListValue(item, dataListFieldId, useParent);
        if (dataListValue == null || dataListValue.isEmpty())
            return ValidationResult.VALID; // trigger data-list field empty — range rule does not apply
        if (!(dataListValue instanceof FieldValueSelect selectValue)) {
            log.error("{} or data-list twinClassField[{}] is not a data-list field, numeric range when data list can not be applied",
                    value.getTwinClassField().logNormal(), dataListFieldId);
            return new ValidationResult(false);
        }
        if (!containsOption(selectValue, expectedOptionId))
            return ValidationResult.VALID; // other option selected — skip range check

        if (!(value instanceof FieldValueText textValue) || textValue.getValue() == null) {
            log.error("{} is not a numeric text field, numeric range when data list can not be applied",
                    value.getTwinClassField().logNormal());
            return new ValidationResult(false);
        }
        BigDecimal numeric;
        try {
            numeric = new BigDecimal(textValue.getValue().trim());
        } catch (NumberFormatException e) {
            return new ValidationResult(false);
        }
        if (minValue != null && numeric.compareTo(BigDecimal.valueOf(minValue)) < 0)
            return new ValidationResult(false);
        if (maxValue != null && numeric.compareTo(BigDecimal.valueOf(maxValue)) > 0)
            return new ValidationResult(false);
        return ValidationResult.VALID;
    }

    private FieldValue resolveDataListValue(FieldValidateItem item, UUID dataListFieldId, boolean useParent) {
        if (!useParent)
            return resolveFieldValue(item.getTwinEntity(), item.getContextFields(), dataListFieldId);
        TwinEntity headTwin = item.getTwinEntity().getHeadTwin();
        if (headTwin == null || headTwin.getFieldValuesKit() == null)
            return null;
        return headTwin.getFieldValuesKit().get(dataListFieldId);
    }

    private static boolean containsOption(FieldValueSelect selectValue, UUID expectedOptionId) {
        if (expectedOptionId == null)
            return false;
        for (DataListOptionEntity option : selectValue.getItemsOrEmpty()) {
            if (option != null && expectedOptionId.equals(option.getId()))
                return true;
        }
        return false;
    }
}
