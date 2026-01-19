package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;

import java.util.*;

public class TwinFieldStorageCalcFieldsSum extends TwinFieldStorageCalc {
    private final Set<UUID> fieldIds;

    public TwinFieldStorageCalcFieldsSum(UUID twinClassFieldId, Set<UUID> fieldIds) {
        super(twinClassFieldId);
        this.fieldIds = fieldIds;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        List<TwinFieldSimpleEntity> fields = new ArrayList<>();
        for (TwinEntity twin : twinsKit) {
            Kit<TwinFieldSimpleEntity, UUID> twinFieldSimpleKit = twin.getTwinFieldSimpleKit();
            for (UUID fieldId : fieldIds) {
                TwinFieldSimpleEntity twinFieldSimple = twinFieldSimpleKit.get(fieldId);
                if (twinFieldSimple != null) {
                    fields.add(twinFieldSimple);
                }
            }
        }

        Map<UUID, Double> resultMap = new HashMap<>();
        for (TwinFieldSimpleEntity field : fields) {
            try {
                if (field.getValue() != null) {
                    double val = Double.parseDouble(field.getValue());
                    resultMap.merge(field.getTwinId(), val, Double::sum);
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        List<TwinFieldCalcProjection> calcList = new ArrayList<>();
        for (UUID twinId : twinsKit.getIdSet()) {
            Double sum = resultMap.getOrDefault(twinId, 0.0);
            calcList.add(new TwinFieldCalcProjection(twinId, String.valueOf(sum)));
        }
        packResult(twinsKit, calcList);
    }

    @Override
    boolean canBeMerged(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TwinFieldStorageCalc that = (TwinFieldStorageCalc) o;
        return Objects.equals(twinClassFieldId, that.twinClassFieldId);
    }
}
