package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
public abstract class ConditionerForLinkedTwinBase extends Conditioner {
    @FeaturerParam(name = "Twin class field id", description = "ID of the field to check", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        UUID extractedTwinClassFieldId = twinClassFieldId.extract(properties);
        FieldValue fieldValue = getFields(factoryItem).get(extractedTwinClassFieldId);
        var linkedTwin = FieldValueLink.getSingleLinkedTwinSafe(fieldValue);
        return check(linkedTwin, properties);
    }

    protected abstract Map<UUID, FieldValue> getFields(FactoryItem factoryItem) throws ServiceException;

    protected abstract boolean check(TwinEntity twinEntity, Properties properties) throws ServiceException;
}
