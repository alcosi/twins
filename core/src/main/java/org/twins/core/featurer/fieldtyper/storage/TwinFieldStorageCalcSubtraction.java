package org.twins.core.featurer.fieldtyper.storage;

import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.UUID;

public class TwinFieldStorageCalcSubtraction extends TwinFieldStorageCalcBinary {
    public TwinFieldStorageCalcSubtraction(TwinFieldSimpleRepository twinFieldSimpleRepository, UUID twinClassFieldId, UUID firstFieldId, UUID secondFieldId) {
        super(twinFieldSimpleRepository, twinClassFieldId, firstFieldId, secondFieldId);
    }

    @Override
    protected String calculate(Double v1, Double v2) {
        double d1 = v1 == null ? 0.0 : v1;
        double d2 = v2 == null ? 0.0 : v2;
        return String.valueOf(d1 - d2);
    }
}
