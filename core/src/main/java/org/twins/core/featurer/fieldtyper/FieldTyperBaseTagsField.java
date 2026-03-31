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
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1330,
        name = "BaseTags",
        description = "Field typer for base tags twin field")
public class FieldTyperBaseTagsField extends FieldTyperImmutable<FieldDescriptorImmutable, FieldValueSelect, TwinFieldStorageTwin, TwinFieldSearchNotImplemented> {

    @Override
    public FieldDescriptorImmutable getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorImmutable();
    }

    @Override
    protected FieldValueSelect deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        var ret = new FieldValueSelect(twinField.getTwinClassField());
        ret.setItems(twin.getTwinTagKit().getList());
        return ret;
    }
}
