package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twin.TwinFieldRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = 1314,
        name = "FieldTyperCountChildrenTwinsV1",
        description = "Get count of child-twins by child-status(inc/exc) on fly")
public class FieldTyperCountChildrenTwinsV1 extends FieldTyper<FieldDescriptorText, FieldValueText> implements FieldTyperCountChildrenTwins {
    public static final Integer ID = 1314;

    @Autowired
    TwinFieldRepository twinFieldRepository;

    @Deprecated
    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorText();
    }

    @Deprecated
    @Override
    protected void serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinFieldEntity twinFieldEntity) throws ServiceException {
        return new FieldValueText().setValue(getCountResult(properties, twinFieldEntity, twinFieldRepository).toString());
    }
}