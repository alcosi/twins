package org.twins.core.featurer.pointer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3102,
        name = "Head twin pointed",
        description = "")
@RequiredArgsConstructor
public class PointerOnHead extends Pointer {
    @Lazy
    private final TwinService twinService;

    @Override
    protected TwinEntity point(Properties properties, TwinEntity srcTwinEntity) throws ServiceException {
        twinService.loadHeadForTwin(srcTwinEntity);
        return srcTwinEntity.getHeadTwin();
    }
}
