package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.auth.AuthService;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2436,
        name = "Is current user assignee for linked twin from output field",
        description = "Checks if current user is assignee for linked twin from output fields")
@Slf4j
public class ConditionerApiUserIsAssigneeForLinkedTwinOutputField extends ConditionerForLinkedTwinBase {
    @Lazy
    @Autowired
    private AuthService authService;

    @Override
    protected Map<UUID, FieldValue> getFields(FactoryItem factoryItem) throws ServiceException {
        return factoryItem.getOutput().getFields();
    }

    @Override
    protected boolean check(TwinEntity twinEntity, Properties properties) throws ServiceException {
        return twinEntity.getAssignerUserId().equals(authService.getApiUser().getUserId());
    }
}
