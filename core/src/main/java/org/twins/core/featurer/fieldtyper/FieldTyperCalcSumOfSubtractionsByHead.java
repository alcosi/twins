package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumOfSubtractionsByHead;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(
        id = FeaturerTwins.ID_1344,
        name = "Fields sum of subtractions by head",
        description = "Fields sum of subtractions by head twin"
)
@RequiredArgsConstructor
public class FieldTyperCalcSumOfSubtractionsByHead extends FieldTyperCalcBinaryByHead<FieldDescriptorText, FieldValueText, TwinFieldStorageCalcSumOfSubtractionsByHead, TwinFieldSearchNotImplemented> {

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;

    @Override
    protected FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorText();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {

    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(String.valueOf(twinField.getTwin().getTwinFieldCalculated().get(twinField.getTwinClassFieldId())));
    }

    @Override
    public TwinFieldStorage getStorage(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new TwinFieldStorageCalcSumOfSubtractionsByHead(
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
