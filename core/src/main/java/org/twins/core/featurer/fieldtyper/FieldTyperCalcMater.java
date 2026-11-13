package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

public interface FieldTyperCalcMater {

    @FeaturerParam(name = "skipIfEmpty", description = "If true, throw error when operand field is missing or empty. If false, skip serialization", optional = true, defaultValue = "false", order = 200)
    public static final FeaturerParamBoolean skipIfEmpty = new FeaturerParamBoolean("skipIfEmpty");

    @FeaturerParam(name = "Allow negative result", description = "If false, materialization fails when the calculated result is below zero", optional = true, defaultValue = "false", order = 201)
    public static final FeaturerParamBoolean allowNegativeResult = new FeaturerParamBoolean("allowNegativeResult");

    default boolean skipIfEmpty(TwinEntity twin, Properties properties, TwinClassFieldService twinClassFieldService, Collection<UUID> operandFieldIds, TwinClassFieldEntity twinClassField) throws ServiceException {
        if (!Boolean.TRUE.equals(skipIfEmpty.extract(properties))) {
            for (UUID operandFieldId : operandFieldIds) {
                if (twinClassFieldService.isDecimalFieldEmpty(twin, operandFieldId)) {
                    return true;
                }
            }
            return false;
        }
        for (UUID operandFieldId : operandFieldIds) {
            if (twinClassFieldService.isDecimalFieldEmpty(twin, operandFieldId)) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, "Operand field [{}] is required for {}", operandFieldId, twinClassField.logNormal());
            }
        }
        return false;
    }

    /**
     * Gates a materialized calculated result against going below zero. Default ({@code allowNegativeResult=false})
     * rejects negative results with {@link ErrorCodeTwins#TWIN_CLASS_FIELD_VALUE_INCORRECT} instead of silently
     * storing a negative; fields that legitimately can go negative set {@code allowNegativeResult=true} to keep
     * the legacy behavior.
     */
    default BigDecimal checkCalculatedResult(TwinClassFieldEntity twinClassField, BigDecimal calculated, Properties properties) throws ServiceException {
        if (calculated != null
                && calculated.signum() < 0
                && !Boolean.TRUE.equals(allowNegativeResult.extract(properties))) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    "Calculated result[{}] is negative while negative result is not allowed for {}", calculated, twinClassField.logNormal());
        }
        return calculated;
    }
}
