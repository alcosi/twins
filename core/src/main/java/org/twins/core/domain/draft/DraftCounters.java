
package org.twins.core.domain.draft;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.cambium.common.exception.ServiceException;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.*;

@Data
@Accessors(chain = true)
public class DraftCounters {
    private Hashtable<Counter, Integer> values = new Hashtable<>();
    private Set<Counter> invalid = new HashSet<>(List.of(Counter.values())); //all counters are invalid from the beginning

    public void set(Counter counter, Integer value) {
        this.values.put(counter, value);
        invalid.remove(counter);
    }

    public Integer getOrZero(Counter counter) {
        return values.getOrDefault(counter, 0);
    }

    public Integer getOrZero(CounterGroup counterGroup) {
        Integer ret = 0;
        for (Counter counter : counterGroup.counters)
            ret += getOrZero(counter);
        return ret;
    }

    public Integer useNullForZero(Counter counter) {
        Integer ret = get(counter);
        return (ret == null || ret == 0) ? null : ret;
    }

    public Integer useNullForZero(CounterGroup counterGroup) {
        Integer ret = getOrZero(counterGroup);
        return ret == 0 ? null : ret;
    }

    public Integer get(Counter counter) {
        return values.get(counter);
    }

    public boolean moreThenZero(Counter counter) {
        return getOrZero(counter) > 0;
    }

    public boolean isZero(Counter counter) {
        return getOrZero(counter) == 0;
    }

    public boolean moreThenZero(CounterGroup counterGroup) {
        return getOrZero(counterGroup) > 0;
    }

    public boolean isZero(CounterGroup counterGroup) {
        return getOrZero(counterGroup) == 0;
    }

