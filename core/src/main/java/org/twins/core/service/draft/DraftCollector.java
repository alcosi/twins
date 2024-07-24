package org.twins.core.service.draft;

import lombok.Getter;
import org.twins.core.dao.draft.DraftEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
public class DraftCollector {
    final DraftEntity draftEntity;
    Map<Class<?>, Set<Object>> saveEntityMap = new HashMap<>();

    public DraftCollector(DraftEntity draftEntity) {
        this.draftEntity = draftEntity;
    }

    public DraftCollector add(Object entity) {
        saveEntityMap.computeIfAbsent(entity.getClass(), k -> new HashSet<>());
        saveEntityMap.get(entity.getClass()).add(entity);
        return this;
    }

    public boolean hasChanges() {
        return !saveEntityMap.isEmpty();
    }

    public void flush() {

    }
}
