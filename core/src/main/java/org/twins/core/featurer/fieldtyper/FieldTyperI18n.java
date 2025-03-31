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

import java.util.List;
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
    public static final String ENTRY_SPLITTER = "<@2@>";
    public static final String KEY_VALUE_SPLITTER = "<@3@>";
    public static final String DELETE_TRANSLATION_MARKER = "##DELETE##";

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

            if (DELETE_TRANSLATION_MARKER.equals(translation)) {
                if (storedField != null) {
                    if (twinChangesCollector.isHistoryCollectorEnabled()) {
                        twinChangesCollector.getHistoryCollector(twin).add(historyService.fieldChangeI18n(
                                value.getTwinClassField(),
                                locale,
                                storedField.getTranslation(),
                                null,
                                null
                        ));
                    }
                    twinChangesCollector.delete(storedField);
                    storedFields.remove(locale);
                }
                continue;
            }

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
            } else if (!storedField.getTranslation().equals(translation)) {
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


    @Override
    protected FieldValueI18n deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        twinService.loadTwinFields(twin);

        Map<Locale, TwinFieldI18nEntity> storedFields = getStoredFieldsForTwinAndField(twin, twinField.getTwinClassField());

        FieldValueI18n fieldValue = new FieldValueI18n(twinField.getTwinClassField());
        for (TwinFieldI18nEntity storedField : storedFields.values()) {
            fieldValue.addTranslation(storedField.getLocale(), storedField.getTranslation());
        }

        return fieldValue;
    }

    private Map<Locale, TwinFieldI18nEntity> getStoredFieldsForTwinAndField(TwinEntity twin, TwinClassFieldEntity twinClassField) {
        return twin.getTwinFieldI18nKit().getGrouped(twinClassField.getId()).stream()
                .collect(Collectors.toMap(
                        TwinFieldI18nEntity::getLocale,
                        Function.identity()
                ));
    }
}



