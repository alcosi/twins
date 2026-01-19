package org.twins.core.featurer.fieldtyper.storage;

import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.Objects;
import java.util.UUID;

public class TwinFieldStorageCalcDivision extends TwinFieldStorageCalcBinary {
    private final String divisionByZeroResult;

    public TwinFieldStorageCalcDivision(TwinFieldSimpleRepository twinFieldSimpleRepository, UUID twinClassFieldId, UUID firstFieldId, UUID secondFieldId, String divisionByZeroResult) {
        super(twinFieldSimpleRepository, twinClassFieldId, firstFieldId, secondFieldId);
        this.divisionByZeroResult = divisionByZeroResult;
    }

    @Override
    protected String calculate(Double v1, Double v2) {
        double d1 = v1 == null ? 0.0 : v1;
        double d2 = v2 == null ? 0.0 : v2;
        if (d2 == 0.0)
            return divisionByZeroResult;
        return String.valueOf(d1 / d2);
    }

    @Override
    boolean canBeMerged(Object o) {
        if (!super.canBeMerged(o))
            return false;
        return Objects.equals(this.divisionByZeroResult, ((TwinFieldStorageCalcDivision) o).divisionByZeroResult);
    }
}
