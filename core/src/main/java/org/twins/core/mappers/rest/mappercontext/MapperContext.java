package org.twins.core.mappers.rest.mappercontext;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.featurer.dao.FeaturerTypeEntity;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListSubsetEntity;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.factory.*;
import org.twins.core.dao.history.HistoryTypeEntity;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.notification.*;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.*;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.service.SystemIdLookup;

import java.util.*;
import java.util.function.Function;

@Slf4j
public class MapperContext {

    @Getter
    private boolean lazyRelations = true;
    private Hashtable<String, Object> properties = new Hashtable<>();

    /**
     * Postponed related objects: outer key is the registered class of the postponed object (see
     * {@link #RELATED_ID_GETTERS}), inner key is the object id (dedup + mode merging in smartPut).
     * Inner maps are created lazily; cloneIgnoreRelatedObjects starts a fresh outer map (level isolation),
     * fork() shares it by reference (see linkToRelatedObjects).
     */
    private Map<Class<?>, Map<Object, RelatedObject<?>>> relatedMaps = new LinkedHashMap<>();

    /** Id getters of all postponable classes; doubles as the whitelist checked by addRelatedObject. */
    private static final Map<Class<?>, Function<Object, Object>> RELATED_ID_GETTERS = buildRelatedIdGetters();

    private static Map<Class<?>, Function<Object, Object>> buildRelatedIdGetters() {
        Map<Class<?>, Function<Object, Object>> idGetters = new HashMap<>();
        register(idGetters, UserEntity.class, UserEntity::getId);
        register(idGetters, UserGroupEntity.class, UserGroupEntity::getId);
        register(idGetters, TwinClassEntity.class, TwinClassEntity::getId);
        register(idGetters, TwinStatusEntity.class, TwinStatusEntity::getId);
        register(idGetters, LinkEntity.class, LinkEntity::getId);
        register(idGetters, TwinTriggerEntity.class, TwinTriggerEntity::getId);
        register(idGetters, TwinEntity.class, TwinEntity::getId);
        register(idGetters, TwinflowTransitionEntity.class, TwinflowTransitionEntity::getId);
        register(idGetters, TwinflowFactoryEntity.class, TwinflowFactoryEntity::getId);
        register(idGetters, DataListEntity.class, DataListEntity::getId);
        register(idGetters, DataListOptionEntity.class, DataListOptionEntity::getId);
        register(idGetters, DataListSubsetEntity.class, DataListSubsetEntity::getId);
        register(idGetters, SpaceRoleEntity.class, SpaceRoleEntity::getId);
        register(idGetters, BusinessAccountEntity.class, BusinessAccountEntity::getId);
        register(idGetters, PermissionGroupEntity.class, PermissionGroupEntity::getId);
        register(idGetters, PermissionSchemaEntity.class, PermissionSchemaEntity::getId);
        register(idGetters, PermissionEntity.class, PermissionEntity::getId);
        register(idGetters, TwinflowEntity.class, TwinflowEntity::getId);
        register(idGetters, TwinFactoryEntity.class, TwinFactoryEntity::getId);
        register(idGetters, TwinFactoryPipelineEntity.class, TwinFactoryPipelineEntity::getId);
        register(idGetters, TwinFactoryConditionSetEntity.class, TwinFactoryConditionSetEntity::getId);
        register(idGetters, TwinFactoryMultiplierEntity.class, TwinFactoryMultiplierEntity::getId);
        register(idGetters, TwinFactoryBranchEntity.class, TwinFactoryBranchEntity::getId);
        register(idGetters, TwinFactoryPipelineStepEntity.class, TwinFactoryPipelineStepEntity::getId);
        register(idGetters, TwinFactoryMultiplierFilterEntity.class, TwinFactoryMultiplierFilterEntity::getId);
        register(idGetters, TwinFactoryEraserEntity.class, TwinFactoryEraserEntity::getId);
        register(idGetters, TwinFactoryTriggerEntity.class, TwinFactoryTriggerEntity::getId);
        register(idGetters, TwinFactoryConditionEntity.class, TwinFactoryConditionEntity::getId);
        register(idGetters, FeaturerEntity.class, FeaturerEntity::getId);
        register(idGetters, FeaturerTypeEntity.class, FeaturerTypeEntity::getId);
        register(idGetters, FaceEntity.class, FaceEntity::getId);
        register(idGetters, TwinClassFieldEntity.class, TwinClassFieldEntity::getId);
        register(idGetters, TwinCommentEntity.class, TwinCommentEntity::getId);
        register(idGetters, TwinClassSchemaEntity.class, TwinClassSchemaEntity::getId);
        register(idGetters, TwinflowSchemaEntity.class, TwinflowSchemaEntity::getId);
        register(idGetters, TierEntity.class, TierEntity::getId);
        register(idGetters, TwinAttachmentRestrictionEntity.class, TwinAttachmentRestrictionEntity::getId);
        register(idGetters, TwinClassFreezeEntity.class, TwinClassFreezeEntity::getId);
        register(idGetters, TwinClassFieldRuleEntity.class, TwinClassFieldRuleEntity::getId);
        register(idGetters, TwinValidatorSetEntity.class, TwinValidatorSetEntity::getId);
        register(idGetters, ProjectionTypeGroupEntity.class, ProjectionTypeGroupEntity::getId);
        register(idGetters, ProjectionTypeEntity.class, ProjectionTypeEntity::getId);
        register(idGetters, SchedulerEntity.class, SchedulerEntity::getId);
        register(idGetters, HistoryNotificationRecipientEntity.class, HistoryNotificationRecipientEntity::getId);
        register(idGetters, NotificationSchemaEntity.class, NotificationSchemaEntity::getId);
        register(idGetters, NotificationChannelEntity.class, NotificationChannelEntity::getId);
        register(idGetters, NotificationContextEntity.class, NotificationContextEntity::getId);
        register(idGetters, NotificationChannelEventEntity.class, NotificationChannelEventEntity::getId);
        register(idGetters, HistoryTypeEntity.class, HistoryTypeEntity::getId);
        register(idGetters, ActionRestrictionReasonEntity.class, ActionRestrictionReasonEntity::getId);
        register(idGetters, TwinPointerEntity.class, TwinPointerEntity::getId);
        //internal transport types: never exposed in relatedObjects, drained by RelatedObjectsRestDTOConverter
        register(idGetters, FeaturerParams.class, FeaturerParams::cacheKey);
        register(idGetters, EntityRef.class, EntityRef::cacheKey);
        return Collections.unmodifiableMap(idGetters);
    }

