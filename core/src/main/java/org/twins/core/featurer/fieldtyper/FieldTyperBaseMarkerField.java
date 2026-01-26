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
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1331,
        name = "BaseMarkers",
        description = "Field typer for base markers twin field")
public class FieldTyperBaseMarkerField extends FieldTyper<FieldDescriptorImmutable, FieldValueSelect, TwinFieldStorageTwin, TwinFieldSearchNotImplemented> {

    @Override
    public FieldDescriptorImmutable getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorImmutable();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueSelect value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_IMMUTABLE, "markers change is not allowed.");
    }

    @Override
    protected FieldValueSelect deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        var ret = new FieldValueSelect(twinField.getTwinClassField());
        ret.setItems(twin.getTwinMarkerKit().getList());
        return ret;
    }
}
