package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

public interface FieldTyperCalcMater {

    @FeaturerParam(
            name = "requiredField",
            description = "If true, throw error when operand field is missing or empty. If false, skip serialization",
            optional = true,
            defaultValue = "false",
            order = 200
    )
    FeaturerParamBoolean requiredField = new FeaturerParamBoolean("requiredField");

    default boolean shouldSkipSerializeOnMissingOperands(TwinEntity twin, Properties properties, TwinClassFieldService twinClassFieldService, Collection<UUID> operandFieldIds, TwinClassFieldEntity twinClassField) throws ServiceException {
        if (operandFieldIds == null || operandFieldIds.isEmpty()) {
            if (Boolean.TRUE.equals(requiredField.extract(properties))) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, "Operand fields are not configured for {}", twinClassField.logNormal());
            }
            return true;
        }

        for (UUID operandFieldId : operandFieldIds) {
            if (twinClassFieldService.isDecimalFieldEmpty(twin, operandFieldId)) {
                if (Boolean.TRUE.equals(requiredField.extract(properties))) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, "Operand field [{}] is required for {}", operandFieldId, twinClassField.logNormal());
                }
                return true;
            }
        }

        return false;
    }
}