    public DraftCounters subtract(Counter counter, Integer value) throws ServiceException {
        Integer currentValue = values.get(counter);
        if (currentValue != null) {
            if (currentValue > value) {
                throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "Current counter value is less then subtracted value");
            }
            values.put(counter, currentValue - value);
        }
        return this;
    }

    public DraftCounters add(Counter counter, Integer value) throws ServiceException {
        Integer currentValue = getOrZero(counter);
        values.put(counter, currentValue - value);
        return this;
    }


    public boolean canBeCommited() {
        return getOrZero(CounterGroup.COMMIT_BLOCKERS) == 0 && isValid(CounterGroup.COMMIT_BLOCKERS);
    }

    public void invalidateIfNotZero(CounterGroup counterGroup, int normalizeCount) {
        if (normalizeCount <= 0)
            return;
        invalid.addAll(Arrays.asList(counterGroup.counters));
    }

    public boolean allAreValid() {
        return invalid.isEmpty();
    }

    public Integer allCountersValues() {
        Integer ret = 0;
        for (Integer value : values.values())
            ret += value;
        return ret;
    }

    public boolean isValid(CounterGroup counters) {
        return isValid(counters.counters);
    }

    public boolean isValid(Counter... counters) {
        if (invalid.isEmpty())
            return true;
        for (Counter counter : counters) {
            if (invalid.contains(counter))
                return false;
        }
        return true;
    }

    public DraftCounters setValid(CounterGroup counterGroup) {
        return setValid(counterGroup.counters);
    }

    public DraftCounters setValid(Counter... counters) {
        Arrays.asList(counters).forEach(invalid::remove);
        return this;
    }

    public enum Counter {
        ERASE_UNDETECTED,
        ERASE_BY_STATUS,
        ERASE_IRREVOCABLE_DETECTED,
        ERASE_IRREVOCABLE_HANDLED,
        ERASE_CASCADE_PAUSE,
        ERASE_CASCADE_EXTRACTED,
        ERASE_SKIP,
        ERASE_LOCK,
        PERSIST_CREATE,
        PERSIST_UPDATE,
        LINK_CREATE,
        LINK_UPDATE,
        LINK_DELETE,
        ATTACHMENT_CREATE,
        ATTACHMENT_UPDATE,
        ATTACHMENT_DELETE,
        MARKER_CREATE,
        MARKER_DELETE,
        TAG_CREATE,
        TAG_DELETE,
        FIELD_SIMPLE_CREATE,
        FIELD_SIMPLE_UPDATE,
        FIELD_SIMPLE_DELETE,
        FIELD_SIMPLE_NON_INDEXED_CREATE,
        FIELD_SIMPLE_NON_INDEXED_UPDATE,
        FIELD_SIMPLE_NON_INDEXED_DELETE,
        FIELD_BOOLEAN_CREATE,
        FIELD_BOOLEAN_UPDATE,
        FIELD_BOOLEAN_DELETE,
        FIELD_USER_CREATE,
        FIELD_USER_UPDATE,
        FIELD_USER_DELETE,
        FIELD_DATALIST_CREATE,
        FIELD_DATALIST_UPDATE,
        FIELD_DATALIST_DELETE,
        FIELD_TWIN_CLASS_CREATE,
        FIELD_TWIN_CLASS_UPDATE,
        FIELD_TWIN_CLASS_DELETE
    }

    @RequiredArgsConstructor
    public enum CounterGroup {
        ERASES(new Counter[]{
                Counter.ERASE_UNDETECTED,
                Counter.ERASE_BY_STATUS,
                Counter.ERASE_IRREVOCABLE_DETECTED,
                Counter.ERASE_IRREVOCABLE_HANDLED,
                Counter.ERASE_CASCADE_PAUSE,
                Counter.ERASE_CASCADE_EXTRACTED,
                Counter.ERASE_SKIP,
                Counter.ERASE_LOCK}),
        PERSISTS(new Counter[]{
                Counter.PERSIST_CREATE,
                Counter.PERSIST_UPDATE}),
        LINKS(new Counter[]{
                Counter.LINK_CREATE,
                Counter.LINK_UPDATE,
                Counter.LINK_DELETE}),
        ATTACHMENTS(new Counter[]{
                Counter.ATTACHMENT_CREATE,
                Counter.ATTACHMENT_UPDATE,
                Counter.ATTACHMENT_DELETE}),
        TAGS(new Counter[]{
                Counter.TAG_CREATE,
                Counter.TAG_DELETE}),
        MARKERS(new Counter[]{
                Counter.MARKER_CREATE,
                Counter.MARKER_DELETE}),
        FIELDS_SIMPLE(new Counter[]{
                Counter.FIELD_SIMPLE_CREATE,
                Counter.FIELD_SIMPLE_UPDATE,
                Counter.FIELD_SIMPLE_DELETE}),
        FIELDS_SIMPLE_NON_INDEXED(new Counter[]{
                Counter.FIELD_SIMPLE_NON_INDEXED_CREATE,
                Counter.FIELD_SIMPLE_NON_INDEXED_UPDATE,
                Counter.FIELD_SIMPLE_NON_INDEXED_DELETE}),
        FIELDS_BOOLEAN(new Counter[]{
                Counter.FIELD_BOOLEAN_CREATE,
                Counter.FIELD_BOOLEAN_UPDATE,
                Counter.FIELD_BOOLEAN_DELETE}),
        FIELDS_USER(new Counter[]{
                Counter.FIELD_USER_CREATE,
                Counter.FIELD_USER_UPDATE,
                Counter.FIELD_USER_DELETE}),
        FIELDS_TWIN_CLASS(new Counter[]{
                Counter.FIELD_TWIN_CLASS_CREATE,
                Counter.FIELD_TWIN_CLASS_UPDATE,
                Counter.FIELD_TWIN_CLASS_DELETE}),
        FIELDS_DATALIST(new Counter[]{
                Counter.FIELD_DATALIST_CREATE,
                Counter.FIELD_DATALIST_UPDATE,
                Counter.FIELD_DATALIST_DELETE}),
        FIELDS(new Counter[]{
                Counter.FIELD_SIMPLE_CREATE,
                Counter.FIELD_SIMPLE_UPDATE,
                Counter.FIELD_SIMPLE_DELETE,
                Counter.FIELD_SIMPLE_NON_INDEXED_CREATE,
                Counter.FIELD_SIMPLE_NON_INDEXED_UPDATE,
                Counter.FIELD_SIMPLE_NON_INDEXED_DELETE,
                Counter.FIELD_BOOLEAN_CREATE,
                Counter.FIELD_BOOLEAN_UPDATE,
                Counter.FIELD_BOOLEAN_DELETE,
                Counter.FIELD_USER_CREATE,
                Counter.FIELD_USER_UPDATE,
                Counter.FIELD_USER_DELETE,
                Counter.FIELD_DATALIST_CREATE,
                Counter.FIELD_DATALIST_UPDATE,
                Counter.FIELD_DATALIST_DELETE,
                Counter.FIELD_TWIN_CLASS_CREATE,
                Counter.FIELD_TWIN_CLASS_UPDATE,
                Counter.FIELD_TWIN_CLASS_DELETE}),
        COMMIT_BLOCKERS(new Counter[]{
                Counter.ERASE_UNDETECTED,
                Counter.ERASE_IRREVOCABLE_DETECTED,
                Counter.ERASE_CASCADE_PAUSE,
                Counter.ERASE_LOCK});

        private final Counter[] counters;
    }
}