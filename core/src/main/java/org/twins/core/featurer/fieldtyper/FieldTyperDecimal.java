package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
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
@Slf4j
@Featurer(
        id = FeaturerTwins.ID_1317,
        name = "Decimal",
        description = "Decimal field with dedicated table storage"
)
public class FieldTyperDecimal extends FieldTyperDecimalBase<FieldDescriptorNumeric, FieldValueText, TwinFieldValueSearchNumeric> {

    @Override
    public FieldDescriptorNumeric getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorNumeric()
                .min(min.extract(properties))
                .max(max.extract(properties))
                .step(step.extract(properties))
                .thousandSeparator(thousandSeparator.extract(properties))
                .decimalSeparator(decimalSeparator.extract(properties))
                .decimalPlaces(decimalPlaces.extract(properties))
                .round(round.extract(properties))
                .extraThousandSeparators(extraThousandSeparatorSet.extract(properties))
                .extraDecimalSeparators(extraDecimalSeparatorSet.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, TwinFieldDecimalEntity twinFieldDecimalEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (value.isUndefined())
            return;
        if (twinFieldDecimalEntity == null && value.isNotEmpty()) {
            // create
            twinFieldDecimalEntity = twinService.createTwinFieldDecimalEntity(twin, value.getTwinClassField(), null);
            twinChangesCollector.add(twinFieldDecimalEntity);
            detectValueChange(twinFieldDecimalEntity, twinChangesCollector, processValue(properties, twinFieldDecimalEntity, value));
        } else if (twinFieldDecimalEntity != null && value.isCleared()) {
            // delete
            twinChangesCollector.delete(twinFieldDecimalEntity);
            addHistoryContext(twinChangesCollector, twinFieldDecimalEntity, null);
        } else if (twinFieldDecimalEntity != null && value.isNotEmpty()) {
            // update
            twinChangesCollector.add(twinFieldDecimalEntity);
            detectValueChange(twinFieldDecimalEntity, twinChangesCollector, processValue(properties, twinFieldDecimalEntity, value));
        }
    }


    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldDecimalEntity twinFieldDecimalEntity) throws ServiceException {
        return deserializeValueBase(properties, twinField, twinFieldDecimalEntity);
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldValueSearchNumeric search) throws ServiceException {
        return TwinSpecification.checkFieldDecimal(search);
    }

    @Override
    protected ValidationResult validate(Properties properties, TwinEntity twin, FieldValueText fieldValue) throws ServiceException {
        var ret = new ValidationResult(true);
        try {
            processAndFormatValue(properties, fieldValue);
        } catch (ServiceException e) {
            ret.setValid(false).addMessage(e.getMessage());
        }

        return ret;
    }

    private BigDecimal processValue(Properties properties, TwinFieldDecimalEntity twinFieldDecimal, FieldValueText value) throws ServiceException {
        return new BigDecimal(processAndFormatValue(properties, value));
    }
}
