package org.twins.core.domain;

import lombok.Getter;
import org.cambium.common.util.ChangesHelper;

import java.util.*;

@Getter
public class EntitiesChangesCollector {
    Map<Class<?>, Map<Object, ChangesHelper>> saveEntityMap = new HashMap<>();
    Map<Class<?>, List<UUID>> deleteEntityIdMap = new HashMap<>();

    private ChangesHelper detectChangesHelper(Object entity) {
        Map<Object, ChangesHelper> entityClassChanges = saveEntityMap.computeIfAbsent(entity.getClass(), k -> new HashMap<>());
        ChangesHelper changesHelper = entityClassChanges.get(entity);
        if (changesHelper == null) {
            changesHelper = new ChangesHelper();
            entityClassChanges.put(entity, changesHelper);
        }
        return changesHelper;
    }

    public EntitiesChangesCollector add(Object entity, String field, Object oldValue, Object newValue) {
        detectChangesHelper(entity).add(field, oldValue, newValue);
        return this;
    }

    public EntitiesChangesCollector add(Object entity) {
        detectChangesHelper(entity);
        return this;
    }

    public boolean isChanged(Object entity, String field, Object oldValue, Object newValue) {
        if (newValue != null && !newValue.equals(oldValue)) {
            detectChangesHelper(entity).add(field, oldValue, newValue);
            return true;
        }
        return false;
    }

    public boolean hasChanges() {
        return !saveEntityMap.isEmpty() || !deleteEntityIdMap.isEmpty();
    }

    public void deleteAll(Class entityClass, Collection<UUID> entitiesIds) {
        for (UUID id : entitiesIds)
            delete(entityClass, id);
    }

    public void delete(Class entityClass, UUID entity) {
        List<UUID> entityClassDeletions = deleteEntityIdMap.computeIfAbsent(entityClass, k -> new ArrayList<>());
        entityClassDeletions.add(entity);
    }
}
