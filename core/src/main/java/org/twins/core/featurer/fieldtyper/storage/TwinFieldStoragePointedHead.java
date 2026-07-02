package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.ArrayList;
import java.util.UUID;


public class TwinFieldStoragePointedHead extends TwinFieldStorageSpirit {
    private final TwinService twinService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinFieldStorage headTwinFieldStorage;

    public TwinFieldStoragePointedHead(TwinService twinService, TwinClassFieldService twinClassFieldService, TwinFieldStorage headTwinFieldStorage) {
        this.twinService = twinService;
        this.headTwinFieldStorage = headTwinFieldStorage;
        this.twinClassFieldService = twinClassFieldService;
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
        var classes = new ArrayList<TwinClassEntity>(headTwins.size());
        for (var entry : headTwins.getGroupedMap().entrySet()) {
            classes.add(entry.getValue().getFirst().getTwinClass());
        }
        // we have to do it here, because headTwinFieldStorage needs this data
        twinClassFieldService.loadTwinClassFields(classes);
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
