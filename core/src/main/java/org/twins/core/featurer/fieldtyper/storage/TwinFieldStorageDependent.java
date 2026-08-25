package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.featurer.FeaturerService;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.util.*;

/**
 * Storage for an in-memory calculated field (e.g. {@link org.twins.core.featurer.fieldtyper.FieldTyperCalcSum})
 * whose value is a function of other twin fields' decimal values ("operands").
 * <p>
 * Responsibility of {@link #load} is strictly the <b>fundamental</b>, batch-friendly part: ensure that every
 * operand's raw value is available in memory — persisted operands land in their raw kits (e.g.
 * {@code twinFieldDecimalKit}), DB-calculated operands (SumByHead / SumByLink / ...) get their own storage's
 * SQL projection loaded into {@code twinFieldCalculated}. This is done by delegating to each operand's own
 * storage via {@code operandFieldTyper.getStorage(operandTcf).load(subKit)}.
 * <p>
 * It does <b>not</b> compute or store this field's own value — that is the {@link FieldTyper}'s job (via its
 * {@code calculate} / {@code computeCalculated}). In-memory calculated operands (e.g. a nested Sum) are likewise
 * not the storage's concern: their value is doracalculated lazily in the FieldTyper.
 * <p>
 * Because the storage never writes its own value, {@code twinFieldCalculated.containsKey(fieldId)} cannot serve
 * as the "loaded" signal. Instead {@link #isLoaded} reads a separate marker set on {@link TwinEntity}
 * ({@code twinFieldCalcStorageLoaded}), which is invalidated together with {@code twinFieldCalculated}.
 * Per-field instance (not a Spring bean), built in the FieldTyper's {@code getStorage(tcf, props)}.
 */
public class TwinFieldStorageDependent extends TwinFieldStorage {
    private final UUID twinClassFieldId;
    private final Set<UUID> operandFieldIds;
    private final FeaturerService featurerService;
    private final TwinClassFieldService twinClassFieldService;

    public TwinFieldStorageDependent(UUID twinClassFieldId,
                                     Set<UUID> operandFieldIds,
                                     FeaturerService featurerService,
                                     TwinClassFieldService twinClassFieldService) {
        this.twinClassFieldId = twinClassFieldId;
        this.operandFieldIds = operandFieldIds;
        this.featurerService = featurerService;
        this.twinClassFieldService = twinClassFieldService;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        // Mark first so that a cyclic operand dependency (A depends on B, B depends on A) short-circuits
        // here on re-entry instead of looping forever. The actual operand loads below are idempotent via
        // each operand storage's own isLoaded gate.
        markLoaded(twinsKit);
        if (KitUtils.isEmpty(twinsKit) || CollectionUtils.isEmpty(operandFieldIds)) {
            return;
        }
        // Resolve operand TwinClassFieldEntities in one bulk call (orphan id -> throws UUID_UNKNOWN by design).
        Kit<TwinClassFieldEntity, UUID> operands = twinClassFieldService.findEntitiesSafe(operandFieldIds);
        for (TwinClassFieldEntity operandTcf : operands.getCollection()) {
            FieldTyper<?, ?, ?, ?> operandTyper = featurerService.getFeaturer(operandTcf.getFieldTyperFeaturerId(), FieldTyper.class);
            TwinFieldStorage operandStorage = operandTyper.getStorage(operandTcf);
            // Bulk-load only the twins for which this operand is not yet loaded.
            List<TwinEntity> needLoad = new ArrayList<>();
            for (TwinEntity twin : twinsKit.getCollection()) {
                if (!operandStorage.isLoaded(twin)) {
                    needLoad.add(twin);
                }
            }
            if (!needLoad.isEmpty()) {
                operandStorage.load(new Kit<>(needLoad, TwinEntity::getId));
            }
        }
    }

    private void markLoaded(Kit<TwinEntity, UUID> twinsKit) {
        if (twinsKit == null) {
            return;
        }
        for (TwinEntity twin : twinsKit.getCollection()) {
            Set<UUID> loaded = twin.getTwinFieldCalcStorageLoaded();
            if (loaded == null) {
                loaded = new HashSet<>();
                twin.setTwinFieldCalcStorageLoaded(loaded);
            }
            loaded.add(twinClassFieldId);
        }
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        Set<UUID> loaded = twinEntity.getTwinFieldCalcStorageLoaded();
        return loaded != null && loaded.contains(twinClassFieldId);
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        Set<UUID> loaded = twinEntity.getTwinFieldCalcStorageLoaded();
        if (loaded == null) {
            loaded = new HashSet<>();
            twinEntity.setTwinFieldCalcStorageLoaded(loaded);
        }
        loaded.add(twinClassFieldId);
    }

    @Override
    public boolean hasStrictValues(UUID twinClassFieldId) {
        return false;
    }

    @Override
    public Collection<UUID> findUsedFields(UUID twinClassId, Set<UUID> twinClassFieldIdSet) {
        return Collections.emptyList();
    }

    @Override
    public void replaceTwinClassFieldForTwinsOfClass(UUID twinClassId, UUID fromTwinClassFieldId, UUID toTwinClassFieldId) {
        // calculated value is not persisted — nothing to rewire
    }

    @Override
    public void deleteTwinFieldsForTwins(Map<UUID, Set<UUID>> deleteMap) {
        // calculated value is not persisted — nothing to delete
    }

    @Override
    boolean canBeMerged(Object o) {
        if (!isSameClass(o)) {
            return false;
        }
        TwinFieldStorageDependent that = (TwinFieldStorageDependent) o;
        return Objects.equals(this.twinClassFieldId, that.twinClassFieldId)
                && Objects.equals(this.operandFieldIds, that.operandFieldIds);
    }
}
