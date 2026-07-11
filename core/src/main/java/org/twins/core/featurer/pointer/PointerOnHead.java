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

import java.util.*;

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
    protected Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins) throws ServiceException {
        twinService.loadHead(srcTwins);
        Map<UUID, TwinEntity> result = new HashMap<>(srcTwins.size());
        for (TwinEntity src : srcTwins) {
            TwinEntity head = src.getHeadTwin();
            if (head != null) {
                result.put(src.getId(), head);
            }
        }
        return result;
    }
}
