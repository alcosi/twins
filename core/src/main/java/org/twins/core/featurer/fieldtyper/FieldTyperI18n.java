package org.twins.core.featurer.fieldtyper;

import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.cambium.i18n.dao.I18nEntity;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1320,
        name = "i18n",
        description = "")
public class FieldTyperI18n extends FieldTyper<FieldDescriptorI18n, FieldValueI18n, TwinFieldI18nEntity, TwinFieldSearchNotImplemented> {

    @Autowired
    private TwinService twinService;

    @Override
    protected FieldDescriptorI18n getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorI18n();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueI18n value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (value.getTwinClassField().getRequired() && (value.getI18nId() == null || value.getI18nTranslations() == null || value.getI18nTranslations().isEmpty())) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, value.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        }

        twinService.loadTwinFields(twin);

        TwinFieldI18nEntity storedField = convertToTwinFieldEntity(twin, value.getTwinClassField());

        I18nEntity newI18n = new I18nEntity()
                .setId(value.getI18nId())
                .setTranslations(value.getI18nTranslations());

        if (storedField == null) {
            if (twinChangesCollector.isHistoryCollectorEnabled()) {
                twinChangesCollector.getHistoryCollector(twin).add(historyService.fieldChangeI18n(
                        value.getTwinClassField(),
                        null,
                        newI18n
                ));
            }
            twinChangesCollector.add(new TwinFieldI18nEntity()
                    .setTwin(twin)
                    .setTwinId(twin.getId())
                    .setTwinClassFieldId(value.getTwinClassField().getId())
                    .setI18nId(value.getI18nId())
                    .setI18n(newI18n));
            return;
        }

        I18nEntity oldI18n = storedField.getI18n();
        if (!storedField.getI18nId().equals(value.getI18nId())) {
            if (twinChangesCollector.isHistoryCollectorEnabled()) {
                twinChangesCollector.getHistoryCollector(twin).add(historyService.fieldChangeI18n(
                        value.getTwinClassField(),
                        oldI18n,
                        newI18n
                ));
            }
            twinChangesCollector.add(storedField
                    .setI18nId(value.getI18nId())
                    .setI18n(newI18n));
        } else {
            if (!oldI18n.getTranslations().equals(value.getI18nTranslations())) {
                if (twinChangesCollector.isHistoryCollectorEnabled()) {
                    twinChangesCollector.getHistoryCollector(twin).add(historyService.fieldChangeI18n(
                            value.getTwinClassField(),
                            oldI18n,
                            newI18n
                    ));
                }
                storedField.getI18n().setTranslations(value.getI18nTranslations());
                twinChangesCollector.add(storedField);
            }
        }
    }

    @Override
    protected FieldValueI18n deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinFieldI18nEntity twinFieldEntity = convertToTwinFieldEntity(twinField.getTwin(), twinField.getTwinClassField());

        if (twinFieldEntity == null) {
            return null;
        }

        return new FieldValueI18n(twinField.getTwinClassField())
                .setI18nId(twinFieldEntity.getI18nId());
    }


    protected TwinFieldI18nEntity convertToTwinFieldEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        twinService.loadTwinFields(twinEntity);
        return twinEntity.getTwinFieldI18nKit().get(twinClassFieldEntity.getId());
    }

}



