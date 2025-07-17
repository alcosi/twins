package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;

import java.util.*;

public abstract class TwinFieldStorageCalc extends TwinFieldStorage {
    protected final UUID twinClassFieldId;

    protected TwinFieldStorageCalc(UUID twinClassFieldId) {
        this.twinClassFieldId = twinClassFieldId;
    }

    public void packResult(Kit<TwinEntity, UUID> twinsKit, List<TwinFieldCalcProjection> calc) {
        Kit<TwinFieldCalcProjection, UUID> calcKit = new Kit<>(calc, TwinFieldCalcProjection::twinId);
        for (var twinEntity : twinsKit) {
            if (twinEntity.getTwinFieldCalculated() == null)
                twinEntity.setTwinFieldCalculated(new HashMap<>());
            String value = calcKit.containsKey(twinEntity.getId()) ? calcKit.get(twinEntity.getId()).calc() : "0";
            twinEntity.getTwinFieldCalculated().put(twinClassFieldId, value);
        }
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return false;
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldCalculated() != null && twinEntity.getTwinFieldCalculated().containsKey(twinClassFieldId);
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        if (twinEntity.getTwinFieldCalculated() == null)
            twinEntity.setTwinFieldCalculated(new HashMap<>());
        twinEntity.getTwinFieldCalculated().put(twinClassFieldId, "0");
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        //nothing to replace
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return Collections.EMPTY_LIST;
    }
}
