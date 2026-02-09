package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamRoundingMode;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
@Featurer(id = FeaturerTwins.ID_1340, name = "Sum fields", description = "Sum of fields")
public class FieldTyperCalcSum extends FieldTyper<FieldDescriptorText, FieldValueText, TwinFieldStorageSimple, TwinFieldSearchNotImplemented> {

    @FeaturerParam(name = "fieldIds", description = "Fields to sum")
    public static final FeaturerParamUUIDSetTwinsTwinClassFieldId fieldIds = new FeaturerParamUUIDSetTwinsTwinClassFieldId("fieldIds");

    @FeaturerParam(name = "Decimal places",
            description = "Number of decimal places.",
            optional = true,
            defaultValue = "2")
    FeaturerParamInt decimalPlaces = new FeaturerParamInt("decimalPlaces");

    @FeaturerParam(
            name = "Rounding mode",
            description = "Rounding mode for decimal scaling",
            optional = true,
            defaultValue = "HALF_UP"
    )
    FeaturerParamRoundingMode roundingMode = new FeaturerParamRoundingMode("roundingMode");

    @Override
    protected FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorText();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        Kit<TwinFieldSimpleEntity, UUID> twinFieldSimpleKit = twinField.getTwin().getTwinFieldSimpleKit();
        Set<UUID> extractedTwinFields = fieldIds.extract(properties);
        BigDecimal totalSum = BigDecimal.ZERO;

        for (UUID twinFieldId : extractedTwinFields) {
            BigDecimal value = FieldTyperNumeric.parseBigDecimalValue(twinField.getTwin(), twinFieldId, BigDecimal.ZERO);
            totalSum = totalSum.add(value);
        }

        // Apply rounding if parameters are specified
        Integer scale = decimalPlaces.extract(properties);
        RoundingMode roundingModeParam = roundingMode.extract(properties);

        if (scale != null) {
            totalSum = totalSum.setScale(scale, roundingModeParam);
            totalSum = totalSum.stripTrailingZeros();
        }

        return new FieldValueText(twinField.getTwinClassField()).setValue(totalSum.toPlainString());
    }
}
