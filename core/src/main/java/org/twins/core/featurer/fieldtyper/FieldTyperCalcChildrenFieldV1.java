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

import static org.cambium.common.util.StringUtils.fmt;

@Component
@Featurer(id = 1312,
        name = "FieldTyperCalcChildrenFieldV1",
        description = "Get sum of child.fields.values on fly")
public class FieldTyperCalcChildrenFieldV1 extends FieldTyper<FieldDescriptorText, FieldValueText> implements FieldTyperCalcChildrenField {
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
    protected void serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinFieldEntity twinFieldEntity) throws ServiceException {
        double result =
                exclude.extract(properties) ?
                        twinFieldRepository.sumChildrenTwinFieldValuesWithStatusNotIn(childrenTwinClassFieldId.extract(properties), childrenTwinStatusIdList.extract(properties)) :
                        twinFieldRepository.sumChildrenTwinFieldValuesWithStatusIn(childrenTwinClassFieldId.extract(properties), childrenTwinStatusIdList.extract(properties));

        return new FieldValueText().setValue(fmt(result));
    }
}
