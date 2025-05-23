package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorBoolean;
import org.twins.core.featurer.fieldtyper.value.FieldValueBoolean;
import org.twins.core.featurer.params.FeaturerParamStringTwinsCheckboxType;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1306,
        name = "Boolean",
        description = "")
@RequiredArgsConstructor
public class FieldTyperBoolean extends FieldTyper<FieldDescriptorBoolean, FieldValueBoolean, TwinFieldBooleanEntity, TwinFieldSearchNotImplemented> {
    @FeaturerParam(name = "CheckboxType", description = "", order = 1, optional = true, defaultValue = "TOGGLE")
    public static final FeaturerParamStringTwinsCheckboxType checkboxType = new FeaturerParamStringTwinsCheckboxType("checkboxType");

    @Lazy
    private final TwinService twinService;

    @Override
    protected FieldDescriptorBoolean getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorBoolean().checkboxType(checkboxType.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueBoolean value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }


    @Override
    protected FieldValueBoolean deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        twinService.loadTwinFields(twin);

        return new FieldValueBoolean(twinField.getTwinClassField()).setValue(twin.getTwinFieldBooleanKit().get(twinField.getTwinClassFieldId()).isValue());
    }

}