    private static <E> void register(Map<Class<?>, Function<Object, Object>> idGetters, Class<E> relatedClass, Function<E, Object> idGetter) {
        Function<Object, Object> previous = idGetters.put(relatedClass, object -> idGetter.apply(relatedClass.cast(object)));
        if (previous != null)
            log.error("Duplicate related id getter registration for class[{}]", relatedClass.getName());
    }

    /**
     * Resolves the registered related class for the given class: exact match first, then the superclass
     * chain, so Hibernate proxies (a subclass of the entity) map to the registered entity class.
     */
    private static Class<?> resolveRelatedClass(Class<?> cls) {
        for (Class<?> c = cls; c != null; c = c.getSuperclass())
            if (RELATED_ID_GETTERS.containsKey(c))
                return c;
        return null;
    }

    private MapperModeMap modes = new MapperModeMap();
    private Hashtable<Class, Hashtable<String, Object>> cachedObjects = new Hashtable<>(); //already converted objects

    public static MapperContext create() {
        return new MapperContext();
    }

    public MapperContext setMode(MapperMode mapperMode) {
        modes.put(mapperMode);
        return this;
    }

    public MapperContext removeMode(MapperMode mapperMode) {
        modes.remove(mapperMode);
        return this;
    }

    public MapperContext setModes(MapperModeCollection mapperModeCollection) {
        modes.clear(); // let's clean them! this will delete forked modes during fork on collection
        return setModes(mapperModeCollection.getConfiguredModes());
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

    private static MapperMode getUpperModeByPriorityOrUse(MapperMode checkForUpperMode, MapperMode forUseModeIfUpperIsAbsent) {
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
        log.debug("lazyRelations = {}", lazyRelations);
        return this;
    }

    public MapperContext addProperty(String key, Object value) {
        properties.put(key, value);
        log.debug("property[{}] was set to[{}]", key, value);
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
        if (relatedObject instanceof TwinEntity twin && SystemIdLookup.isSystemClass(twin.getTwinClassId()))
            return true; // system twins (user and business account) are skipped
        Class<?> relatedClass = resolveRelatedClass(relatedObject.getClass());
        if (relatedClass == null) {
            debugLog(relatedObject, " can not be stored in mapperContext");
            return false;
        }
        smartPutRelated(relatedClass, relatedObject, RELATED_ID_GETTERS.get(relatedClass).apply(relatedObject));
        if (relatedObject instanceof EasyLoggable loggable)
            log.debug("{} will be converted later", loggable.logNormal());
        return true;
    }

    @SuppressWarnings("unchecked")
    private void smartPutRelated(Class<?> relatedClass, Object object, Object id) {
        Map<Object, RelatedObject<?>> relatedMap = relatedMaps.computeIfAbsent(relatedClass, key -> new LinkedHashMap<>());
        smartPut((Map<Object, RelatedObject<Object>>) (Map<?, ?>) relatedMap, object, id);
    }

    /**
     * Postponed objects of the given registered class, keyed by id; null when nothing of this class
     * was postponed yet (or the class is not registered at all).
     */
    @SuppressWarnings("unchecked")
    public <E> Map<Object, RelatedObject<E>> getRelatedMap(Class<E> relatedClass) {
        return (Map<Object, RelatedObject<E>>) (Map<?, ?>) relatedMaps.get(relatedClass);
    }

    /**
     * We should not blindly put new related object to correspondence map.
     * In some case we can already have an object with same id in map, but with more detailed modes.
     * So we have to use the most detailed modes in such case
     */
    public <E, K> void smartPut(Map<K, RelatedObject<E>> map, E object, K id) {
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

    public <T extends MapperMode> boolean hasMode(Class<T> modeClass) {
        return modes.containsKey(modeClass);
    }

    public <T extends MapperMode> boolean hasModeOrEmpty(T mode) {
        MapperMode configuredMode = modes.get(mode.getClass());
        if (configuredMode != null)
            return configuredMode.equals(mode);
        else
            return true;
    }

    public <T extends MapperMode> boolean hasModeButNot(T mode) {
        return !hasModeOrEmpty(mode);
    }

    public <T extends MapperMode> boolean hasEmpty(T mode) {
        MapperMode configuredMode = modes.get(mode.getClass());
        return configuredMode == null;
    }

    public <S> S getFromCache(Class<S> clazz, String cacheId) {
        if (cacheId == null) {
            log.debug("CacheId is null for class[{}]", clazz.getSimpleName());
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
            log.error("Incorrect cached object type loaded by cacheId[{}]. Expected[{}] but got[{}]", cacheId, clazz.getSimpleName(), obj.getClass().getSimpleName());
        return null;
    }

    public void putToCache(Class clazz, String cacheId, Object obj) {
        if (cacheId == null) {
            log.debug("CacheId is null for class[{}]", clazz.getSimpleName());
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

    public MapperContext fork() {
        MapperContext mapperContext = cloneIgnoreRelatedObjects();
        linkToRelatedObjects(this, mapperContext);
        return mapperContext;
    }

    public MapperContext forkAndExclude(MapperMode... excludeModes) {
        MapperContext fork = fork();
        if (excludeModes != null) {
            for (MapperMode mapperMode : excludeModes) {
                fork.removeMode(mapperMode);
            }
        }
        return fork;
    }

    public MapperContext forkOnPoint(MapperModePointer<?>... mapperModePointers) {
        MapperContext fork = fork();
        for (MapperModePointer<?> mapperModePointer : mapperModePointers) {
            MapperModePointer<?> configuredPointer = getModeOrUse(mapperModePointer);
            MapperMode pointedMode = configuredPointer.point();
            if (pointedMode == null)
                continue;
            else if (pointedMode instanceof MapperModeCollection modeCollection) {
                fork.setModes(modeCollection); // we will override duplicates
            } else {
                fork.removeMode(mapperModePointer); //this will protect us from stackoverflow
                fork.setMode(pointedMode);
            }
        }
        return fork != null ? fork : this;
    }

    public MapperContext cloneWithFlushedModes() {
        MapperContext mapperContext = new MapperContext();
        mapperContext.lazyRelations = this.lazyRelations;
        mapperContext.cachedObjects = this.cachedObjects; // same map
        mapperContext.properties = new Hashtable<>(this.properties); // new map with presets
        linkToRelatedObjects(this, mapperContext);
        return mapperContext;
    }

    private static void linkToRelatedObjects(MapperContext srcMapperContext, MapperContext dstMapperContext) {
        dstMapperContext.relatedMaps = srcMapperContext.relatedMaps;
    }

    public MapperContext fork(MapperModeCollection mapperModeCollection) {
        MapperContext cloneMapperContext = fork();
        mapperModeCollection = getModeOrUse(mapperModeCollection);
        cloneMapperContext.setModes(mapperModeCollection);
        return cloneMapperContext;
    }

    private void debugLog(Object obj, String message) {
        if (obj instanceof EasyLoggable loggable)
            log.debug("{}{}", loggable.easyLog(EasyLoggable.Level.NORMAL), message);
        else
            log.debug("Object of class[{}]{}", obj.getClass().getSimpleName(), message);
    }
}
