package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

import static org.cambium.common.util.StringUtils.fmt;

@Component
@Featurer(id = FeaturerTwins.ID_1313,
        name = "FieldTyperCalcChildrenFieldV2",
        description = """
Save sum of child.fields.values on serializeValue, and return saved total from database
                              """)
public class FieldTyperCalcChildrenFieldV2 extends FieldTyperSimple<FieldDescriptorText, FieldValueText> implements FieldTyperCalcChildrenField {
    public static final Integer ID = 1313;

    @Autowired
    TwinFieldSimpleRepository twinFieldSimpleRepository;

    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorText();
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        detectValueChange(twinFieldEntity, twinChangesCollector, fmt(getSumResult(properties, twinFieldEntity.getTwin(), twinFieldSimpleRepository)));
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldSimpleEntity twinFieldEntity) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(fmt(parseTwinFieldValue(twinFieldEntity)));
    }
}
