package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1319,
        name = "FieldTyperAssigneeAvatar",
        description = "Allow the field to have an attachment")
public class FieldTyperAssigneeAvatar extends FieldTyper<FieldDescriptorImmutable, FieldValueText, UserEntity> {

    @Override
    protected FieldDescriptorImmutable getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        //todo not implemented
        return null;
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {

    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        Collection<TwinFieldSimpleEntity> collection = twinField.getTwin().getTwinFieldSimpleKit().getCollection();
        String value = "";
        for (TwinFieldSimpleEntity twinFieldSimpleEntity : collection) {
            if (twinFieldSimpleEntity.getTwinClassField().getKey().equals(UserEntity.Fields.email))
                value = twinFieldSimpleEntity.getValue();
        }
        return new FieldValueText(twinField.getTwinClassField()).setValue(value);
    }
}
