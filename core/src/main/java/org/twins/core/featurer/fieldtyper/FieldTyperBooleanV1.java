package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchBoolean;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorBoolean;
import org.twins.core.featurer.fieldtyper.value.FieldValueBoolean;
import org.twins.core.featurer.params.FeaturerParamStringTwinsCheckboxType;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1306,
        name = "Boolean",
        description = "")
@RequiredArgsConstructor
public class FieldTyperBooleanV1 extends FieldTyperBoolean<FieldDescriptorBoolean, FieldValueBoolean, TwinFieldSearchBoolean> {

    @FeaturerParam(name = "CheckboxType", description = "", order = 1, optional = true, defaultValue = "TOGGLE")
    public static final FeaturerParamStringTwinsCheckboxType checkboxType = new FeaturerParamStringTwinsCheckboxType("checkboxType");

    @Override
    protected FieldDescriptorBoolean getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorBoolean().checkboxType(checkboxType.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldBooleanEntity twinFieldBooleanEntity, FieldValueBoolean value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        detectValueChange(twinFieldBooleanEntity, twinChangesCollector, value.getValue());
    }

    @Override
    protected FieldValueBoolean deserializeValue(Properties properties, TwinField twinField, TwinFieldBooleanEntity twinFieldBooleanEntity) throws ServiceException {
        return new FieldValueBoolean(twinField.getTwinClassField())
                .setValue(twinFieldBooleanEntity != null ? twinFieldBooleanEntity.getValue() : null);
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchBoolean twinFieldSearchBoolean) {
        return Specification.where(TwinSpecification.checkFieldBoolean(twinFieldSearchBoolean));
    }
}
