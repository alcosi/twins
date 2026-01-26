package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwinFieldStoragePointedHead extends TwinFieldStorageSpirit {
    @Lazy
    private final TwinService twinService;

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        twinService.loadHeadForTwin(twinsKit.getList());
        var headTwins = new Kit<>(TwinEntity::getId);
        for (var twinEntity : twinsKit.getList()) {
            if (twinEntity.getHeadTwin() != null) {
                headTwins.add(twinEntity.getHeadTwin());
            }
        }
        twinService.loadFieldsValues(headTwins);
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        if (twinEntity.getHeadTwinId() == null
                || (twinEntity.getHeadTwin() != null && twinEntity.getHeadTwin().getFieldValuesKit() != null)) {
            return true;
        }
        return false;
    }
}
