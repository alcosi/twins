package org.twins.core.mappers.rest;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.service.SystemEntityService;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class MapperContext {
    private boolean lazyRelations = true;
    private Hashtable<String, Object> properties = new Hashtable<>();
    @Getter
    private final Map<UUID, UserEntity> relatedUserMap = new LinkedHashMap<>();
    @Getter
    private final Map<UUID, TwinClassEntity> relatedTwinClassMap = new LinkedHashMap<>();
    @Getter
    private final Map<UUID, TwinStatusEntity> relatedTwinStatusMap = new LinkedHashMap<>();
    @Getter
    private final Map<UUID, TwinEntity> relatedTwinMap = new LinkedHashMap<>();
    @Getter
    private final Map<UUID, TwinflowTransitionEntity> relatedTwinflowTransitionMap = new LinkedHashMap<>();
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
        log.debug("lazyRelations = " + lazyRelations);
        return this;
    }

    public MapperContext addProperty(String key, Object value) {
        properties.put(key, value);
        log.debug("property[" + key + "] was set to[" + value + "]");
        return this;
    }

    public boolean addRelatedObject(Object relatedObject) {
        if (relatedObject == null)
            return true;
        if (relatedObject instanceof UserEntity user)
            relatedUserMap.put(user.getId(), user);
        else if (relatedObject instanceof TwinClassEntity twinClass)
            relatedTwinClassMap.put(twinClass.getId(), twinClass);
        else if (relatedObject instanceof TwinStatusEntity twinStatus)
            relatedTwinStatusMap.put(twinStatus.getId(), twinStatus);
        else if (relatedObject instanceof TwinEntity twin) {
            if (!SystemEntityService.isSystemClass(twin.getTwinClassId())) // system twins (user and ba) will be skipped
                relatedTwinMap.put(twin.getId(), twin);
        }
        else if (relatedObject instanceof TwinflowTransitionEntity twinflowTransition)
            relatedTwinflowTransitionMap.put(twinflowTransition.getId(), twinflowTransition);
        else {
            debugLog(relatedObject, " can not be stored in mapperContext");
            return false;
        }
        if (relatedObject instanceof EasyLoggable loggable)
            log.debug(loggable.easyLog(EasyLoggable.Level.NORMAL) + " will be converted later");
        return true;
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

    public <T extends MapperMode> boolean hasModeOrEmpty(T mode) {
        MapperMode configuredMode = modes.get(mode.getClass());
        if (configuredMode != null)
            return configuredMode.equals(mode);
        else
            return true;
    }

    public <S> S getFromCache(Class<S> clazz, String cacheId) {
        if (cacheId == null) {
            log.debug("CacheId is null for class[" + clazz.getSimpleName() + "]");
            return null;
        }
        Hashtable<String, Object> cache = cachedObjects.get(clazz);
        if (cache == null)
            return null;
        Object obj = cache.get(cacheId);
        if (obj == null)
            return null;
        else if (clazz.isInstance(obj)) {
            debugLog(obj, " was found by cacheId[" + cacheId + "]");
            return (S) obj;
        } else
            log.error("Incorrect cached object type loaded by cacheId[" + cacheId + "]. Expected[" + clazz.getSimpleName() + "] but got[" + obj.getClass().getSimpleName() + "]");
        return null;
    }

    public void putToCache(Class clazz, String cacheId, Object obj) {
        if (cacheId == null) {
            log.debug("CacheId is null for class[" + clazz.getSimpleName() + "]");
            return;
        }
        Hashtable<String, Object> cache = cachedObjects.get(clazz);
        if (cache == null) {
            cache = new Hashtable<>();
            cachedObjects.put(clazz, cache);
            debugLog(obj, " was added to cache with id[" + cacheId + "]");
        }
        if (!clazz.isInstance(obj)) {
            log.error("Incorrect cached object type");
            return;
        }
        cache.put(cacheId, obj);
    }

    public MapperContext cloneIgnoreRelatedObjects() {
        MapperContext mapperContext = new MapperContext();
        mapperContext.modes = this.modes;
        mapperContext.cachedObjects = this.cachedObjects;
        mapperContext.lazyRelations = this.lazyRelations;
        mapperContext.properties = this.properties;
        return mapperContext;
    }

    private void debugLog(Object obj, String message) {
        if (obj instanceof EasyLoggable loggable)
            log.debug(loggable.easyLog(EasyLoggable.Level.NORMAL) + message);
        else
            log.debug("Object of class[" + obj.getClass().getSimpleName() + "]" + message);
    }
}
