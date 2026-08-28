package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.twin.TwinService;

import java.util.UUID;


public class TwinFieldStoragePointedHead extends TwinFieldStorageSpirit {
    private final TwinService twinService;
    private final TwinFieldStorage headTwinFieldStorage;

    public TwinFieldStoragePointedHead(TwinService twinService, TwinFieldStorage headTwinFieldStorage) {
        this.twinService = twinService;
        this.headTwinFieldStorage = headTwinFieldStorage;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        twinService.loadHead(twinsKit.getList());
        var headTwins = new KitGrouped<>(TwinEntity::getId, TwinEntity::getTwinClassId);
        for (var twinEntity : twinsKit.getList()) {
            if (twinEntity.getHeadTwin() != null) {
                headTwins.add(twinEntity.getHeadTwin());
            }
        }
        twinService.loadClass(headTwins.getCollection());
        headTwinFieldStorage.load(headTwins);
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        if (twinEntity.getHeadTwinId() == null
                || (twinEntity.getHeadTwin() != null && headTwinFieldStorage.isLoaded(twinEntity.getHeadTwin()))) {
            return true;
        }
        return false;
    }

    @Override
    boolean canBeMerged(Object o) {
        return super.canBeMerged(o) &&
                (o instanceof TwinFieldStoragePointedHead twinFieldStoragePointedHead) && headTwinFieldStorage.getClass().equals(twinFieldStoragePointedHead.headTwinFieldStorage.getClass());
    }
}
