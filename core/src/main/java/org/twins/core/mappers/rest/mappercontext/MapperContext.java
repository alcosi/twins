package org.twins.core.mappers.rest.mappercontext;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.dao.FeaturerEntity;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.comment.TwinCommentEntity;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.*;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;
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
    private Map<UUID, RelatedObject<UserGroupEntity>> relatedUserGroupMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinClassEntity>> relatedTwinClassMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinStatusEntity>> relatedTwinStatusMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinTriggerEntity>> relatedTwinTriggerMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinEntity>> relatedTwinMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinflowTransitionEntity>> relatedTwinflowTransitionMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<DataListEntity>> relatedDataListMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<DataListOptionEntity>> relatedDataListOptionMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<SpaceRoleEntity>> relatedSpaceRoleMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<BusinessAccountEntity>> relatedBusinessAccountMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<PermissionGroupEntity>> relatedPermissionGroupMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<PermissionSchemaEntity>> relatedPermissionSchemaMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<PermissionEntity>> relatedPermissionMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinflowEntity>> relatedTwinflowMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinFactoryEntity>> relatedFactoryMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinFactoryPipelineEntity>> relatedFactoryPipelineMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinFactoryConditionSetEntity>> relatedFactoryConditionSetMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinFactoryMultiplierEntity>> relatedFactoryMultiplierMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<FaceEntity>> relatedFaceMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<I18nEntity>> relatedI18nMap = new LinkedHashMap<>();
    @Getter
    private Map<Integer, RelatedObject<FeaturerEntity>> relatedFeaturerMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinClassFieldEntity>> relatedTwinClassFieldMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinCommentEntity>> relatedCommentMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinClassSchemaEntity>> relatedTwinClassSchemaMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TierEntity>> relatedTierMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinAttachmentRestrictionEntity>> relatedAttachmentRestrictionMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinClassFreezeEntity>> relatedTwinClassFreezeMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<TwinClassFieldRuleEntity>> relatedClassFieldRuleMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<ProjectionTypeGroupEntity>> relatedProjectionTypeGroupMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<ProjectionTypeEntity>> relatedProjectionTypeMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<SchedulerEntity>> relatedSchedulerMap = new LinkedHashMap<>();
    @Getter
    private Map<UUID, RelatedObject<HistoryNotificationRecipientEntity>> relatedHistoryNotificationRecipientMap = new LinkedHashMap<>();

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
        else if (relatedObject instanceof UserGroupEntity userGroup)
            smartPut(relatedUserGroupMap, userGroup, userGroup.getId());
        else if (relatedObject instanceof TwinClassEntity twinClass)
            smartPut(relatedTwinClassMap, twinClass, twinClass.getId());
        else if (relatedObject instanceof TwinStatusEntity twinStatus)
            smartPut(relatedTwinStatusMap, twinStatus, twinStatus.getId());
        else if (relatedObject instanceof TwinTriggerEntity twinTrigger)
            smartPut(relatedTwinTriggerMap, twinTrigger, twinTrigger.getId());
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
        else if (relatedObject instanceof SpaceRoleEntity spaceRole)
            smartPut(relatedSpaceRoleMap, spaceRole, spaceRole.getId());
        else if (relatedObject instanceof BusinessAccountEntity businessAccount)
            smartPut(relatedBusinessAccountMap, businessAccount, businessAccount.getId());
        else if (relatedObject instanceof PermissionGroupEntity permissionGroup)
            smartPut(relatedPermissionGroupMap, permissionGroup, permissionGroup.getId());
        else if (relatedObject instanceof PermissionEntity permission)
            smartPut(relatedPermissionMap, permission, permission.getId());
        else if (relatedObject instanceof PermissionSchemaEntity permissionSchema)
            smartPut(relatedPermissionSchemaMap, permissionSchema, permissionSchema.getId());
        else if (relatedObject instanceof TwinflowEntity twinflow)
            smartPut(relatedTwinflowMap, twinflow, twinflow.getId());
        else if (relatedObject instanceof TwinFactoryEntity twinFactory)
            smartPut(relatedFactoryMap, twinFactory, twinFactory.getId());
        else if (relatedObject instanceof TwinFactoryPipelineEntity twinFactoryPipeline)
            smartPut(relatedFactoryPipelineMap, twinFactoryPipeline, twinFactoryPipeline.getId());
        else if (relatedObject instanceof TwinFactoryConditionSetEntity factoryConditionSet)
            smartPut(relatedFactoryConditionSetMap, factoryConditionSet, factoryConditionSet.getId());
        else if (relatedObject instanceof TwinFactoryMultiplierEntity factoryMultiplier)
            smartPut(relatedFactoryMultiplierMap, factoryMultiplier, factoryMultiplier.getId());
        else if (relatedObject instanceof FaceEntity face)
            smartPut(relatedFaceMap, face, face.getId());
        else if (relatedObject instanceof I18nEntity i18n)
            smartPut(relatedI18nMap, i18n, i18n.getId());
        else if (relatedObject instanceof FeaturerEntity featurer)
            smartPut(relatedFeaturerMap, featurer, featurer.getId());
        else if (relatedObject instanceof TwinClassFieldEntity twinClassField)
            smartPut(relatedTwinClassFieldMap, twinClassField, twinClassField.getId());
        else if (relatedObject instanceof TwinCommentEntity entity)
            smartPut(relatedCommentMap, entity, entity.getId());
        else if (relatedObject instanceof TwinClassSchemaEntity twinClassSchema)
            smartPut(relatedTwinClassSchemaMap, twinClassSchema, twinClassSchema.getId());
        else if (relatedObject instanceof TierEntity tier)
            smartPut(relatedTierMap, tier, tier.getId());
        else if (relatedObject instanceof TwinAttachmentRestrictionEntity entity)
            smartPut(relatedAttachmentRestrictionMap, entity, entity.getId());
        else if (relatedObject instanceof TwinClassFieldRuleEntity entity)
            smartPut(relatedClassFieldRuleMap, entity, entity.getId());
        else if (relatedObject instanceof TwinClassFreezeEntity entity)
            smartPut(relatedTwinClassFreezeMap, entity, entity.getId());
        else if (relatedObject instanceof ProjectionTypeGroupEntity entity)
            smartPut(relatedProjectionTypeGroupMap, entity, entity.getId());
        else if (relatedObject instanceof ProjectionTypeEntity entity)
            smartPut(relatedProjectionTypeMap, entity, entity.getId());
        else if (relatedObject instanceof SchedulerEntity entity)
            smartPut(relatedSchedulerMap, entity, entity.getId());
        else if (relatedObject instanceof HistoryNotificationRecipientEntity entity)
            smartPut(relatedHistoryNotificationRecipientMap, entity, entity.getId());
        else {
            debugLog(relatedObject, " can not be stored in mapperContext");
            return false;
        }
        if (relatedObject instanceof EasyLoggable loggable)
            log.debug("{} will be converted later", loggable.logNormal());
        return true;
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
        dstMapperContext.relatedUserMap = srcMapperContext.relatedUserMap;
        dstMapperContext.relatedUserGroupMap = srcMapperContext.relatedUserGroupMap;
        dstMapperContext.relatedTwinClassMap = srcMapperContext.relatedTwinClassMap;
        dstMapperContext.relatedTwinStatusMap = srcMapperContext.relatedTwinStatusMap;
        dstMapperContext.relatedTwinTriggerMap = srcMapperContext.relatedTwinTriggerMap;
        dstMapperContext.relatedTwinMap = srcMapperContext.relatedTwinMap;
        dstMapperContext.relatedTwinflowTransitionMap = srcMapperContext.relatedTwinflowTransitionMap;
        dstMapperContext.relatedDataListMap = srcMapperContext.relatedDataListMap;
        dstMapperContext.relatedDataListOptionMap = srcMapperContext.relatedDataListOptionMap;
        dstMapperContext.relatedSpaceRoleMap = srcMapperContext.relatedSpaceRoleMap;
        dstMapperContext.relatedBusinessAccountMap = srcMapperContext.relatedBusinessAccountMap;
        dstMapperContext.relatedPermissionGroupMap = srcMapperContext.relatedPermissionGroupMap;
        dstMapperContext.relatedPermissionMap = srcMapperContext.relatedPermissionMap;
        dstMapperContext.relatedPermissionSchemaMap = srcMapperContext.relatedPermissionSchemaMap;
        dstMapperContext.relatedTwinflowMap = srcMapperContext.relatedTwinflowMap;
        dstMapperContext.relatedFactoryMap = srcMapperContext.relatedFactoryMap;
        dstMapperContext.relatedFactoryPipelineMap = srcMapperContext.relatedFactoryPipelineMap;
        dstMapperContext.relatedFactoryConditionSetMap = srcMapperContext.relatedFactoryConditionSetMap;
        dstMapperContext.relatedFactoryMultiplierMap = srcMapperContext.relatedFactoryMultiplierMap;
        dstMapperContext.relatedFaceMap = srcMapperContext.relatedFaceMap;
        dstMapperContext.relatedI18nMap = srcMapperContext.relatedI18nMap;
        dstMapperContext.relatedFeaturerMap = srcMapperContext.relatedFeaturerMap;
        dstMapperContext.relatedTwinClassFieldMap = srcMapperContext.relatedTwinClassFieldMap;
        dstMapperContext.relatedCommentMap = srcMapperContext.relatedCommentMap;
        dstMapperContext.relatedTwinClassSchemaMap = srcMapperContext.relatedTwinClassSchemaMap;
        dstMapperContext.relatedTierMap = srcMapperContext.relatedTierMap;
        dstMapperContext.relatedAttachmentRestrictionMap = srcMapperContext.relatedAttachmentRestrictionMap;
        dstMapperContext.relatedTwinClassFreezeMap = srcMapperContext.relatedTwinClassFreezeMap;
        dstMapperContext.relatedClassFieldRuleMap = srcMapperContext.relatedClassFieldRuleMap;
        dstMapperContext.relatedProjectionTypeGroupMap = srcMapperContext.relatedProjectionTypeGroupMap;
        dstMapperContext.relatedProjectionTypeMap = srcMapperContext.relatedProjectionTypeMap;
        dstMapperContext.relatedSchedulerMap = srcMapperContext.relatedSchedulerMap;
        dstMapperContext.relatedHistoryNotificationRecipientMap = srcMapperContext.relatedHistoryNotificationRecipientMap;
    }

    public MapperContext fork(MapperModeCollection mapperModeCollection) {
        MapperContext cloneMapperContext = fork();
        mapperModeCollection = getModeOrUse(mapperModeCollection);
        cloneMapperContext.setModes(mapperModeCollection);
        return cloneMapperContext;
    }

    private void debugLog(Object obj, String message) {
        if (obj instanceof EasyLoggable loggable)
            log.debug(loggable.easyLog(EasyLoggable.Level.NORMAL) + message);
        else
            log.debug("Object of class[" + obj.getClass().getSimpleName() + "]" + message);
    }
}
