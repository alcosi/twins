package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.*;

public abstract class TwinFieldStorageCalcBinary extends TwinFieldStorageCalc {
    protected final TwinFieldSimpleRepository twinFieldSimpleRepository;
    protected final UUID firstFieldId;
    protected final UUID secondFieldId;

    public TwinFieldStorageCalcBinary(TwinFieldSimpleRepository twinFieldSimpleRepository, UUID twinClassFieldId, UUID firstFieldId, UUID secondFieldId) {
        super(twinClassFieldId);
        this.twinFieldSimpleRepository = twinFieldSimpleRepository;
        this.firstFieldId = firstFieldId;
        this.secondFieldId = secondFieldId;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
    }

    protected abstract String calculate(Double v1, Double v2);

    @Override
    boolean canBeMerged(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TwinFieldStorageCalcBinary that = (TwinFieldStorageCalcBinary) o;
        return Objects.equals(twinClassFieldId, that.twinClassFieldId) &&
                Objects.equals(firstFieldId, that.firstFieldId) &&
                Objects.equals(secondFieldId, that.secondFieldId);
    }
}
