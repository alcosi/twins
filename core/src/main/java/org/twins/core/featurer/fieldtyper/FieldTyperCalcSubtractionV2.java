package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldValueSearchNumeric;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorNumeric;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.math.BigDecimal;
import java.util.Properties;

@Component
@Featurer(
        id = FeaturerTwins.ID_1353,
        name = "Subtraction (saved)",
        description = "Save first minus second field on serializeValue, and return saved total from database"
)
public class FieldTyperCalcSubtractionV2 extends FieldTyperDecimalBase<FieldDescriptorNumeric, FieldValueText, TwinFieldValueSearchNumeric> implements FieldTyperCalcBinary {

    @Override
    public FieldDescriptorNumeric getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorNumeric();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, TwinFieldDecimalEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinFieldEntity == null) {
            twinFieldEntity = twinService.createTwinFieldDecimalEntity(twin, value.getTwinClassField(), null);
            twinChangesCollector.add(twinFieldEntity);
        }
        detectValueChange(twinFieldEntity, twinChangesCollector, calcSubtraction(properties, twin));
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldDecimalEntity twinFieldEntity) throws ServiceException {
        return deserializeValueBase(properties, twinField, twinFieldEntity);
    }

    private BigDecimal calcSubtraction(Properties properties, TwinEntity twin) throws ServiceException {
        var firstValue = twinClassFieldService.getDecimalValue(twin, firstFieldId.extract(properties), BigDecimal.ZERO);
        var secondValue = twinClassFieldService.getDecimalValue(twin, secondFieldId.extract(properties), BigDecimal.ZERO);

        return scaleAndRound(firstValue.subtract(secondValue), properties);
    }
}
