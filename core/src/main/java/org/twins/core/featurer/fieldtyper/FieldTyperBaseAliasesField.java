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
import org.twins.core.featurer.fieldtyper.value.FieldValueAliases;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1329,
        name = "BaseAliases",
        description = "Field typer for base aliases twin field")
public class FieldTyperBaseAliasesField extends FieldTyperImmutable<FieldDescriptorImmutable, FieldValueAliases, TwinFieldStorageTwin, TwinFieldSearchNotImplemented> {

    @Override
    public FieldDescriptorImmutable getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorImmutable();
    }

    @Override
    protected FieldValueAliases deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        FieldValueAliases ret = new FieldValueAliases(twinField.getTwinClassField());
        ret.setItems(twin.getTwinAliases().getList());
        return ret;
    }
}
