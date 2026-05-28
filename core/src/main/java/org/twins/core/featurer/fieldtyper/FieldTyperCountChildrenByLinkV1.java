package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcBackwardLinkedTwinCount;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1351,
        name = "Count linked twins by link and status (on fly)",
        description = "Get count of linked twins by link and status(inc/exc) on fly")
public class FieldTyperCountChildrenByLinkV1 extends FieldTyperImmutable<FieldDescriptorText, FieldValueText, TwinFieldStorageCalcBackwardLinkedTwinCount, TwinFieldSearchNotImplemented> implements FieldTyperCountChildrenByLink {
    public static final Integer ID = 1351;

    @Autowired
    TwinRepository twinRepository;

    @Deprecated
    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorText();
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(twinField.getTwin().getTwinFieldCalculated().get(twinField.getTwinClassFieldId()).toString());
    }

    @Override
    public TwinFieldStorage getStorage(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new TwinFieldStorageCalcBackwardLinkedTwinCount(
                twinRepository,
                twinClassFieldEntity.getId(),
                linkIds.extract(properties),
                linkedTwinStatusIdList.extract(properties),
                exclude.extract(properties));
    }
}
