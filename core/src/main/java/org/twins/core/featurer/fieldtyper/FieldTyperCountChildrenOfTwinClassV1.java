package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcChildrenInStatusCount;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcChildrenOfClassCount;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1333,
        name = "Count children twins by twin class id (on fly)",
        description = "Get count of child-twins by twin class id on fly")
public class FieldTyperCountChildrenOfTwinClassV1 extends FieldTyper<FieldDescriptorText, FieldValueText, TwinFieldStorageCalcChildrenInStatusCount, TwinFieldSearchNotImplemented> implements FieldTyperCountChildrenOfTwinClass {
    @Autowired
    TwinFieldSimpleRepository twinFieldSimpleRepository;

    @Deprecated
    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        //todo maybe need to change descriptor type
        return new FieldDescriptorText();
    }

    @Deprecated
    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(getCountResult(properties, twinField.getTwin(), twinFieldSimpleRepository).toString());
    }

    @Override
    public TwinFieldStorage getStorage(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new TwinFieldStorageCalcChildrenOfClassCount(
                twinFieldSimpleRepository,
                twinClassFieldEntity.getId(),
                twinClassIds.extract(properties));
    }
}
