package org.twins.core.featurer.factory.lookuper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Result of a batch field lookup: the per-item values plus the per-item failures. The batch loop
 * isolates failures per item (a failing item does not abort the batch), so the caller can reproduce
 * the old per-item semantics — typically by re-throwing the failure inside its own per-item
 * isolation loop (see {@code Filler.fillEachIsolated}).
 */
public record LookupResult(Map<FactoryItem, FieldValue> values, Map<FactoryItem, ServiceException> failures) {

    public static LookupResult empty(int expectedSize) {
        return new LookupResult(new HashMap<>(expectedSize), new HashMap<>());
    }

    public boolean hasFailures() {
        return !failures.isEmpty();
    }

    public FieldValue value(FactoryItem factoryItem) {
        return values.get(factoryItem);
    }

    /** Re-throws the recorded failure of this item, if any — keeps the original error and its per-item attribution. */
    public void rethrowFailureIfPresent(FactoryItem factoryItem) throws ServiceException {
        ServiceException failure = failures.get(factoryItem);
        if (failure != null)
            throw failure;
    }
}
