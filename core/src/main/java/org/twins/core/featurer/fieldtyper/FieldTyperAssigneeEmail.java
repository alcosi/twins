package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorAttachment;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueInvisible;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1318,
        name = "FieldTyperAssigneeEmail",
        description = "Allow the field to have an attachment")
public class FieldTyperAssigneeEmail extends FieldTyper<FieldDescriptorImmutable, FieldValueText, UserEntity> { ;

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
            if (twinFieldSimpleEntity.getTwinClassField().getKey().equals(UserEntity.Fields.avatar))
                value = twinFieldSimpleEntity.getValue();
        }
        return new FieldValueText(twinField.getTwinClassField()).setValue(value);
    }
}
