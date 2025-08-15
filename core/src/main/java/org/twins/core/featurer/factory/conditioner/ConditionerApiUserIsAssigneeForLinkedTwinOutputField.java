package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2436,
        name = "Is current user assignee for linked twin from output field",
        description = "Checks if current user is assignee for linked twin from output fields")
@Slf4j
public class ConditionerApiUserIsAssigneeForLinkedTwinOutputField extends ConditionerApiUserIsAssigneeForLinkedTwinBase {
    @FeaturerParam(name = "Twin class field id", description = "ID of the field to check", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        UUID extractedTwinClassFieldId = twinClassFieldId.extract(properties);
        return check(factoryItem, factoryItem.getOutput().getFields().get(extractedTwinClassFieldId), extractedTwinClassFieldId);
    }
}
