package org.twins.core.featurer.fieldtyper.storage;

import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.Objects;
import java.util.UUID;

public class TwinFieldStorageCalcMultiplication extends TwinFieldStorageCalcBinary {
    private final boolean replaceZeroWithOne;

    public TwinFieldStorageCalcMultiplication(TwinFieldSimpleRepository twinFieldSimpleRepository, UUID twinClassFieldId, UUID firstFieldId, UUID secondFieldId, boolean replaceZeroWithOne) {
        super(twinFieldSimpleRepository, twinClassFieldId, firstFieldId, secondFieldId);
        this.replaceZeroWithOne = replaceZeroWithOne;
    }

    @Override
    protected String calculate(Double v1, Double v2) {
        double d1 = prepare(v1);
        double d2 = prepare(v2);
        return String.valueOf(d1 * d2);
    }

    private double prepare(Double v) {
        if (replaceZeroWithOne) {
            return (v == null || v == 0) ? 1.0 : v;
        }
        return v == null ? 0.0 : v;
    }

    @Override
    boolean canBeMerged(Object o) {
        if (!super.canBeMerged(o))
            return false;
        return this.replaceZeroWithOne == ((TwinFieldStorageCalcMultiplication) o).replaceZeroWithOne;
    }
}
