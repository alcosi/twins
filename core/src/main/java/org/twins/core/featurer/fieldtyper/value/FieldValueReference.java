package org.twins.core.featurer.fieldtyper.value;

import lombok.Getter;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Transition state of a field value: the value is known only as a list of entity ids, the entities themselves
 * are NOT loaded. Produced by the string parser (TwinService.parseFieldValue) and consumed by
 * TwinService.materializeFieldValues, which bulk-resolves it into the concrete FieldValue* carrying loaded
 * entities — one query per entity type per batch, so no N+1. The target value type is resolved once at parse
 * time and carried here, so materialization needs no featurer re-lookup.
 * <p>
 * The class deliberately exposes NO entity accessors (no getItems()/getValue()): a half-loaded value must not
 * be readable. A consumer receiving a FieldValueReference where a concrete type is expected fails fast on
 * instanceof instead of silently reading null fields of an id-only stub entity.
 * <p>
 * State follows the ids, mirroring FieldValueCollection: null — UNDEFINED, empty — CLEARED, non-empty — PRESENT.
 * <p>
 * Extension point: select fields (FieldValueSelect) are NOT covered — their resolution needs featurer params
 * (dataListId, supportCustomValue) and stays on the legacy in-place parse path. When they migrate, extend this
 * class with the option criteria (e.g. FieldValueReferenceSelect) instead of widening this class.
 */
public class FieldValueReference extends FieldValueStated {

    @Getter
    private final Class<? extends FieldValue> valueType;

    @Getter
    private List<UUID> ids;

    public FieldValueReference(TwinClassFieldEntity twinClassField, Class<? extends FieldValue> valueType, List<UUID> ids) {
        super(twinClassField);
        this.valueType = valueType;
        this.ids = ids;
    }

    @Override
    public FieldValueReference newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueReference(newTwinClassFieldEntity, valueType, null);
    }

    @Override
    public boolean hasValue(String value) {
        if (ids == null)
            return false;
        UUID valueUUID;
        try {
            valueUUID = UUID.fromString(value);
        } catch (Exception e) {
            return false;
        }
        return ids.contains(valueUUID);
    }

    @Override
    public void copyValueTo(FieldValueStated dst) {
        var dstValue = (FieldValueReference) dst;
        dstValue.ids = ids == null ? null : new ArrayList<>(ids);
    }

    @Override
    public void onUndefine() {
        ids = null;
    }

    @Override
    public void onClear() {
        ids = new ArrayList<>(); // empty, not null — mirrors FieldValueCollection: cleared, not undefined
    }

    @Override
    protected void updateMutableValueState() {
        state = ids == null ? State.UNDEFINED : ids.isEmpty() ? State.CLEARED : State.PRESENT;
    }
}
