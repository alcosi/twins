package org.twins.core.mappers.rest;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Slf4j
@Accessors(fluent = true)
public class MapperContext {
    private boolean lazyRelations = true;
    private Hashtable<String, Object> properties = new Hashtable<>();
    private Map<UUID, UserEntity> relatedUserMap = new LinkedHashMap<>();
    private Map<UUID, TwinClassEntity> relatedTwinClassMap = new LinkedHashMap<>();
    private Map<UUID, TwinStatusEntity> relatedTwinStatusMap = new LinkedHashMap<>();
    private Map<UUID, TwinEntity> relatedTwinMap = new LinkedHashMap<>();
    private Hashtable<Class<MapperMode>, MapperMode> modes = new Hashtable<>();
    private Hashtable<Class, Hashtable<String, Object>> cachedObjects = new Hashtable<>(); //already converted objects

    public static MapperContext create() {
        return new MapperContext();
    }

    public MapperContext setMode(MapperMode mapperMode) {
        modes.put((Class<MapperMode>) mapperMode.getClass(), mapperMode);
        return this;
    }

    public boolean isLazyRelations() {
        return lazyRelations;
    }

    public MapperContext setLazyRelations(boolean lazyRelations) {
        this.lazyRelations = lazyRelations;
        return this;
    }

    public MapperContext addProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    public MapperContext addRelatedObject(Object relatedObject) {
        if (relatedObject == null)
            return this;
        if (relatedObject instanceof UserEntity user)
            relatedUserMap.put(user.getId(), user);
        else if (relatedObject instanceof TwinClassEntity twinClass)
            relatedTwinClassMap.put(twinClass.getId(), twinClass);
        else if (relatedObject instanceof TwinStatusEntity twinStatus) {
            relatedTwinStatusMap().put(twinStatus.getId(), twinStatus);
        } else if (relatedObject instanceof TwinEntity twin) {
            relatedTwinMap().put(twin.getId(), twin);
        }
        return this;
    }

    public <T> T getProperty(String key, Class<T> type) {
        return (T) properties.get(key);
    }

    public MapperContext setModeIfNotPresent(MapperMode mapperMode) {
        MapperMode configuredMode = modes.get(mapperMode.getClass());
        if (configuredMode == null)
            setMode(mapperMode);
        return this;
    }

    public <T extends MapperMode> T getModeOrUse(T mode) {
        MapperMode configuredMode = modes.get(mode.getClass());
        if (configuredMode != null)
            return (T) configuredMode;
        else
            return mode;
    }

    public <T extends MapperMode> boolean hasMode(T mode) {
        MapperMode configuredMode = modes.get(mode.getClass());
        if (configuredMode != null)
            return configuredMode.equals(mode);
        else
            return false;
    }

    public Map<UUID, UserEntity> getRelatedUserMap() {
        return relatedUserMap;
    }

    public Map<UUID, TwinClassEntity> getRelatedTwinClassMap() {
        return relatedTwinClassMap;
    }

    public Map<UUID, TwinEntity> getRelatedTwinMap() {
        return relatedTwinMap;
    }

    public Map<UUID, TwinStatusEntity> getRelatedTwinStatusMap() {
        return relatedTwinStatusMap;
    }

    public <S> S getFromCache(Class<S> clazz, String cacheId) {
        if (cacheId == null)
            return null;
        Hashtable<String, Object> cache =  cachedObjects.get(clazz);
        if (cache == null)
            return null;
        Object obj = cache.get(cacheId);
        if (obj == null)
            return null;
        else if (clazz.isInstance(obj)) {
            return (S) obj;
        }
        else
            log.error("Incorrect cached object type");
        return null;
    }

    public void putToCache(Class clazz, String cacheId, Object obj) {
        if (cacheId == null)
            return;
        Hashtable<String, Object> cache =  cachedObjects.get(clazz);
        if (cache == null) {
            cache = new Hashtable<>();
            cachedObjects.put(clazz, cache);
        }
        if (!clazz.isInstance(obj)) {
            log.error("Incorrect cached object type");
            return;
        }
        cache.put(cacheId, obj);
    }
}
