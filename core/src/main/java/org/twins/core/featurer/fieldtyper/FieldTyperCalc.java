package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDependent;

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
 *   <li>{@link #resolveDependentDecimalValue} — reads a dependent field's decimal value (persisted /
 *       DB-calculated operands are bulk-loaded by the field's own storage; in-memory calculated operands
 *       are doracalculated lazily here).</li>
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
     * Resolves the decimal value of a dependent (operand) field.
     * <p>
     * Persisted operands (raw kit) and DB-calculated operands (SumByHead / SumByLink / ... in
     * {@code twinFieldCalculated}) are already bulk-loaded by this field's storage
     * (see {@link TwinFieldStorageDependent}), so the
     * fast path is a plain {@code getDecimalValue} read. The only remaining case is an in-memory calculated
     * operand (e.g. a nested Sum) whose value has not been computed yet — it is doracalculated lazily here
     * via its own typer's {@code deserializeValue} (which cascades through {@link #computeCalculated},
     * bounded by the cycle guard).
     */
    default BigDecimal resolveDependentDecimalValue(TwinEntity twin, TwinClassFieldEntity twinClassField) throws ServiceException {
        FieldTyper<?, ?, ?, ?> self = fieldTyper();
        // persisted / DB-calculated operand — already loaded by the storage; read straight away.
        BigDecimal value = self.getTwinClassFieldService().getDecimalValue(twin, twinClassField.getId(), null);
        if (value != null) {
            return value;
        }
        // in-memory calculated operand not computed yet — doracalculate it lazily (cascades via computeCalculated).
        FieldTyper<?, ?, ?, ?> fieldTyper = self.featurerService.getFeaturer(twinClassField.getFieldTyperFeaturerId(), FieldTyper.class);
        fieldTyper.deserializeValue(new TwinField(twin, twinClassField));
        value = self.getTwinClassFieldService().getDecimalValue(twin, twinClassField.getId(), null);
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
