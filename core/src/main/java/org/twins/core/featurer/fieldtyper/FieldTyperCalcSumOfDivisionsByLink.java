package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
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
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumOfDivisionsByLink;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1346,
        name = "Fields sum of divisions by link",
        description = "Fields sum of divisions by link twin")
@RequiredArgsConstructor
public class FieldTyperCalcSumOfDivisionsByLink extends FieldTyperCalcBinaryByLink<FieldDescriptorText, FieldValueText, TwinFieldStorageCalcSumOfDivisionsByLink, TwinFieldSearchNotImplemented> {

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;

    @FeaturerParam(name = "Throw on division by zero", order = 6, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean throwOnDivisionByZero = new FeaturerParamBoolean("throwOnDivisionByZero");

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
        return new TwinFieldStorageCalcSumOfDivisionsByLink(
                twinClassFieldEntity.getId(),
                twinFieldDecimalRepository,
                firstFieldId.extract(properties),
                secondFieldId.extract(properties),
                linkId.extract(properties),
                srcElseDst.extract(properties),
                linkedTwinInStatusIdSet.extract(properties),
                linkedTwinOfClassIds.extract(properties),
                statusExclude.extract(properties),
                throwOnDivisionByZero.extract(properties)
        );
    }
}
