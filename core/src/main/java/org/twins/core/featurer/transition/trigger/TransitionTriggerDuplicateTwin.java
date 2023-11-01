package org.twins.core.featurer.transition.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = 1501,
        name = "TransitionTriggerDuplicateTwin",
        description = "")
@RequiredArgsConstructor
public class TransitionTriggerDuplicateTwin extends TransitionTrigger {
    @Lazy
    final TwinService twinService;
    @Lazy
    final AuthService authService;

    @FeaturerParam(name = "twinId", description = "")
    public static final FeaturerParamUUID twinId = new FeaturerParamUUID("twinId");

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        TwinEntity srcTwin = twinService.findEntity(twinId.extract(properties), EntitySmartService.FindMode.ifEmptyNull, EntitySmartService.ReadPermissionCheckMode.ifDeniedLog);
        if (srcTwin == null) {
            log.error("Can not access twin by id[" + twinId.extract(properties) + "]. Please check database config");
            return;
        }
        ApiUser apiUser = authService.getApiUser();
        log.info(twinEntity.easyLog(EasyLoggable.Level.NORMAL) + " will be cloned");
        twinService.duplicateTwin(srcTwin, apiUser.getBusinessAccount(), apiUser.getUser());
    }
}
