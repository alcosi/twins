package org.twins.core.mappers.rest;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.service.SystemEntityService;

import java.util.*;

@Slf4j
public class MapperContext {

    @Getter
    private boolean lazyRelations = true;
    private Hashtable<String, Object> properties = new Hashtable<>();

    @Getter
    private Map<UUID, RelatedObject<UserEntity>> relatedUserMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinClassEntity>> relatedTwinClassMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinStatusEntity>> relatedTwinStatusMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinEntity>> relatedTwinMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinflowTransitionEntity>> relatedTwinflowTransitionMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<DataListEntity>> relatedDataListMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<DataListOptionEntity>> relatedDataListOptionMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<SpaceRoleUserEntity>> relatedSpaceRoleMap = new LinkedHashMap<>();
    private MapperModeMap modes = new MapperModeMap();
    private Hashtable<Class, Hashtable<String, Object>> cachedObjects = new Hashtable<>(); //already converted objects

    public static MapperContext create() {
        return new MapperContext();
    }

    public MapperContext setMode(MapperMode mapperMode) {
        modes.put(mapperMode);
        return this;
    }

    public MapperContext setModes(MapperMode... mapperModes) {
        if (mapperModes != null)
            for (MapperMode mapperMode : mapperModes)
                modes.put(mapperMode);
        return this;
    }

    public MapperContext setModesMap(MapperModeMap mapperModeMap) {
        modes = mapperModeMap;
        return this;
    }

    /**
     * we will set given mode only if no mode is set or if already existed mode has lower priority
     */
    public MapperContext setPriorityMinMode(MapperMode mode) {
        MapperMode configuredMode = modes.get(mode.getClass());
        if (configuredMode == null || configuredMode.getPriority() < mode.getPriority())
            setMode(mode);
        //case: several modes with identical priorities in the MapperMode implementer
        else if (!configuredMode.equals(mode) && configuredMode.getPriority() == mode.getPriority()) {
            setMode(getUpperModeByPriorityOrUse(mode, configuredMode));
        }
        return this;
    }

