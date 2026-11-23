package org.twins.core.featurer.fieldtyper.value;

import lombok.Getter;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

/**
 * Single-id transition state of a group key value: the key is known only as an entity id, the entity
 * itself is NOT loaded. TwinGroupLevel.extractKeys produces it for base-field keys (base fields live
 * in twin columns, not in twin.fieldValuesKit), TwinGroupedSearchService bulk-materializes it into
 * the concrete single value (FieldValueUserSingle and siblings) — one findEntitiesSafe per entity
 * type per request, so no N+1. The target value type is resolved once at level resolution and
 * carried here, so materialization needs no featurer lookup.
 * <p>
 * Unlike the multi-id {@link FieldValueReference} (the string-parser transport, which carries a
 * whole link-field id list), this one holds exactly one id — hence FieldValueSingle: the scalar key
 * is the id itself, with no "first element of a list" shortcut.
 * <p>
 * Like FieldValueReference, it deliberately exposes NO entity accessors: a half-loaded key must not
 * be readable — a consumer expecting a concrete type fails fast on instanceof instead of silently
 * reading null fields of an id-only stub entity.
 */
public class FieldValueReferenceSingle extends FieldValueStated implements FieldValueSingle {

    @Getter
    private final Class<? extends FieldValue> valueType;

    @Getter
    private UUID id;

    public FieldValueReferenceSingle(TwinClassFieldEntity twinClassField, Class<? extends FieldValue> valueType, UUID id) {
        super(twinClassField);
        this.valueType = valueType;
        this.id = id;
    }

    @Override
    public FieldValueReferenceSingle newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueReferenceSingle(newTwinClassFieldEntity, valueType, null);
    }

    @Override
    public Object getScalarValue() {
        return id;
    }

    @Override
    public boolean hasValue(String value) {
        return id != null && id.toString().equals(value);
    }

    @Override
    public void copyValueTo(FieldValueStated dst) {
        ((FieldValueReferenceSingle) dst).id = id;
    }

    @Override
    public void onUndefine() {
        id = null;
    }

    @Override
    public void onClear() {
        id = null;
    }
}
