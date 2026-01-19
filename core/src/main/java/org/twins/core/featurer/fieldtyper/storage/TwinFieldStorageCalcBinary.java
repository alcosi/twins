package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
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
        Set<UUID> fieldsToLoad = new HashSet<>();
        if (firstFieldId != null)
            fieldsToLoad.add(firstFieldId);
        if (secondFieldId != null)
            fieldsToLoad.add(secondFieldId);

        List<TwinFieldSimpleEntity> fields = twinFieldSimpleRepository
                .findByTwinIdInAndTwinClassFieldIdIn(twinsKit.getIdSet(), fieldsToLoad);

        // Map<TwinId, Map<FieldId, Double>>
        Map<UUID, Map<UUID, Double>> values = new HashMap<>();

        for (TwinFieldSimpleEntity field : fields) {
            try {
                if (field.getValue() != null) {
                    double val = Double.parseDouble(field.getValue());
                    values.computeIfAbsent(field.getTwinId(), k -> new HashMap<>()).put(field.getTwinClassFieldId(),
                            val);
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        List<TwinFieldCalcProjection> calcList = new ArrayList<>();
        for (TwinEntity twin : twinsKit.getCollection()) {
            Map<UUID, Double> twinValues = values.getOrDefault(twin.getId(), Collections.emptyMap());
            Double v1 = twinValues.get(firstFieldId);
            Double v2 = twinValues.get(secondFieldId);
            calcList.add(new TwinFieldCalcProjection(twin.getId(), calculate(v1, v2)));
        }
        packResult(twinsKit, calcList);
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
