package org.twins.core.featurer.pointer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.*;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3101,
        name = "Self pointed",
        description = "")
public class PointerOnSelf extends Pointer {
    @Override
    protected Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins) throws ServiceException {
        Map<UUID, TwinEntity> result = new HashMap<>(srcTwins.size());
        for (TwinEntity src : srcTwins) {
            result.put(src.getId(), src);
        }
        return result;
    }
}
