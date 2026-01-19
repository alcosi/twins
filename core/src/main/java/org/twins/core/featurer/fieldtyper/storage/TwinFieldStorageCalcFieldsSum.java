package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.*;

public class TwinFieldStorageCalcFieldsSum extends TwinFieldStorageCalc {
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;
    private final Set<UUID> fieldIds;

    public TwinFieldStorageCalcFieldsSum(TwinFieldSimpleRepository twinFieldSimpleRepository, UUID twinClassFieldId, Set<UUID> fieldIds) {
        super(twinClassFieldId);
        this.twinFieldSimpleRepository = twinFieldSimpleRepository;
        this.fieldIds = fieldIds;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        List<TwinFieldSimpleEntity> fields = twinFieldSimpleRepository.findByTwinIdInAndTwinClassFieldIdIn(twinsKit.getIdSet(), fieldIds);
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
        TwinFieldStorageCalcFieldsSum that = (TwinFieldStorageCalcFieldsSum) o;
        return Objects.equals(twinClassFieldId, that.twinClassFieldId) && Objects.equals(fieldIds, that.fieldIds);
    }
}
