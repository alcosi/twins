package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.service.I18nService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldI18nEntity;
import org.twins.core.dao.twin.TwinFieldUserEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorI18n;
import org.twins.core.featurer.fieldtyper.value.FieldValueI18n;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.service.twin.TwinService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1320,
        name = "i18n",
        description = "")
public class FieldTyperI18n extends FieldTyper<FieldDescriptorI18n, FieldValueI18n, TwinFieldI18nEntity, TwinFieldSearchNotImplemented> {

    @Autowired
    private TwinService twinService;

    @Override
    protected FieldDescriptorI18n getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorI18n()
                .i18nId(UUID.fromString(properties.getProperty("i18nId")));
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueI18n value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinFieldI18nEntity twinFieldEntity = convertToTwinFieldEntity(twin, value.getTwinClassField());

        if (twinFieldEntity == null) {
            twinFieldEntity = new TwinFieldI18nEntity()
                    .setTwinId(twin.getId())
                    .setTwinClassFieldId(value.getTwinClassField().getId())
                    .setI18nId(value.getI18nId());
            twinChangesCollector.add(twinFieldEntity);
        } else {
            twinFieldEntity.setI18nId(value.getI18nId());
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


//    protected void detectValueChange(TwinFieldI18nEntity twinFieldEntity, TwinChangesCollector twinChangesCollector, I18nEntity i18nEntity) {
//        if (twinChangesCollector.collectIfChanged(twinFieldEntity, "field[" + twinFieldEntity.getTwinClassField().getKey() + "]", twinFieldEntity.getI18nId(), i18nEntity)) {
//            if (twinChangesCollector.isHistoryCollectorEnabled()) {
//                twinChangesCollector.getHistoryCollector(twinFieldEntity.getTwin()).add(
//                        historyService.fieldChangeI18n(twinFieldEntity.getTwinClassField(), twinFieldEntity.getI18n(), i18nEntity));
//                ;
//                twinFieldEntity.setI18nId(i18nEntity.getId());
//            }
//        }
//    }
}



