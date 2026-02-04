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
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumOfDivisionsByHead;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1342,
        name = "Fields sum of divisions by head",
        description = "Fields sum of divisions by head twin")
@RequiredArgsConstructor
public class FieldTyperCalcSumOfDivisionsByHead extends FieldTyperCalcBinaryByHead<FieldDescriptorText, FieldValueText, TwinFieldStorageCalcSumOfDivisionsByHead, TwinFieldSearchNotImplemented> {

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
        Object calcValue = twinField.getTwin().getTwinFieldCalculated().get(twinField.getTwinClassFieldId());
        if (calcValue == null) {
            return new FieldValueText(twinField.getTwinClassField()).setValue(null);
        }

        // Convert to BigDecimal for rounding
        BigDecimal value;
        if (calcValue instanceof BigDecimal) {
            value = (BigDecimal) calcValue;
        } else {
            value = new BigDecimal(calcValue.toString());
        }

        // Apply rounding if parameters are specified
        Integer scale = decimalPlaces.extract(properties);
        RoundingMode roundingModeParam = roundingMode.extract(properties);

        if (scale != null) {
            value = value.setScale(scale, roundingModeParam);
            value = value.stripTrailingZeros();
        }

        return new FieldValueText(twinField.getTwinClassField()).setValue(value.toPlainString());
    }

    @Override
    public TwinFieldStorage getStorage(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new TwinFieldStorageCalcSumOfDivisionsByHead(
                twinClassFieldEntity.getId(),
                twinFieldDecimalRepository,
                firstFieldId.extract(properties),
                secondFieldId.extract(properties),
                childrenTwinInStatusIds.extract(properties),
                childrenTwinOfClassIds.extract(properties),
                statusExclude.extract(properties),
                throwOnDivisionByZero.extract(properties)
        );
    }
}