    private MapperMode getUpperModeByPriorityOrUse(MapperMode checkForUpperMode, MapperMode forUseModeIfUpperIsAbsent) {
        Class<? extends MapperMode> modeClass = checkForUpperMode.getClass();
        try {
            MapperMode[] enumConstants = modeClass.getEnumConstants();
            MapperMode upperMode = null;

            for (MapperMode mode : enumConstants)
                if (mode.getPriority() > checkForUpperMode.getPriority() && (upperMode == null || upperMode.getPriority() > mode.getPriority()))
                    upperMode = mode;

            if (upperMode != null) return upperMode;
            else return forUseModeIfUpperIsAbsent;
        } catch (Exception e) {
            log.error(e.getMessage());
            return forUseModeIfUpperIsAbsent;
        }
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

    public boolean addRelatedObjectCollection(Collection<?> relatedObjectCollection) {
        if (relatedObjectCollection == null)
            return true;
        relatedObjectCollection.forEach(this::addRelatedObject);
        return true;
    }

    public List<UUID> addRelatedObjectMap(Map<UUID, ?> relatedObjectMap) {
        if (relatedObjectMap == null)
            return null;
        relatedObjectMap.values().forEach(this::addRelatedObject);
        return relatedObjectMap.keySet().stream().toList();
    }

    public boolean addRelatedObject(Object relatedObject) {
        if (relatedObject == null)
            return true;
        if (relatedObject instanceof UserEntity user)
            smartPut(relatedUserMap, user, user.getId());
        else if (relatedObject instanceof TwinClassEntity twinClass)
            smartPut(relatedTwinClassMap, twinClass, twinClass.getId());
        else if (relatedObject instanceof TwinStatusEntity twinStatus)
            smartPut(relatedTwinStatusMap, twinStatus, twinStatus.getId());
        else if (relatedObject instanceof TwinEntity twin) {
            if (!SystemEntityService.isSystemClass(twin.getTwinClassId())) // system twins (user and ba) will be skipped
                smartPut(relatedTwinMap, twin, twin.getId());
        }
        else if (relatedObject instanceof TwinflowTransitionEntity twinflowTransition)
            smartPut(relatedTwinflowTransitionMap, twinflowTransition, twinflowTransition.getId());
        else if (relatedObject instanceof DataListEntity dataList)
            smartPut(relatedDataListMap, dataList, dataList.getId());
        else if (relatedObject instanceof DataListOptionEntity dataListOption)
            smartPut(relatedDataListOptionMap, dataListOption, dataListOption.getId());
        else if (relatedObject instanceof SpaceRoleUserEntity spaceRole)
            smartPut(relatedSpaceRoleMap, spaceRole, spaceRole.getId());
        else {
            debugLog(relatedObject, " can not be stored in mapperContext");
            return false;
        }
        if (relatedObject instanceof EasyLoggable loggable)
            log.debug(loggable.easyLog(EasyLoggable.Level.NORMAL) + " will be converted later");
        return true;
    }

    /**
     * We should not blindly put new related object to correspondence map.
     * In some case we can already have an object with same id in map, but with more detailed modes.
     * So we have to use the most detailed modes in such case
     */
    public <E> void smartPut(Map<UUID, RelatedObject<E>> map, E object, UUID id) {
        RelatedObject<E> alreadyRelated = map.get(id);
        if (alreadyRelated == null) {
            MapperModeMap relatedObjectModes = isolateModes();
            map.put(id, new RelatedObject<>(object, relatedObjectModes));
            return;
        }
        // merge modes. detailed is more high priority
        MapperMode alreadyRegisteredMode;
        for (MapperMode newMapperMode : modes.values()) {
            alreadyRegisteredMode = alreadyRelated.getModes().get(newMapperMode.getClass());
            if (alreadyRegisteredMode == null || alreadyRegisteredMode.getPriority() < newMapperMode.getPriority())
                alreadyRelated.getModes().put(newMapperMode);
            //else current stored mode is more priority
        }
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

    /**
     * if we have some more priority mode in map we will use it, otherwise wi will user this
     */
    public <T extends MapperMode> T getPriorityModeOrUse(T mode) {
        MapperMode configuredMode = modes.get(mode.getClass());
        if (configuredMode != null && configuredMode.getPriority() > mode.getPriority())
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

    public <T extends MapperMode> boolean hasEmpty(T mode) {
        MapperMode configuredMode = modes.get(mode.getClass());
        return configuredMode == null;
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

    public MapperModeMap isolateModes() {
        return new MapperModeMap(this.modes);
    }

    public MapperContext cloneIgnoreRelatedObjects() {
        MapperContext mapperContext = new MapperContext();
        mapperContext.lazyRelations = this.lazyRelations;
        mapperContext.modes = isolateModes(); // new map with presets
        mapperContext.cachedObjects = this.cachedObjects; // same map
        mapperContext.properties = new Hashtable<>(this.properties); // new map with presets
        return mapperContext;
    }

    public MapperContext cloneWithIsolatedModes() {
        MapperContext mapperContext = cloneIgnoreRelatedObjects();
        mapperContext.relatedUserMap = this.relatedUserMap;
        mapperContext.relatedTwinClassMap = this.relatedTwinClassMap;
        mapperContext.relatedTwinStatusMap = this.relatedTwinStatusMap;
        mapperContext.relatedTwinMap = this.relatedTwinMap;
        mapperContext.relatedTwinflowTransitionMap = this.relatedTwinflowTransitionMap;
        mapperContext.relatedDataListMap = this.relatedDataListMap;
        mapperContext.relatedDataListOptionMap = this.relatedDataListOptionMap;
        mapperContext.relatedSpaceRoleMap = this.relatedSpaceRoleMap;
        return mapperContext;
    }

    public MapperContext cloneWithIsolatedModes(MapperModeCollection mapperModeCollection) {
        MapperContext cloneMapperContext = cloneWithIsolatedModes();
        mapperModeCollection = getModeOrUse(mapperModeCollection);
        cloneMapperContext.setModes(mapperModeCollection.getConfiguredModes());
        return cloneMapperContext;
    }

    private void debugLog(Object obj, String message) {
        if (obj instanceof EasyLoggable loggable)
            log.debug(loggable.easyLog(EasyLoggable.Level.NORMAL) + message);
        else
            log.debug("Object of class[" + obj.getClass().getSimpleName() + "]" + message);
    }
}
