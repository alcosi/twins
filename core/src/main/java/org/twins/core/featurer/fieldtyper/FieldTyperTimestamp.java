package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldTimestampEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldValueSearchDate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorDate;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTimestamp;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;
import org.twins.core.service.history.HistoryItem;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

@Component
@Slf4j
@Featurer(
        id = FeaturerTwins.ID_1302,
        name = "Timestamp",
        description = "Timestamp field with dedicated table storage"
)
public class FieldTyperTimestamp extends FieldTyperSingleValue<
        FieldDescriptorDate,
        FieldValueDate,
        TwinFieldTimestampEntity,
        Timestamp,
        TwinFieldStorageTimestamp,
        TwinFieldValueSearchDate> implements FieldTyperDateTime{

    @FeaturerParam(name = "HoursPast", description = "max hours in the past", optional = true, defaultValue = "-1")
    public static final FeaturerParamInt hoursPast = new FeaturerParamInt("hoursPast");

    @FeaturerParam(name = "HoursFuture", description = "max hours in the future", optional = true, defaultValue = "-1")
    public static final FeaturerParamInt hoursFuture = new FeaturerParamInt("hoursFuture");

    @Override
    public FieldDescriptorDate getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        LocalDateTime now = LocalDateTime.now();
        FieldDescriptorDate fieldDescriptorDate = new FieldDescriptorDate()
                .pattern(pattern.extract(properties))
                .beforeDate(now.minusHours(hoursPast.extract(properties)))
                .afterDate(now.plusHours(hoursFuture.extract(properties)));
        fieldDescriptorDate.backendValidated(false);
        return fieldDescriptorDate;
    }

    @Override
    protected void setEntityValue(TwinFieldTimestampEntity twinFieldEntity, Timestamp newValue) {
        twinFieldEntity.setValue(newValue);
    }

    @Override
    protected Timestamp getEntityValue(TwinFieldTimestampEntity twinFieldEntity) {
        return twinFieldEntity.getValue();
    }

    @Override
    protected Kit<TwinFieldTimestampEntity, UUID> getFieldKit(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldTimestampKit();
    }

    @Override
    protected TwinFieldTimestampEntity createTwinFieldEntity(TwinEntity twin, TwinClassFieldEntity twinClassField) {
        return TwinFieldTimestampEntity.of(twin, twinClassField);
    }

    @Override
    protected Timestamp processValue(Properties properties, TwinFieldTimestampEntity twinFieldEntity, FieldValueDate value) {
        return Timestamp.valueOf(value.getDate());
    }

    @Override
    protected HistoryItem<?> createHistoryItem(TwinFieldTimestampEntity twinFieldEntity, Timestamp newValue) {
        return historyService.fieldChangeTimestamp(
                twinFieldEntity.getTwinClassField(),
                twinFieldEntity.getValue(),
                newValue
        );
    }

    @Override
    protected FieldValueDate deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        var twinFieldEntity = resolveTwinFieldEntity(twinField.getTwin(), twinField.getTwinClassField());
        var fieldValueDate = new FieldValueDate(twinField.getTwinClassField(), pattern.extract(properties));
        if (twinFieldEntity != null && twinFieldEntity.getValue() != null) {
            fieldValueDate.setDate(twinFieldEntity.getValue().toLocalDateTime());
        } else
            fieldValueDate.undefine();
        return fieldValueDate;
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldValueSearchDate search) {
        return TwinSpecification.checkFieldTimestamp(search);
    }

    @Override
    public ValidationResult validate(Properties properties, TwinEntity twin, FieldValueDate value) {
        ValidationResult result = new ValidationResult(true);
        try {
            LocalDateTime localDateTime = value.getDate();
            LocalDateTime now = LocalDateTime.now();
            Integer minHours = hoursPast.extract(properties);
            if (minHours != null && minHours >= 0) {
                LocalDateTime minDate = now.minusHours(minHours);
                if (localDateTime.isBefore(minDate)) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Date value [" + localDateTime + "] is more than " + minHours + " hours in the past");
                }
            }
            Integer maxHours = hoursFuture.extract(properties);
            if (maxHours != null && maxHours >= 0) {
                LocalDateTime maxDate = now.plusHours(maxHours);
                if (localDateTime.isAfter(maxDate)) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Date value [" + localDateTime + "] is more than " + maxHours + " hours in the future");
                }
            }
        } catch (ServiceException e) {
            result = new ValidationResult(false, i18nService.translateToLocale(value.getTwinClassField().getBeValidationErrorI18nId()));
        }
        return result;
    }
}