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
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumOfMultiplicationsByHead;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(
        id = FeaturerTwins.ID_1343,
        name = "Fields sum of multiplications by head",
        description = "Fields sum of multiplications by head twin"
)
@RequiredArgsConstructor
public class FieldTyperCalcSumOfMultiplicationsByHead extends FieldTyperCalcBinaryByHead<FieldDescriptorText, FieldValueText, TwinFieldStorageCalcSumOfMultiplicationsByHead, TwinFieldSearchNotImplemented> {

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;

    @Override
    protected FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorText();
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(
                        scaleAndRound(
                                twinField
                                        .getTwin()
                                        .getTwinFieldCalculated()
                                        .get(twinField.getTwinClassFieldId()),
                                properties
                        ).toPlainString()
                );
    }

    @Override
    public TwinFieldStorage getStorage(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new TwinFieldStorageCalcSumOfMultiplicationsByHead(
                twinClassFieldEntity.getId(),
                twinFieldDecimalRepository,
                firstFieldId.extract(properties),
                secondFieldId.extract(properties),
                childrenTwinInStatusIds.extract(properties),
                childrenTwinOfClassIds.extract(properties),
                statusExclude.extract(properties)
        );
    }
}
