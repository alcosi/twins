package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorLinkHead;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTwin;
import org.twins.core.featurer.fieldtyper.value.FieldValueLinkSingle;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1323,
        name = "BaseHead",
        description = "Field typer for base head twin field")
public class FieldTyperBaseHeadField extends FieldTyperImmutable<FieldDescriptorLinkHead, FieldValueLinkSingle, TwinFieldStorageTwin, TwinFieldSearchNotImplemented> {

    @Override
    public FieldDescriptorLinkHead getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorLinkHead();
    }

    @Override
    protected FieldValueLinkSingle deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        return new FieldValueLinkSingle(twinField.getTwinClassField()).setValue(twin.getHeadTwin());
    }
}
