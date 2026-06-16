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

    @FeaturerParam(name = "required", description = "If true, throw error when operand field is missing or empty. If false, skip serialization", optional = true, defaultValue = "false", order = 200)

    FeaturerParamBoolean required = new FeaturerParamBoolean("required");

    default boolean skipIfEmpty(TwinEntity twin, Properties properties, TwinClassFieldService twinClassFieldService, Collection<UUID> operandFieldIds, TwinClassFieldEntity twinClassField) throws ServiceException {
        if (!Boolean.TRUE.equals(required.extract(properties))) {
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
}
