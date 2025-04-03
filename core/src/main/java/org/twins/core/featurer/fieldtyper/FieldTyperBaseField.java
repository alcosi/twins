package org.twins.core.featurer.fieldtyper;

import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.domain.search.TwinFieldSearchText;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorBase;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSpirit;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueBase;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1321,
        name = "Base",
        description = "")
public class FieldTyperBaseField extends FieldTyper<FieldDescriptorBase, FieldValueBase, TwinFieldSimpleEntity, TwinFieldSearchNotImplemented> {

    @Override
    public FieldDescriptorBase getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorBase();
    }

    @Override
    protected FieldValueBase deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        return null;
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueBase value, TwinChangesCollector twinChangesCollector) throws ServiceException {

    }

    protected void serializeValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    protected FieldValueBase deserializeValue(Properties properties, TwinField twinField, TwinFieldSimpleEntity twinFieldEntity) {
        return new FieldValueBase(twinField.getTwinClassField()).setValue(twinFieldEntity != null && twinFieldEntity.getValue() != null ? twinFieldEntity.getValue() : null);
    }

    public Specification<TwinEntity> searchBy(TwinFieldSearchNotImplemented search) throws ServiceException {
        return null;
    }


}
