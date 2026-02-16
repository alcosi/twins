package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcChildrenInStatusCount;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1314,
        name = "Count children twins by child-status (on fly)",
        description = "Get count of child-twins by child-status(inc/exc) on fly")
public class FieldTyperCountChildrenByStatusV1 extends FieldTyperImmutable<FieldDescriptorText, FieldValueText, TwinFieldStorageCalcChildrenInStatusCount, TwinFieldSearchNotImplemented> implements FieldTyperCountChildrenByStatus {
    public static final Integer ID = 1314;

    @Autowired
    TwinFieldSimpleRepository twinFieldSimpleRepository;

    @Deprecated
    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorText();
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(getCountResult(properties, twinField.getTwin(), twinFieldSimpleRepository).toString());
    }

    @Override
    public TwinFieldStorage getStorage(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new TwinFieldStorageCalcChildrenInStatusCount(
                twinFieldSimpleRepository,
                twinClassFieldEntity.getId(),
                childrenTwinStatusIdList.extract(properties),
                exclude.extract(properties));
    }
}
