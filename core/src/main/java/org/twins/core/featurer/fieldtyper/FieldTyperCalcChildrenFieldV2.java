package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twin.TwinFieldRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

import static org.cambium.common.util.StringUtils.fmt;

@Component
@Featurer(id = 1313,
        name = "FieldTyperCalcChildrenFieldV2",
        description = """
Save sum of child.fields.values on serializeValue, and return saved total from database
                              """)
public class FieldTyperCalcChildrenFieldV2 extends FieldTyperBasic<FieldDescriptorText, FieldValueText> implements FieldTyperCalcChildrenField {
    public static final Integer ID = 1313;

    @Autowired
    TwinFieldRepository twinFieldRepository;

    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorText();
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        detectValueChange(twinFieldEntity, twinChangesCollector, fmt(getSumResult(properties, twinFieldEntity.getTwin(), twinFieldRepository)));
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldEntity twinFieldEntity) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField(), twinFieldEntity != null)
                .setValue(fmt(parseTwinFieldValue(twinFieldEntity)));
    }
}
