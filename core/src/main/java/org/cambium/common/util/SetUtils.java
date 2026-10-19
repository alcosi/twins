package org.cambium.common.util;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SetUtils {
    public static <T> Set<T> narrowSet(Set<T> mainSet, Set<T> narrowSet) {
        if (org.apache.commons.collections.CollectionUtils.isEmpty(narrowSet))
            return mainSet;
        if (CollectionUtils.isEmpty(mainSet)) //it is ok to limit search in this case
            return narrowSet;
        if (mainSet.size() == 1) // not possible to narrow
            return mainSet;
        Set<T> crossSet = new HashSet<>();
        for (T narrowId : narrowSet) {
            if (mainSet.contains(narrowId))  //we have crossing
                crossSet.add(narrowId);
            // else element must be skipped, because it is not in original set
        }
        return crossSet.isEmpty() ? mainSet : crossSet;
    }

    public static <T> Set<T>  safeAdd(Set<T> set, T element) {
        if (set != null && element != null) set.add(element);
        return set;
    }

    /**
     * Adds a single value to the include or the exclude set of a builder object, depending on the
     * {@code exclude} flag. Collapses the recurring boilerplate of paired {@code xxxList} /
     * {@code xxxExcludeList} fields:
     * <pre>{@code
     * if (exclude)
     *     xxxExcludeList = CollectionUtils.safeAdd(xxxExcludeList, value);
     * else
     *     xxxList = CollectionUtils.safeAdd(xxxList, value);
     * return this;
     * }</pre>
     * Getters/setters are passed as bound method references (e.g. {@code this::getXxxList} /
     * {@code this::setXxxList}); Lombok {@code @Data} + {@code @Accessors(chain = true)} supply both,
     * and the chain setters also satisfy {@link Consumer}. Uses
     * {@link org.cambium.common.util.CollectionUtils#safeAdd} so null sets are lazily created.
     *
     * @param instance      the builder instance (returned for chaining).
     * @param value         the value to add.
     * @param exclude       {@code true} → add to the exclude set, {@code false} → add to the include set.
     * @param includeGetter supplies the current include set.
     * @param includeSetter sets the include set.
     * @param excludeGetter supplies the current exclude set.
     * @param excludeSetter sets the exclude set.
     * @return the builder instance.
     */
    public static <C, T> C safeAdd(C instance, T value, boolean exclude,
                                   Supplier<Set<T>> includeGetter, Consumer<Set<T>> includeSetter,
                                   Supplier<Set<T>> excludeGetter, Consumer<Set<T>> excludeSetter) {
        if (exclude)
            excludeSetter.accept(org.cambium.common.util.CollectionUtils.safeAdd(excludeGetter.get(), value));
        else
            includeSetter.accept(org.cambium.common.util.CollectionUtils.safeAdd(includeGetter.get(), value));
        return instance;
    }

    /**
     * Same as {@link #safeAdd(Object, Object, boolean, Supplier, Consumer, Supplier, Consumer)} but adds a
     * collection of values at once.
     */
    public static <C, T> C safeAddAll(C instance, Collection<T> values, boolean exclude,
                                      Supplier<Set<T>> includeGetter, Consumer<Set<T>> includeSetter,
                                      Supplier<Set<T>> excludeGetter, Consumer<Set<T>> excludeSetter) {
        if (exclude)
            excludeSetter.accept(org.cambium.common.util.CollectionUtils.safeAdd(excludeGetter.get(), values));
        else
            includeSetter.accept(org.cambium.common.util.CollectionUtils.safeAdd(includeGetter.get(), values));
        return instance;
    }
}
