package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTwin;
import org.twins.core.featurer.fieldtyper.value.FieldValueId;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1327,
        name = "BaseId",
        description = "Field typer for base id twin field")
public class FieldTyperBaseIdField extends FieldTyper<FieldDescriptorImmutable, FieldValueId, TwinFieldStorageTwin, TwinFieldSearchNotImplemented> {

    @Override
    public FieldDescriptorImmutable getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorImmutable();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueId value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_IMMUTABLE, "id change is not allowed.");
    }

    @Override
    protected FieldValueId deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        return new FieldValueId(twinField.getTwinClassField()).setValue(twin.getId());
    }
}
