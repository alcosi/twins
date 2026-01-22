package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
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
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumOfMultiplicationsByLink;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumOfSubtractionsByLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1348,
        name = "Fields sum of subtractions by link",
        description = "Fields sum of subtractions by link twin")
@RequiredArgsConstructor
public class FieldTyperCalcSumOfSubtractionsByLink extends FieldTyperCalcBinaryByLink<FieldDescriptorText, FieldValueText, TwinFieldStorageCalcSumOfSubtractionsByLink, TwinFieldSearchNotImplemented> {
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;

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
        return new TwinFieldStorageCalcSumOfSubtractionsByLink(
                twinClassFieldEntity.getId(),
                twinFieldSimpleRepository,
                firstFieldId.extract(properties),
                secondFieldId.extract(properties),
                linkId.extract(properties),
                srcElseDst.extract(properties),
                linkedTwinInStatusIdSet.extract(properties),
                linkedTwinOfClassIds.extract(properties),
                statusExclude.extract(properties));
    }
}
