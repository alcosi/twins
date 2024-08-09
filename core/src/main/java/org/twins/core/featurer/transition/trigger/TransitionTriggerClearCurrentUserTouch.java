package org.twins.core.featurer.transition.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamStringTouchId;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinTouchService;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1502,
        name = "TransitionTriggerClearCurrentUserTouch",
        description = "")
@RequiredArgsConstructor
public class TransitionTriggerClearCurrentUserTouch extends TransitionTrigger {
    @Lazy
    final TwinTouchService twinTouchService;

    @Lazy
    final AuthService authService;

    @FeaturerParam(name = "touchId", description = "")
    public static final FeaturerParamStringTouchId touchId = new FeaturerParamStringTouchId("touchId");

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        twinTouchService.deleteCurrentUserTouch(twinEntity.getId(), touchId.extract(properties));
    }
}
