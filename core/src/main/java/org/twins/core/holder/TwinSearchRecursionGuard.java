package org.twins.core.holder;

import org.cambium.common.exception.ServiceException;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Thread-local chain of the saved searches currently being resolved on this thread. Every entry
 * point that runs one saved search inside another pushes the search id here —
 * {@link org.twins.core.featurer.twin.executor.TwinSearchExecutor} public calls (concat children)
 * and subquery finders ({@code TwinFinderSubquery}) alike — so a saved search that reaches itself,
 * directly or through other searches, fails fast with a clear config error instead of recursing
 * to a stack overflow. One shared chain also catches mixed cycles (a concat child whose subquery
 * leads back to the concatenation).
 * <p>
 * The chain preserves push order, so the error names the whole cycle ({@code A -> B -> A}).
 * <p>
 * Cleanup contract: every {@link #push} must be paired with {@link #pop} in {@code finally} at
 * the same entry point. Thread-local on purpose — executors and finders are singletons shared by
 * concurrent requests, and resolution runs synchronously on one thread (virtual threads included:
 * not pooled, so nothing leaks between tasks — the same pattern as {@link EntityRequestCacheHolder}).
 */
public class TwinSearchRecursionGuard {

    private static final ThreadLocal<Set<UUID>> STACK = ThreadLocal.withInitial(LinkedHashSet::new);

    public static void push(UUID searchId) throws ServiceException {
        if (searchId == null)
            return;
        Set<UUID> stack = STACK.get();
        if (!stack.add(searchId)) {
            StringBuilder chain = new StringBuilder();
            for (UUID id : stack) {
                if (chain.length() > 0)
                    chain.append(" -> ");
                chain.append(id);
            }
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                    "circular saved search: " + chain + " -> " + searchId);
        }
    }

    public static void pop(UUID searchId) {
        if (searchId != null)
            STACK.get().remove(searchId);
    }

    public static void clear() {
        STACK.remove();
    }
}
