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
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsPermissionId;
import org.twins.core.service.permission.PermissionService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2403,
        name = "Current user has permission",
        description = "")
@Slf4j
public class ConditionerApiUserHasPermission extends Conditioner {
    @FeaturerParam(name = "Permission id", description = "", order = 1)
    public static final FeaturerParamUUID permissionId = new FeaturerParamUUIDTwinsPermissionId("permissionId");

    @Lazy
    @Autowired
    PermissionService permissionService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return permissionService.currentUserHasPermission(permissionId.extract(properties));
    }
}
