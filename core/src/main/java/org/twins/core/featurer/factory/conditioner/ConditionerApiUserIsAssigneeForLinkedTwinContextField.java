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
@Featurer(id = FeaturerTwins.ID_2435,
        name = "Is current user assignee for linked twin from context field",
        description = "Checks if current user is assignee for linked twin from context fields")
@Slf4j
public class ConditionerApiUserIsAssigneeForLinkedTwinContextField extends ConditionerForLinkedTwinBase {
    @Lazy
    @Autowired
    private AuthService authService;

    @Override
    protected Map<UUID, FieldValue> getFields(FactoryItem factoryItem) throws ServiceException {
        return factoryItem.getFactoryContext().getFields();
    }

    @Override
    protected boolean check(TwinEntity twinEntity, Properties properties) throws ServiceException {
        return twinEntity.getAssignerUserId().equals(authService.getApiUser().getUserId());
    }
}
