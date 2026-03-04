package org.twins.core.domain;

import lombok.Getter;
import org.cambium.common.util.ChangesHelper;
import org.hibernate.Hibernate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntitiesChangesCollector {
    @Getter
    Map<Class<?>, Map<EntityKey, ChangesHelper>> saveEntityMap = new ConcurrentHashMap<>();
    @Getter
    Map<Class<?>, Set<Object>> deleteEntityMap = new ConcurrentHashMap<>();
    //    Map<Class<?>, Set<UUID>> deleteEntityIdMap = new HashMap<>(); id's is not enough for drafting

    public EntitiesChangesCollector() {}

    protected ChangesHelper detectChangesHelper(Identifiable entity) {
        //todo perhaps we need to call Hibernate.getClass
        Class<?> entityClass = entity.getClass();
        UUID entityId = entity.getId();
        EntityKey entityKey = new EntityKey(entityId, entity);

        Map<EntityKey, ChangesHelper> entityClassChanges = saveEntityMap.computeIfAbsent(entityClass, k -> new ConcurrentHashMap<>());
        return entityClassChanges.computeIfAbsent(entityKey, k -> new ChangesHelper());
    }

    public List<Object> getSaveEntitiesAll() {
        List<Object> ret = new ArrayList<>();
        saveEntityMap.forEach((k, v) -> v.keySet().forEach(key -> ret.add(key.entity())));
        return ret;
    }

    public <T> Set<T> getSaveEntities(Class<T> entityClass) {
        Map<EntityKey, ChangesHelper> map = saveEntityMap.get(entityClass);
        if (map == null) {
            return Collections.emptySet();
        }
        Set<T> result = new HashSet<>();
        for (EntityKey key : map.keySet()) {
            result.add((T) key.entity());
        }
        return result;
    }

    public EntitiesChangesCollector add(Identifiable entity, String field, Object oldValue, Object newValue) {
        detectChangesHelper(entity).add(field, oldValue, newValue);
        return this;
    }

    public EntitiesChangesCollector add(Identifiable entity) {
        detectChangesHelper(entity);
        return this;
    }

    public EntitiesChangesCollector addAll(Collection<? extends Identifiable> entities) {
        for (Identifiable entity : entities) {
            detectChangesHelper(entity);
        }
        return this;
    }

    public boolean collectIfChanged(Identifiable entity, String field, Object oldValue, Object newValue) {
        if (newValue != null && !newValue.equals(oldValue)) {
            detectChangesHelper(entity).addWithNullifySupport(field, oldValue, newValue);
            return true;
        }
        return false;
    }

    public boolean hasChanges() {
        return !saveEntityMap.isEmpty() || !deleteEntityMap.isEmpty();
    }

    public boolean hasChanges(Identifiable entity) {
        if (!hasChanges())
            return false;
        if (saveEntityMap.containsKey(entity.getClass()) && saveEntityMap.get(entity.getClass()).containsKey(new EntityKey(entity.getId(), entity)))
            return true;
        if (deleteEntityMap.containsKey(entity.getClass()) && deleteEntityMap.get(entity.getClass()).contains(entity))
            return true;
        return false;
    }

    public void deleteAll(Collection<? extends Identifiable> entities) {
        for (Identifiable entity : entities)
            delete(entity);
    }

    public void delete(Identifiable entity) {
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
