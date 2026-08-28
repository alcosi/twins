package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1315,
        name = "Count children twins",
        description = "Save count of child-twin by child-status(exl/inc) on serializeValue, and return saved total from database")
public class FieldTyperCountChildrenByStatusV2 extends FieldTyperSimple<FieldDescriptorText, FieldValueText, TwinFieldSearchNotImplemented> implements FieldTyperCountChildrenByStatus {
    public static final Integer ID = 1315;

    @Autowired
    TwinRepository twinRepository;

    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorText();
    }

    @Override
    protected String processValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, FieldValueText value) throws ServiceException {
        // Count fields ignore the incoming value — the stored value is computed from children.
        return getCountResult(properties, twinFieldEntity.getTwin(), twinRepository).toString();
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldSimpleEntity twinFieldEntity) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(parseTwinFieldValue(twinFieldEntity).toString());
    }
}
