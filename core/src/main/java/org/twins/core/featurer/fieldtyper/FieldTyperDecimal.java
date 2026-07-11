package org.twins.core.featurer.fieldtyper;

import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldValueSearchNumeric;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorNumeric;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDecimal;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.history.HistoryItem;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(
        id = FeaturerTwins.ID_1317,
        name = "Decimal",
        description = "Decimal field with dedicated table storage"
)
public class FieldTyperDecimal extends FieldTyperSingleValue<
        FieldDescriptorNumeric,
        FieldValueText,
        TwinFieldDecimalEntity,
        BigDecimal,
        TwinFieldStorageDecimal,
        TwinFieldValueSearchNumeric> implements FieldTyperNumeric {

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
    protected void setEntityValue(TwinFieldDecimalEntity twinFieldEntity, BigDecimal newValue) {
        twinFieldEntity.setValue(newValue);
    }

    @Override
    protected BigDecimal getEntityValue(TwinFieldDecimalEntity twinFieldEntity) {
        return twinFieldEntity.getValue();
    }

    @Override
    protected Kit<TwinFieldDecimalEntity, UUID> getFieldKit(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldDecimalKit();
    }

    @Override
    protected TwinFieldDecimalEntity createTwinFieldEntity(TwinEntity twin, TwinClassFieldEntity twinClassField) {
        return TwinFieldDecimalEntity.of(twin, twinClassField);
    }

    @Override
    protected BigDecimal processValue(Properties properties, TwinFieldDecimalEntity twinFieldEntity, FieldValueText value) throws ServiceException {
        return new BigDecimal(processAndFormatValue(properties, value));
    }

    @Override
    protected HistoryItem<?> createHistoryItem(TwinFieldDecimalEntity twinFieldEntity, BigDecimal newValue) {
        return historyService.fieldChangeDecimal(
                twinFieldEntity.getTwinClassField(),
                twinFieldEntity.getValue() != null ? twinFieldEntity.getValue() : null,
                newValue
        );
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldDecimalEntity twinFieldEntity) throws ServiceException {
        return deserializeValueBase(properties, twinField, twinFieldEntity);
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
}
