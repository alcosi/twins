package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTwin;
import org.twins.core.featurer.fieldtyper.value.FieldValueStatus;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1324,
        name = "BaseStatus",
        description = "Field typer for base status twin field")
public class FieldTyperBaseStatusField extends FieldTyperImmutable<FieldDescriptorImmutable, FieldValueStatus, TwinFieldStorageTwin, TwinFieldSearchNotImplemented> {

    @Override
    public FieldDescriptorImmutable getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorImmutable();
    }

    @Override
    protected FieldValueStatus deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        return new FieldValueStatus(twinField.getTwinClassField()).setValue(twin.getTwinStatus());
    }
}
