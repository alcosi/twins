package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumByHead;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Featurer(
        id = FeaturerTwins.ID_1312,
        name = "Sum children field values (on fly)",
        description = "Get sum of child.fields.values on fly"
)
public class FieldTyperCalcChildrenFieldV1 extends FieldTyperImmutable<FieldDescriptorText, FieldValueText, TwinFieldStorageCalcSumByHead, TwinFieldSearchNotImplemented> implements FieldTyperCalcChildrenField {

    public static final Integer ID = 1312;
    private final TwinFieldDecimalRepository twinFieldDecimalRepository;

    @Deprecated
    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorText();
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(String.valueOf(twinField.getTwin().getTwinFieldCalculated().get(twinField.getTwinClassFieldId())));
    }

    @Override
    public TwinFieldStorage getStorage(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new TwinFieldStorageCalcSumByHead(
                twinFieldDecimalRepository,
                twinClassFieldEntity.getId(),
                Set.of(childrenTwinClassFieldId.extract(properties)),
                childrenTwinStatusIdList.extract(properties),
                null,
                exclude.extract(properties)
        );
    }
}
