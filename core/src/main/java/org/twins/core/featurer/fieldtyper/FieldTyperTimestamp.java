package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldTimestampEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchDate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorDate;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTimestamp;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Properties;

@Component
@Slf4j
@Featurer(
        id = FeaturerTwins.ID_1302,
        name = "Timestamp",
        description = "Timestamp field with dedicated table storage"
)
public class FieldTyperTimestamp extends FieldTyper<FieldDescriptorDate, FieldValueDate, TwinFieldStorageTimestamp, TwinFieldSearchDate> implements FieldTyperDateTime{

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
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueDate value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (value.isUndefined())
            return;

        var twinFieldTimestampEntity = convertToTwinFieldTimestampEntity(twin, value.getTwinClassField());

        if (twinFieldTimestampEntity == null && value.isNotEmpty()) {
            // create field
            twinFieldTimestampEntity = twinService.createTwinFieldTimestampEntity(twin, value.getTwinClassField(), null);
            twinChangesCollector.add(twinFieldTimestampEntity);
            detectValueChange(twinFieldTimestampEntity, twinChangesCollector, Timestamp.valueOf(value.getDate()));
        } else if (twinFieldTimestampEntity != null && value.isCleared()) {
            // delete field
            twinChangesCollector.delete(twinFieldTimestampEntity);
            addHistoryContext(twinChangesCollector,  twinFieldTimestampEntity, null);
        } else if (twinFieldTimestampEntity != null && value.isNotEmpty()) {
            // update field
            twinChangesCollector.add(twinFieldTimestampEntity);
            detectValueChange(twinFieldTimestampEntity, twinChangesCollector, Timestamp.valueOf(value.getDate()));
        }
    }

    @Override
    protected FieldValueDate deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        var twinFieldEntity = convertToTwinFieldTimestampEntity(twinField.getTwin(), twinField.getTwinClassField());
        var fieldValueDate = new FieldValueDate(twinField.getTwinClassField(), pattern.extract(properties));
        if (twinFieldEntity != null && twinFieldEntity.getValue() != null) {
            fieldValueDate.setDate(twinFieldEntity.getValue().toLocalDateTime());
        } else
            fieldValueDate.undefine();
        return fieldValueDate;
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchDate search) {
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

    private void detectValueChange(TwinFieldTimestampEntity twinFieldTimestampEntity, TwinChangesCollector twinChangesCollector, Timestamp newValue) {
        if (twinChangesCollector.collectIfChanged(twinFieldTimestampEntity, "field[" + twinFieldTimestampEntity.getTwinClassField().getKey() + "]", twinFieldTimestampEntity.getValue(), newValue)) {
            addHistoryContext(twinChangesCollector, twinFieldTimestampEntity, newValue);
            twinFieldTimestampEntity.setValue(newValue);
        }
    }

    private void addHistoryContext(TwinChangesCollector twinChangesCollector, TwinFieldTimestampEntity twinFieldTimestampEntity, Timestamp newValue) {
        if (twinChangesCollector.isHistoryCollectorEnabled()) {
            twinChangesCollector
                    .getHistoryCollector(twinFieldTimestampEntity.getTwin())
                    .add(
                            historyService.fieldChangeTimestamp(
                                    twinFieldTimestampEntity.getTwinClassField(),
                                    twinFieldTimestampEntity.getValue(),
                                    newValue
                            )
                    );
        }
    }

    private TwinFieldTimestampEntity convertToTwinFieldTimestampEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        twinService.loadTwinFields(twinEntity);
        return twinEntity.getTwinFieldTimestampKit().get(twinClassFieldEntity.getId());
    }
}