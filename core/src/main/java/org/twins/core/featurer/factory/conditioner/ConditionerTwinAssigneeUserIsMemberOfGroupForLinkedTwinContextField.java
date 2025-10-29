package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.KitUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsUserGroupId;
import org.twins.core.service.user.UserGroupService;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2439,
        name = "Is assignee user member of group for linked twin from context field",
        description = "Checks if linked twin assignee user is member of group linked twin from context fields")
@Slf4j
public class ConditionerTwinAssigneeUserIsMemberOfGroupForLinkedTwinContextField extends ConditionerForLinkedTwinBase {
    @FeaturerParam(name = "User group ids", description = "", order = 1)
    public static final FeaturerParamUUIDSet userGroupIds = new FeaturerParamUUIDSetTwinsUserGroupId("userGroupIds");

    @Lazy
    @Autowired
    private UserGroupService userGroupService;

    @Override
    protected Map<UUID, FieldValue> getFields(FactoryItem factoryItem) throws ServiceException {
        return factoryItem.getFactoryContext().getFields();
    }

    @Override
    protected boolean check(TwinEntity twinEntity, Properties properties) throws ServiceException {
        Set<UUID> propertiesUuids = userGroupIds.extract(properties);
        UserEntity assignerUser = twinEntity.getAssignerUser();
        userGroupService.loadGroups(assignerUser);
        return KitUtils.isNotEmpty(assignerUser.getUserGroups()) && assignerUser.getUserGroups().getIdSet().stream().anyMatch(propertiesUuids::contains);
    }
}
