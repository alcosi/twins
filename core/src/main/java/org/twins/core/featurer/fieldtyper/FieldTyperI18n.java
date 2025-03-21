package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldI18nEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorI18n;
import org.twins.core.featurer.fieldtyper.value.FieldValueI18n;
import org.twins.core.service.twin.TwinService;

import java.util.Locale;
import java.util.Properties;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_1320,
        name = "i18n",
        description = "")
public class FieldTyperI18n extends FieldTyper<FieldDescriptorI18n, FieldValueI18n, TwinFieldI18nEntity, TwinFieldSearchNotImplemented> {
    @Lazy
    private final TwinService twinService;

    @Override
    protected FieldDescriptorI18n getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorI18n();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueI18n value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (value.getTwinClassField().getRequired() && MapUtils.isEmpty(value.getTranslations())) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED,
                    value.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        }

        twinService.loadTwinFields(twin);

        Map<Locale, TwinFieldI18nEntity> storedFields = getStoredFieldsForTwinAndField(twin, value.getTwinClassField());

        for (Map.Entry<Locale, String> entry : value.getTranslations().entrySet()) {
            Locale locale = entry.getKey();
            String translation = entry.getValue();

            TwinFieldI18nEntity storedField = storedFields.get(locale);

            if (storedField == null) {
                TwinFieldI18nEntity newTwinFieldI18n = new TwinFieldI18nEntity()
                        .setTwin(twin)
                        .setTwinId(twin.getId())
                        .setTwinClassFieldId(value.getTwinClassField().getId())
                        .setLocale(locale)
                        .setTranslation(translation);

                if (twinChangesCollector.isHistoryCollectorEnabled()) {
                    twinChangesCollector.getHistoryCollector(twin).add(historyService.fieldChangeI18n(
                            value.getTwinClassField(),
                            null,
                            null,
                            locale,
                            translation
                    ));
                }

                twinChangesCollector.add(newTwinFieldI18n);
            } else {
                if (!storedField.getTranslation().equals(translation)) {
                    if (twinChangesCollector.isHistoryCollectorEnabled()) {
                        twinChangesCollector.getHistoryCollector(twin).add(historyService.fieldChangeI18n(
                                value.getTwinClassField(),
                                storedField.getLocale(),
                                storedField.getTranslation(),
                                locale,
                                translation
                        ));
                    }

                    storedField.setTranslation(translation);
                    twinChangesCollector.add(storedField);
                }
            }
        }

        for (Map.Entry<Locale, TwinFieldI18nEntity> entry : storedFields.entrySet()) {
            if (!value.getTranslations().containsKey(entry.getKey())) {
                if (twinChangesCollector.isHistoryCollectorEnabled()) {
                    twinChangesCollector.getHistoryCollector(twin).add(historyService.fieldChangeI18n(
                            value.getTwinClassField(),
                            entry.getKey(),
                            entry.getValue().getTranslation(),
                            null,
                            null
                    ));
                }

                twinChangesCollector.delete(entry.getValue());
            }
        }
    }


    @Override
    protected FieldValueI18n deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        Map<Locale, TwinFieldI18nEntity> storedFields = getStoredFieldsForTwinAndField(twinField.getTwin(), twinField.getTwinClassField());

        FieldValueI18n fieldValue = new FieldValueI18n(twinField.getTwinClassField());
        for (TwinFieldI18nEntity storedField : storedFields.values()) {
            fieldValue.addTranslation(storedField.getLocale(), storedField.getTranslation());
        }

        return fieldValue;
    }

    private Map<Locale, TwinFieldI18nEntity> getStoredFieldsForTwinAndField(TwinEntity twin, TwinClassFieldEntity twinClassField) {
        return twin.getTwinFieldI18nKit().getMap().values().stream()
                .filter(field -> field.getTwinClassFieldId().equals(twinClassField.getId()))
                .collect(Collectors.toMap(TwinFieldI18nEntity::getLocale, Function.identity()));
    }
}



