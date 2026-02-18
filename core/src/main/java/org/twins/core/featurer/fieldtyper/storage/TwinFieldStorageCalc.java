package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;

import java.math.BigDecimal;
import java.util.*;

public abstract class TwinFieldStorageCalc extends TwinFieldStorage {
    protected final UUID twinClassFieldId;

    protected TwinFieldStorageCalc(UUID twinClassFieldId) {
        this.twinClassFieldId = twinClassFieldId;
    }

    public void packResult(Kit<TwinEntity, UUID> twinsKit, List<TwinFieldCalcProjection> calc) {
        var calcKit = new Kit<>(calc, TwinFieldCalcProjection::twinId);
        for (var twinEntity : twinsKit) {
            if (twinEntity.getTwinFieldCalculated() == null)
                twinEntity.setTwinFieldCalculated(new HashMap<>());
            var value = calcKit.containsKey(twinEntity.getId()) ? calcKit.get(twinEntity.getId()).calc() : BigDecimal.ZERO;
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
        twinEntity.getTwinFieldCalculated().put(twinClassFieldId, BigDecimal.ZERO);
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        //nothing to replace
    }

    public void deleteTwinFieldsForTwins(Map<UUID, Set<UUID>> deleteMap) {
        // nothing to delete
    };

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return Collections.EMPTY_LIST;
    }
}
