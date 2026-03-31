package org.twins.core.featurer.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinId;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1501,
        name = "DuplicateTwin",
        description = "")
@RequiredArgsConstructor
public class TwinTriggerDuplicateTwin extends TwinTrigger {
    @Lazy
    final TwinService twinService;
    @Lazy
    final AuthService authService;

    @FeaturerParam(name = "Twin id", description = "", order = 1)
    public static final FeaturerParamUUID twinId = new FeaturerParamUUIDTwinsTwinId("twinId");

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        TwinEntity srcTwin = twinService.findEntity(twinId.extract(properties), EntitySmartService.FindMode.ifEmptyNull, EntitySmartService.ReadPermissionCheckMode.ifDeniedLog);
        if (srcTwin == null) {
            log.error("Can not access twin by id[{}]. Please check database config", twinId.extract(properties));
            return;
        }
        log.info("{} will be cloned", twinEntity.logShort());
        twinService.duplicateTwin(srcTwin, null);
    }
}
