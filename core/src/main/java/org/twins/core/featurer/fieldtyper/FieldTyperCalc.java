package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;

import java.math.BigDecimal;
import java.util.*;

/**
 * Mixin for field typers whose value is calculated in memory from other fields of the same twin
 * (e.g. sum / product / difference of fields). Provides reusable building blocks so a new calculated
 * field typer only has to implement the actual formula:
 * <ul>
 *   <li>caching of the computed value in {@link TwinEntity#getTwinFieldCalculated()} — the same field is
 *       never recomputed twice and can be consumed by another calculated field;</li>
 *   <li>cycle protection against circular dependencies (A = f(B), B = g(A), or A = f(A));</li>
 *   <li>{@link #resolveDependentDecimalValue} — loads and reads the decimal value of a dependent field
 *       regardless of its storage kind (plain decimal, per-field calc storage, or a nested calculated field).</li>
 * </ul>
 * <p>
 * Intended to be implemented by {@link FieldTyper} subclasses — the default methods rely on that
 * (see {@link #fieldTyper()}).
 */
public interface FieldTyperCalc {

    Logger log = LoggerFactory.getLogger(FieldTyperCalc.class);

    /**
     * Calculated fields currently being computed on this thread — backs the cycle guard. Shared across
     * all calculated field typer instances (they are singleton beans), so nested/looped dependencies
     * between different calculated typers are detected too.
     */
    ThreadLocal<Set<UUID>> COMPUTATION_STACK = ThreadLocal.withInitial(HashSet::new);

    /**
     * Implementors are expected to be {@link FieldTyper}; this cast exposes the services needed by
     * the default methods below.
     */
    default FieldTyper<?, ?, ?, ?> fieldTyper() {
        return (FieldTyper<?, ?, ?, ?>) this;
    }

    /**
     * Hook: the actual formula. Each calculated field typer implements this (e.g. sum / product /
     * difference of dependent fields). Called by {@link #computeCalculated} only on a cache miss and
     * outside of a cycle, so implementors do not have to worry about caching or recursion safety —
     * use {@link #resolveDependentDecimalValue} to read each dependent field's value.
     */
    BigDecimal calculate(TwinEntity twin, Properties properties) throws ServiceException;

    /**
     * Template method: returns the cached value if present, otherwise runs {@link #calculate} and stores
     * the result. Cycle-safe — a field already on the computation stack is short-circuited with {@code 0}
     * (and intentionally left out of the cache) instead of looping forever.
     */
    default BigDecimal computeCalculated(TwinEntity twin, UUID fieldId, Properties properties) throws ServiceException {
        BigDecimal cached = readCalculated(twin, fieldId);
        if (cached != null) {
            return cached;
        }
        Set<UUID> stack = COMPUTATION_STACK.get();
        if (!stack.add(fieldId)) {
            log.warn("Cyclic calculated field dependency detected, breaking the loop with 0 for field {}", fieldId);
            return BigDecimal.ZERO;
        }
        try {
            BigDecimal value = calculate(twin, properties);
            cacheCalculated(twin, fieldId, value);
            return value;
        } finally {
            stack.remove(fieldId);
            if (stack.isEmpty()) {
                COMPUTATION_STACK.remove();
            }
        }
    }

    /**
     * Resolves the decimal value of a dependent field, making sure its value is actually loaded
     * (this is the bug being fixed — at some call points the dependent fields were not loaded yet):
     * <ul>
     *   <li>plain decimal fields live in {@code twinFieldDecimalKit},</li>
     *   <li>calc fields with per-field storage (SumByHead / SumByLink / ...) live in
     *       {@code twinFieldCalculated}, populated by their own {@code storage.load()},</li>
     *   <li>nested plain calculated fields have no stored value, so they are computed on demand via
     *       their own typer, which caches the result back into {@code twinFieldCalculated}.</li>
     * </ul>
     * No recursion through field ids happens here — each calc storage loads its value with a single SQL
     * projection, so the only recursion path is the nested-calculated-field fallback, bounded by the cycle guard.
     */
    default BigDecimal resolveDependentDecimalValue(TwinEntity twin, UUID fieldId, TwinClassFieldEntity twinClassField) throws ServiceException {
        FieldTyper<?, ?, ?, ?> self = fieldTyper();
        // already present in the decimal kit or in the calc cache?
        BigDecimal value = self.getTwinClassFieldService().getDecimalValue(twin, fieldId, null);
        if (value != null) {
            return value;
        }
        if (twinClassField == null) {
            return BigDecimal.ZERO;
        }
        // ensure the dependent field's own storage is loaded (this is what was missing for calc fields)
        FieldTyper<?, ?, ?, ?> fieldTyper = self.getFeaturerService().getFeaturer(twinClassField.getFieldTyperFeaturerId(), FieldTyper.class);
        TwinFieldStorage storage = fieldTyper.getStorage(twinClassField);
        if (!storage.isLoaded(twin)) {
            storage.load(Kit.singleton(twin, TwinEntity::getId));
        }
        value = self.getTwinClassFieldService().getDecimalValue(twin, fieldId, null);
        if (value != null) {
            return value;
        }
        // field has no stored value (e.g. it is a nested calculated field) — compute it, which caches the result
        fieldTyper.deserializeValue(new TwinField(twin, twinClassField));
        value = self.getTwinClassFieldService().getDecimalValue(twin, fieldId, null);
        return value != null ? value : BigDecimal.ZERO;
    }

    default BigDecimal readCalculated(TwinEntity twin, UUID fieldId) {
        Map<UUID, BigDecimal> calc = twin.getTwinFieldCalculated();
        return calc != null ? calc.get(fieldId) : null;
    }

    default void cacheCalculated(TwinEntity twin, UUID fieldId, BigDecimal value) {
        Map<UUID, BigDecimal> calc = twin.getTwinFieldCalculated();
        if (calc == null) {
            calc = new HashMap<>();
            twin.setTwinFieldCalculated(calc);
        }
        calc.put(fieldId, value);
    }
}
