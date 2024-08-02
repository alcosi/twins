package org.twins.core.domain;

import lombok.Getter;
import org.cambium.common.util.ChangesHelper;

import java.util.*;

@Getter
public class EntitiesChangesCollector {
    Map<Class<?>, Map<Object, ChangesHelper>> saveEntityMap = new HashMap<>();
//    Map<Class<?>, Set<UUID>> deleteEntityIdMap = new HashMap<>(); id's is not enough for drafting
    Map<Class<?>, Set<Object>> deleteEntityMap = new HashMap<>();

    private ChangesHelper detectChangesHelper(Object entity) {
        Map<Object, ChangesHelper> entityClassChanges = saveEntityMap.computeIfAbsent(entity.getClass(), k -> new HashMap<>());
        ChangesHelper changesHelper = entityClassChanges.get(entity);
        if (changesHelper == null) {
            changesHelper = new ChangesHelper();
            entityClassChanges.put(entity, changesHelper);
        }
        return changesHelper;
    }

    public List<Object> getSaveEntitiesAll() {
        List<Object> ret = new ArrayList<>();
        saveEntityMap.forEach((k, v) -> ret.addAll(v.values()));
        return ret;
    }

    public <T> Set<T> getSaveEntities(Class<T> entityClass) {
        Map<Object, ChangesHelper> map = saveEntityMap.get(entityClass);
        return map != null ? (Set<T>) map.keySet() : Collections.emptySet();
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
        return !saveEntityMap.isEmpty() || !deleteEntityMap.isEmpty();
    }

    public void deleteAll(Collection<?> entitiesIds) {
        for (Object entity : entitiesIds)
            delete(entity);
    }

    public void delete(Object entity) {
        Set<Object> entityClassDeletions = deleteEntityMap.computeIfAbsent(entity.getClass(), k -> new HashSet<>());
        entityClassDeletions.add(entity);
    }

    public <T> Set<T> getDeletes(Class<T> entityClass) {
        Set<Object> deleteEntities = deleteEntityMap.get(entityClass);
        return deleteEntities != null ? (Set<T>) deleteEntities : Collections.emptySet();
    }
}
