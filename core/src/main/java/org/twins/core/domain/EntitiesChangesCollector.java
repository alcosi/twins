package org.twins.core.domain;

import lombok.Getter;
import org.cambium.common.util.ChangesHelper;
import org.hibernate.Hibernate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntitiesChangesCollector {
    @Getter
    Map<Class<?>, Map<Object, ChangesHelper>> saveEntityMap = new ConcurrentHashMap<>();
    @Getter
    Map<Class<?>, Set<Object>> deleteEntityMap = new ConcurrentHashMap<>();
    //    Map<Class<?>, Set<UUID>> deleteEntityIdMap = new HashMap<>(); id's is not enough for drafting

    public EntitiesChangesCollector() {}

    protected ChangesHelper detectChangesHelper(Object entity) {
        //todo perhaps we need to call Hibernate.getClass
        Map<Object, ChangesHelper> entityClassChanges = saveEntityMap.computeIfAbsent(entity.getClass(), k -> new ConcurrentHashMap<>());
        return entityClassChanges.computeIfAbsent(entity, k -> new ChangesHelper());
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

    public EntitiesChangesCollector addAll(Collection<?> entities) {
        for (Object entity : entities) {
            detectChangesHelper(entity);
        }
        return this;
    }

    public boolean collectIfChanged(Object entity, String field, Object oldValue, Object newValue) {
        if (newValue != null && !newValue.equals(oldValue)) {
            detectChangesHelper(entity).addWithNullifySupport(field, oldValue, newValue);
            return true;
        }
        return false;
    }

    public boolean collectIfChangedWithNullSupport(Object entity, String field, Object oldValue, Object newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            detectChangesHelper(entity).addWithNullifySupport(field, oldValue, newValue);
            return true;
        }
        return false;
    }

    public boolean hasChanges() {
        return !saveEntityMap.isEmpty() || !deleteEntityMap.isEmpty();
    }

    public boolean hasChanges(Object entity) {
        if (!hasChanges())
            return false;
        if (saveEntityMap.containsKey(entity.getClass()) && saveEntityMap.get(entity.getClass()).containsKey(entity))
            return true;
        if (deleteEntityMap.containsKey(entity.getClass()) && deleteEntityMap.get(entity.getClass()).contains(entity))
            return true;
        return false;
    }

    public void deleteAll(Collection<?> entitiesIds) {
        for (Object entity : entitiesIds)
            delete(entity);
    }

    public void delete(Object entity) {
        Set<Object> entityClassDeletions = deleteEntityMap.computeIfAbsent(Hibernate.getClass(entity), k -> new HashSet<>());
        entityClassDeletions.add(entity);
    }

    public <T> Set<T> getDeletes(Class<T> entityClass) {
        Set<Object> deleteEntities = deleteEntityMap.get(entityClass);
        return deleteEntities != null ? (Set<T>) deleteEntities : Collections.emptySet();
    }

    protected void clear() {
        saveEntityMap.clear();
        deleteEntityMap.clear();
    }
}
