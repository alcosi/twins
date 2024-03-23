package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSpirit;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

import static org.cambium.common.util.StringUtils.fmt;

@Component
@Featurer(id = 1312,
        name = "FieldTyperCalcChildrenFieldV1",
        description = "Get sum of child.fields.values on fly")
public class FieldTyperCalcChildrenFieldV1 extends FieldTyper<FieldDescriptorText, FieldValueText, TwinFieldStorageSpirit> implements FieldTyperCalcChildrenField {
    public static final Integer ID = 1312;

    @Autowired
    TwinFieldRepository twinFieldRepository;

    @Deprecated
    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorText();
    }

    @Deprecated
    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField(), true)
                .setValue(fmt(getSumResult(properties, twinField.getTwin(), twinFieldRepository)));
    }
}
