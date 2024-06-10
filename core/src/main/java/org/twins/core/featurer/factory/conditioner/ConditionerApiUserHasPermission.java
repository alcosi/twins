package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsPermissionId;
import org.twins.core.service.permission.PermissionService;

import java.util.Properties;

@Component
@Featurer(id = 2403,
        name = "ConditionerApiUserHasPermission",
        description = "")
@Slf4j
public class ConditionerApiUserHasPermission extends Conditioner {
    @FeaturerParam(name = "permissionId", description = "")
    public static final FeaturerParamUUID permissionId = new FeaturerParamUUIDTwinsPermissionId("permissionId");

    @Lazy
    @Autowired
    PermissionService permissionService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return permissionService.currentUserHasPermission(permissionId.extract(properties));
    }
}
