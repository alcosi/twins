package org.twins.core.service.twinclass;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.CacheEvictCollector;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.file.FileData;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.*;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.dao.domain.DomainTypeTwinClassOwnerTypeRepository;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionRepository;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.*;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.EntityRelinkOperation;
import org.twins.core.domain.twinclass.TwinClassCreate;
import org.twins.core.domain.twinclass.TwinClassUpdate;
import org.twins.core.enums.EntityRelinkOperationStrategy;
import org.twins.core.enums.domain.DomainType;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.headhunter.HeadHunter;
import org.twins.core.featurer.headhunter.HeadHunterImpl;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.resource.ResourceService;
import org.twins.core.service.twin.TwinMarkerService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twin.TwinTagService;
import org.twins.core.service.twinflow.TwinflowService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.cambium.common.util.CacheUtils.evictCache;
import static org.twins.core.dao.twinclass.TwinClassEntity.convertUuidFromLtreeFormat;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class TwinClassService extends TwinsEntitySecureFindService<TwinClassEntity> {
    private final TwinRepository twinRepository;
    private final TwinClassRepository twinClassRepository;
    private final TwinClassSchemaRepository twinClassSchemaRepository;
    private final TwinClassSchemaMapRepository twinClassSchemaMapRepository;
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    private final TwinClassFreezeService twinClassFreezeService;
    private final EntitySmartService entitySmartService;
    private final I18nService i18nService;
    private final DataListRepository dataListRepository;
    private final PermissionRepository permissionRepository;
    private final DomainTypeTwinClassOwnerTypeRepository domainTypeTwinClassOwnerTypeRepository;
    private final ResourceService resourceService;
    @Lazy
    private final PermissionService permissionService;
    @Lazy
    private final TwinStatusService twinStatusService;
    @Lazy
    private final TwinflowService twinflowService;
    @Lazy
    private final DomainService domainService;
    @Lazy
    private final AuthService authService;
    @Lazy
    private final FeaturerService featurerService;
    @Lazy
    private final TwinMarkerService twinMarkerService;
    @Lazy
    private final TwinTagService twinTagService;
    @Lazy
    private final DataListService dataListService;
    @Lazy
    private final TwinService twinService;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public CrudRepository<TwinClassEntity, UUID> entityRepository() {
        return twinClassRepository;
    }

    @Override
    public Function<TwinClassEntity, UUID> entityGetIdFunction() {
        return TwinClassEntity::getId;
    }

    @Override
    public BiFunction<UUID, String, Optional<TwinClassEntity>> findByDomainIdAndKeyFunction() throws ServiceException {
        return twinClassRepository::findByDomainIdAndKey;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (entity.getDomainId() != null //some system twinClasses can be out of any domain
                && !entity.getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + apiUser.getDomain().easyLog(EasyLoggable.Level.NORMAL));
            return true;
        }
        //todo check permission schema
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        switch (entityValidateMode) {
            case beforeSave:
                ApiUser apiUser = authService.getApiUser();
                if (entity.getDomainId() != null && !entity.getDomainId().equals(apiUser.getDomain().getId()))
                    return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_READ_DENIED.getMessage());

                if (
                        (entity.getHeadTwinClassId() != null && entity.getHeadTwinClassId().equals(entity.getId())) ||
                                (entity.getExtendsTwinClassId() != null && entity.getExtendsTwinClassId().equals(entity.getId()))
                )
                    return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_CYCLE.getMessage());

                if (entity.getTwinClassFreezeId() != null && (entity.getTwinClassFreeze() == null || !entity.getTwinClassFreeze().getId().equals(entity.getTwinClassFreezeId()))) {
                    entity.setTwinClassFreeze(twinClassFreezeService.findEntitySafe(entity.getTwinClassFreezeId()));
                }

                if (entity.getMarkerDataListId() != null
                        && !dataListRepository.existsByIdAndDomainIdOrIdAndDomainIdIsNull(entity.getMarkerDataListId(), apiUser.getDomainId(), entity.getMarkerDataListId()))
                    throw new ServiceException(ErrorCodeTwins.DATALIST_LIST_UNKNOWN, "unknown marker data list id[" + entity.getMarkerDataListId() + "]");
                if (entity.getTagDataListId() != null
                        && !dataListRepository.existsByIdAndDomainIdOrIdAndDomainIdIsNull(entity.getTagDataListId(), apiUser.getDomainId(), entity.getTagDataListId()))
                    throw new ServiceException(ErrorCodeTwins.DATALIST_LIST_UNKNOWN, "unknown tag data list id[" + entity.getTagDataListId() + "]");
                if (entity.getViewPermissionId() != null
                        && !permissionRepository.existsByIdAndPermissionGroup_DomainId(entity.getViewPermissionId(), apiUser.getDomainId()))
                    throw new ServiceException(ErrorCodeTwins.PERMISSION_ID_UNKNOWN, "unknown view permission id[" + entity.getViewPermissionId() + "]");
                if (entity.getEditPermissionId() != null
                        && !permissionRepository.existsByIdAndPermissionGroup_DomainId(entity.getEditPermissionId(), apiUser.getDomainId()))
                    throw new ServiceException(ErrorCodeTwins.PERMISSION_ID_UNKNOWN, "unknown edit permission id[" + entity.getEditPermissionId() + "]");
                if (entity.getCreatePermissionId() != null
                        && !permissionRepository.existsByIdAndPermissionGroup_DomainId(entity.getCreatePermissionId(), apiUser.getDomainId()))
                    throw new ServiceException(ErrorCodeTwins.PERMISSION_ID_UNKNOWN, "unknown create permission id[" + entity.getCreatePermissionId() + "]");
                if (entity.getDeletePermissionId() != null
                        && !permissionRepository.existsByIdAndPermissionGroup_DomainId(entity.getDeletePermissionId(), apiUser.getDomainId()))
                    throw new ServiceException(ErrorCodeTwins.PERMISSION_ID_UNKNOWN, "unknown delete permission id[" + entity.getDeletePermissionId() + "]");
                break;
            default:
        }
        return true;
    }

    @Override
    public CacheSupportType getCacheSupportType() {
        return CacheSupportType.GLOBAL;
    }

    public UUID checkTwinClassSchemaAllowed(UUID domainId, UUID twinClassSchemaId) throws ServiceException {
        Optional<TwinClassSchemaEntity> twinClassSchemaEntity = twinClassSchemaRepository.findById(twinClassSchemaId);
        if (twinClassSchemaEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown twinClassSchemaId[" + twinClassSchemaId + "]");
        if (twinClassSchemaEntity.get().getDomainId() != domainId)
            throw new ServiceException(ErrorCodeTwins.PERMISSION_SCHEMA_NOT_ALLOWED, "twinClassSchemaId[" + twinClassSchemaId + "] is not allows in domain[" + domainId + "]");
        return twinClassSchemaId;
    }

    public TwinClassSchemaMapEntity findTwinClassSchemaMap(UUID twinClassSchemaId, UUID twinClassId) {
        Optional<TwinClassSchemaMapEntity> twinClassSchemaMapEntity = twinClassSchemaMapRepository.findByTwinClassSchemaIdAndTwinClassId(twinClassSchemaId, twinClassId);
        return twinClassSchemaMapEntity.orElse(null);
    }

    @Transactional
    public TwinClassEntity duplicateTwinClass(ApiUser apiUser, UUID twinClassId, String newKey) throws ServiceException {
        TwinClassEntity srcTwinClassEntity = findEntity(twinClassId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
        log.info(srcTwinClassEntity.logShort() + " will be duplicated with ne key[" + newKey + "]");
        TwinClassEntity duplicateTwinClassEntity = new TwinClassEntity()
                .setKey(newKey)
                .setCreatedByUserId(apiUser.getUser().getId())
                .setPermissionSchemaSpace(srcTwinClassEntity.getPermissionSchemaSpace())
                .setTwinflowSchemaSpace(srcTwinClassEntity.getTwinflowSchemaSpace())
                .setTwinClassSchemaSpace(srcTwinClassEntity.getTwinClassSchemaSpace())
                .setAliasSpace(srcTwinClassEntity.getAliasSpace())
                .setAssigneeRequired(srcTwinClassEntity.getAssigneeRequired())
                .setAbstractt(srcTwinClassEntity.getAbstractt())
                .setUniqueName(srcTwinClassEntity.getUniqueName())
                .setExtendsTwinClassId(srcTwinClassEntity.getExtendsTwinClassId())
                .setHeadTwinClassId(srcTwinClassEntity.getHeadTwinClassId())
                .setIconDarkResourceId(srcTwinClassEntity.getIconDarkResourceId())
                .setIconDarkResource(srcTwinClassEntity.getIconDarkResource())
                .setIconLightResourceId(srcTwinClassEntity.getIconLightResourceId())
                .setIconLightResource(srcTwinClassEntity.getIconLightResource())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setDomainId(srcTwinClassEntity.getDomainId())
                .setOwnerType(srcTwinClassEntity.getOwnerType())
                .setAssigneeRequired(srcTwinClassEntity.getAssigneeRequired());
        I18nEntity i18nDuplicate;
        if (srcTwinClassEntity.getNameI18NId() != null) {
            i18nDuplicate = i18nService.duplicateI18n(srcTwinClassEntity.getNameI18NId());
            duplicateTwinClassEntity
                    .setNameI18NId(i18nDuplicate.getId());
        }
        if (srcTwinClassEntity.getDescriptionI18NId() != null) {
            i18nDuplicate = i18nService.duplicateI18n(srcTwinClassEntity.getDescriptionI18NId());
            duplicateTwinClassEntity
                    .setDescriptionI18NId(i18nDuplicate.getId());
        }
        duplicateTwinClassEntity = entitySmartService.save(duplicateTwinClassEntity, twinClassRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        twinClassFieldService.duplicateFieldsForClass(apiUser, twinClassId, duplicateTwinClassEntity.getId());
        return duplicateTwinClassEntity;
    }

    public void loadExtendsHierarchyChildClasses(TwinClassEntity twinClassEntity) throws ServiceException {
        loadExtendsHierarchyChildClasses(Collections.singletonList(twinClassEntity));
    }

    public void loadExtendsHierarchyChildClasses(Collection<TwinClassEntity> twinClassEntityList) throws ServiceException {
        List<TwinClassEntity> needLoad = new ArrayList<>();
        List<String> classLTree = new ArrayList<>();
        for (TwinClassEntity twinClass : twinClassEntityList) {
            if (twinClass.getExtendsHierarchyChildClassKit() != null)
                continue;
            twinClass.setExtendsHierarchyChildClassKit(new Kit<>(TwinClassEntity::getId));
            needLoad.add(twinClass);
            classLTree.add(LTreeUtils.matchInTheMiddle(twinClass.getId()));
        }
        if (CollectionUtils.isEmpty(needLoad))
            return;
        List<TwinClassEntity> childClasses = twinClassRepository.findByDomainIdAndExtendsHierarchyContains(authService.getApiUser().getDomainId(), String.join(",", classLTree));
        for (TwinClassEntity twinClass : needLoad) {
            for (TwinClassEntity childClass : childClasses) {
                if (childClass.getExtendedClassIdSet().contains(twinClass.getId()))
                    twinClass.getExtendsHierarchyChildClassKit().add(childClass);
            }
        }
    }

    public void loadHeadHierarchyChildClasses(TwinClassEntity twinClassEntity) throws ServiceException {
        loadHeadHierarchyChildClasses(Collections.singletonList(twinClassEntity));
    }

    public void loadHeadHierarchyChildClasses(Collection<TwinClassEntity> twinClassEntityList) throws ServiceException {
        List<TwinClassEntity> needLoad = new ArrayList<>();
        List<String> classLTree = new ArrayList<>();
        for (TwinClassEntity twinClass : twinClassEntityList) {
            if (twinClass.getHeadHierarchyChildClassKit() != null)
                continue;
            twinClass.setHeadHierarchyChildClassKit(new Kit<>(TwinClassEntity::getId));
            needLoad.add(twinClass);
            classLTree.add(LTreeUtils.matchInTheMiddle(twinClass.getId()));
        }
        if (CollectionUtils.isEmpty(needLoad))
            return;
        List<TwinClassEntity> childClasses = twinClassRepository.findByDomainIdAndHeadHierarchyContains(authService.getApiUser().getDomainId(), String.join(",", classLTree));
        for (TwinClassEntity twinClass : needLoad) {
            for (TwinClassEntity childClass : childClasses) {
                if (childClass.getId().equals(twinClass.getId()))
                    continue;
                if (childClass.getHeadHierarchyClassIdSet().contains(twinClass.getId()))
                    twinClass.getHeadHierarchyChildClassKit().add(childClass);
            }
        }
    }

    public void loadPermissions(TwinClassEntity twinClassEntity) {
        loadPermissions(Collections.singletonList(twinClassEntity));
    }

    public void loadPermissions(Collection<TwinClassEntity> twinClassEntityCollection) {
        KitGrouped<TwinClassEntity, UUID, UUID> needLoadView = new KitGrouped<>(TwinClassEntity::getId, TwinClassEntity::getViewPermissionId);
        for (TwinClassEntity twinClass : twinClassEntityCollection) {
            if (twinClass.getViewPermission() == null && twinClass.getViewPermissionId() != null)
                needLoadView.add(twinClass);
        }
        KitGrouped<TwinClassEntity, UUID, UUID> needLoadCreate = new KitGrouped<>(TwinClassEntity::getId, TwinClassEntity::getCreatePermissionId);
        for (TwinClassEntity twinClass : twinClassEntityCollection) {
            if (twinClass.getCreatePermission() == null && twinClass.getCreatePermissionId() != null)
                needLoadCreate.add(twinClass);
        }
        KitGrouped<TwinClassEntity, UUID, UUID> needLoadEdit = new KitGrouped<>(TwinClassEntity::getId, TwinClassEntity::getEditPermissionId);
        for (TwinClassEntity twinClass : twinClassEntityCollection) {
            if (twinClass.getEditPermission() == null && twinClass.getEditPermissionId() != null)
                needLoadEdit.add(twinClass);
        }
        KitGrouped<TwinClassEntity, UUID, UUID> needLoadDelete = new KitGrouped<>(TwinClassEntity::getId, TwinClassEntity::getDeletePermissionId);
        for (TwinClassEntity twinClass : twinClassEntityCollection) {
            if (twinClass.getDeletePermission() == null && twinClass.getDeletePermissionId() != null)
                needLoadDelete.add(twinClass);
        }
        if (!KitUtils.isEmpty(needLoadView)) {
            List<PermissionEntity> permissions = permissionRepository.findByIdIn(needLoadView.getGroupedMap().keySet());
            for (PermissionEntity permission : permissions) {
                for (TwinClassEntity twinClass : needLoadView.getGrouped(permission.getId())) {
                    twinClass.setViewPermission(permission);
                }
            }
        }
        if (!KitUtils.isEmpty(needLoadCreate)) {
            List<PermissionEntity> permissions = permissionRepository.findByIdIn(needLoadCreate.getGroupedMap().keySet());
            for (PermissionEntity permission : permissions) {
                for (TwinClassEntity twinClass : needLoadCreate.getGrouped(permission.getId())) {
                    twinClass.setCreatePermission(permission);
                }
            }
        }
        if (!KitUtils.isEmpty(needLoadEdit)) {
            List<PermissionEntity> permissions = permissionRepository.findByIdIn(needLoadEdit.getGroupedMap().keySet());
            for (PermissionEntity permission : permissions) {
                for (TwinClassEntity twinClass : needLoadEdit.getGrouped(permission.getId())) {
                    twinClass.setEditPermission(permission);
                }
            }
        }
        if (!KitUtils.isEmpty(needLoadDelete)) {
            List<PermissionEntity> permissions = permissionRepository.findByIdIn(needLoadDelete.getGroupedMap().keySet());
            for (PermissionEntity permission : permissions) {
                for (TwinClassEntity twinClass : needLoadDelete.getGrouped(permission.getId())) {
                    twinClass.setDeletePermission(permission);
                }
            }
        }

    }

    public boolean isInstanceOf(TwinEntity twin, UUID ofClass) throws ServiceException {
        return isInstanceOf(twin.getTwinClass(), ofClass);
    }

    public boolean isInstanceOf(TwinClassEntity instanceClass, UUID ofClass) throws ServiceException {
        if (!instanceClass.getId().equals(ofClass)) {
            return instanceClass.getExtendedClassIdSet().contains(ofClass);
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinClassEntity createInDomainClass(TwinClassCreate twinClassCreate, FileData iconLight, FileData iconDark) throws ServiceException {
        return createInDomainClass((List.of(twinClassCreate)), iconLight, iconDark).getFirst();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassEntity> createInDomainClass(List<TwinClassCreate> twinClassCreates, FileData iconLight, FileData iconDark) throws ServiceException {
        if (CollectionUtils.isEmpty(twinClassCreates)) {
            return Collections.emptyList();
        }

        ApiUser apiUser = authService.getApiUser();
        List<TwinClassEntity> classesToSave = new ArrayList<>();
        List<TwinClassEntity> classesWithPermissions = new ArrayList<>();
        Map<String, TwinClassCreate> createByOriginalKey = new HashMap<>();

        for (TwinClassCreate create : twinClassCreates) {
            TwinClassEntity twinClass = create.getTwinClass();
            String classKey = KeyUtils.upperCaseNullFriendly(twinClass.getKey(), ErrorCodeTwins.TWIN_CLASS_KEY_INCORRECT);
            createByOriginalKey.put(classKey, create);
            twinClass.setKey(classKey);

            if (twinClassRepository.existsByDomainIdAndKey(apiUser.getDomainId(), classKey)) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_KEY_ALREADY_IN_USE, "Class key already exists: " + classKey);
            }

            if (twinClass.getHeadTwinClassId() == null ||
                    SystemEntityService.isSystemClass(twinClass.getHeadTwinClassId())) {
                twinClass
                        .setHeadHunterFeaturerId(null)
                        .setHeadHunterParams(null);
            } else {
                if (!twinClassRepository.existsByDomainIdAndId(
                        apiUser.getDomainId(),
                        twinClass.getHeadTwinClassId())) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN, "Unknown head twin class id: " + twinClass.getHeadTwinClassId());
                }

                if (twinClass.getHeadHunterFeaturerId() == null) {
                    twinClass
                            .setHeadHunterFeaturerId(HeadHunterImpl.ID_2601)
                            .setHeadHunterParams(null);
                }
            }

            if (twinClass.getHeadHunterFeaturerId() != null) {
                featurerService.checkValid(twinClass.getHeadHunterFeaturerId(), twinClass.getHeadHunterParams(), HeadHunter.class);
                featurerService.prepareForStore(twinClass.getHeadHunterFeaturerId(), twinClass.getHeadHunterParams());
            }

            if (twinClass.getExtendsTwinClassId() != null) {
                if (!twinClassRepository.existsByDomainIdAndId(apiUser.getDomainId(), twinClass.getExtendsTwinClassId())) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN, "Unknown extends twin class id: " + twinClass.getExtendsTwinClassId());
                }
            } else {
                twinClass.setExtendsTwinClassId(apiUser.getDomain().getAncestorTwinClassId());
            }

            twinClass
                    .setNameI18NId(i18nService.createI18nAndTranslations(I18nType.TWIN_CLASS_NAME, create.getNameI18n()).getId())
                    .setDescriptionI18NId(i18nService.createI18nAndTranslations(I18nType.TWIN_CLASS_DESCRIPTION, create.getDescriptionI18n()).getId())
                    .setDomainId(apiUser.getDomainId())
                    .setOwnerType(domainService.checkDomainSupportedTwinClassOwnerType(apiUser.getDomain(), create.getTwinClass().getOwnerType()))
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(apiUser.getUserId());

            if (twinClass.getAssigneeRequired() == null) {
                twinClass.setAssigneeRequired(false);
            }
            if (twinClass.getSegment() == null) {
                twinClass.setSegment(false);
            }
            if (twinClass.getHasDynamicMarkers() == null) {
                twinClass.setHasDynamicMarkers(false);
            }
            if (twinClass.getUniqueName() == null) {
                twinClass.setUniqueName(false);
            }

            twinClass.setHasSegment(false);

            twinClass.setHeadHierarchyCounterDirectChildren(0);
            twinClass.setExtendsHierarchyCounterDirectChildren(0);

            validateEntityAndThrow(twinClass, EntitySmartService.EntityValidateMode.beforeSave);
            processIcons(twinClass, iconLight, iconDark);
            classesToSave.add(twinClass);
        }

        Iterable<TwinClassEntity> savedIterable = entitySmartService.saveAllAndLog(classesToSave, twinClassRepository);
        List<TwinClassEntity> savedClasses = StreamSupport.stream(savedIterable.spliterator(), false).toList();

        for (TwinClassEntity savedClass : savedClasses) {
            Boolean autoCreatePerms = createByOriginalKey.get(savedClass.getKey()).getAutoCreatePermission();
            Boolean autoCreateTwinflow = createByOriginalKey.get(savedClass.getKey()).getAutoCreateTwinflow();

            refreshExtendsHierarchyTree(savedClass);
            refreshHeadHierarchyTree(savedClass);

            if (Boolean.TRUE.equals(autoCreatePerms)) {
                Map<PermissionService.DefaultClassPermissionsPrefix, PermissionEntity> permissions =
                        permissionService.createDefaultPermissionsForNewInDomainClass(savedClass);

                boolean updated = false;
                if (savedClass.getViewPermissionId() == null) {
                    savedClass.setViewPermissionId(permissions.get(PermissionService.DefaultClassPermissionsPrefix.VIEW).getId());
                    savedClass.setViewPermission(permissions.get(PermissionService.DefaultClassPermissionsPrefix.VIEW));
                    updated = true;
                }
                if (savedClass.getEditPermissionId() == null) {
                    savedClass.setEditPermissionId(permissions.get(PermissionService.DefaultClassPermissionsPrefix.EDIT).getId());
                    savedClass.setEditPermission(permissions.get(PermissionService.DefaultClassPermissionsPrefix.EDIT));
                    updated = true;
                }
                if (savedClass.getCreatePermissionId() == null) {
                    savedClass.setCreatePermissionId(permissions.get(PermissionService.DefaultClassPermissionsPrefix.CREATE).getId());
                    savedClass.setCreatePermission(permissions.get(PermissionService.DefaultClassPermissionsPrefix.CREATE));
                    updated = true;
                }
                if (savedClass.getDeletePermissionId() == null) {
                    savedClass.setDeletePermissionId(permissions.get(PermissionService.DefaultClassPermissionsPrefix.DELETE).getId());
                    savedClass.setDeletePermission(permissions.get(PermissionService.DefaultClassPermissionsPrefix.DELETE));
                    updated = true;
                }

                if (updated) {
                    classesWithPermissions.add(savedClass);
                }
            }

            refreshExtendsHierarchyTree(savedClass);
            refreshHeadHierarchyTree(savedClass);

            //todo batch create for twinStatus and twinflow
            if (Boolean.TRUE.equals(autoCreateTwinflow)) {
                TwinStatusEntity status = twinStatusService.createStatus(savedClass, "init", "Initial status");
                TwinflowEntity twinflow = twinflowService.createTwinflow(savedClass, status);
                twinflowService.registerTwinflow(twinflow, apiUser.getDomain(), savedClass);
            }
        }

        if (!classesWithPermissions.isEmpty()) {
            entitySmartService.saveAllAndLog(classesWithPermissions, twinClassRepository);
        }

        return savedClasses;
    }

    public void loadHeadTwinClass(TwinClassEntity twinClassEntity) throws ServiceException {
        if (twinClassEntity.getHeadTwinClassId() == null || twinClassEntity.getHeadTwinClass() != null)
            return;
        twinClassEntity.setHeadTwinClass(findEntitySafe(twinClassEntity.getHeadTwinClassId()));
    }

    public void loadHeadTwinClasses(Collection<TwinClassEntity> twinClassEntityCollection) {
        KitGrouped<TwinClassEntity, UUID, UUID> needLoad = new KitGrouped<>(TwinClassEntity::getId, TwinClassEntity::getHeadTwinClassId);
        for (TwinClassEntity twinClass : twinClassEntityCollection) {
            if (twinClass.getHeadTwinClass() != null)
                continue;
            needLoad.add(twinClass);
        }
        if (KitUtils.isEmpty(needLoad))
            return;
        List<TwinClassEntity> heads = twinClassRepository.findByIdIn(needLoad.getGroupedMap().keySet());
        for (TwinClassEntity headTwinClass : heads) {
            for (TwinClassEntity twinClass : needLoad.getGrouped(headTwinClass.getId()))
                twinClass.setHeadTwinClass(headTwinClass);
        }
    }

    public void loadExtendsTwinClass(TwinClassEntity twinClassEntity) throws ServiceException {
        if (twinClassEntity.getExtendsTwinClassId() == null || twinClassEntity.getExtendsTwinClass() != null)
            return;
        twinClassEntity.setExtendsTwinClass(findEntitySafe(twinClassEntity.getExtendsTwinClassId()));
    }

    public void loadExtendsTwinClasses(Collection<TwinClassEntity> twinClassEntityCollection) {
        KitGrouped<TwinClassEntity, UUID, UUID> needLoad = new KitGrouped<>(TwinClassEntity::getId, TwinClassEntity::getExtendsTwinClassId);
        for (TwinClassEntity twinClass : twinClassEntityCollection) {
            if (twinClass.getExtendsTwinClass() != null)
                continue;
            needLoad.add(twinClass);
        }
        if (KitUtils.isEmpty(needLoad))
            return;
        List<TwinClassEntity> heads = twinClassRepository.findByIdIn(needLoad.getGroupedMap().keySet());
        for (TwinClassEntity extendsTwinClass : heads) {
            for (TwinClassEntity twinClass : needLoad.getGrouped(extendsTwinClass.getId()))
                twinClass.setExtendsTwinClass(extendsTwinClass);
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinClassEntity updateTwinClasses(TwinClassUpdate twinClassUpdate, FileData iconLight, FileData iconDark) throws ServiceException {
        return updateTwinClasses(List.of(twinClassUpdate), iconLight, iconDark).getFirst();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassEntity> updateTwinClasses(List<TwinClassUpdate> twinClassUpdates, FileData iconLight, FileData iconDark) throws ServiceException {
        if (CollectionUtils.isEmpty(twinClassUpdates)) {
            return Collections.emptyList();
        }

        Kit<TwinClassEntity, UUID> dbTwinClassesKit = findEntitiesSafe(
                twinClassUpdates.stream()
                        .map(update -> update.getTwinClass().getId())
                        .collect(Collectors.toList())
        );

        ChangesHelperMulti<TwinClassEntity> changes = new ChangesHelperMulti<>();
        CacheEvictCollector cacheEvictCollector = new CacheEvictCollector();

        List<TwinClassEntity> allEntities = dbTwinClassesKit.getList();

        for (TwinClassUpdate twinClassUpdate : twinClassUpdates) {
            TwinClassEntity dbTwinClassEntity = dbTwinClassesKit.get(twinClassUpdate.getTwinClass().getId());
            if (dbTwinClassEntity.getOwnerType().isSystemLevel())
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "system class can be edited");
            ChangesHelper changesHelper = new ChangesHelper();

            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getAbstractt, TwinClassEntity::setAbstractt, TwinClassEntity.Fields.abstractt, changesHelper);
            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getUniqueName, TwinClassEntity::setUniqueName, TwinClassEntity.Fields.uniqueName, changesHelper);
            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getTwinClassSchemaSpace, TwinClassEntity::setTwinClassSchemaSpace, TwinClassEntity.Fields.twinClassSchemaSpace, changesHelper);
            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getTwinflowSchemaSpace, TwinClassEntity::setTwinflowSchemaSpace, TwinClassEntity.Fields.twinflowSchemaSpace, changesHelper);
            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getAliasSpace, TwinClassEntity::setAliasSpace, TwinClassEntity.Fields.aliasSpace, changesHelper);
            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getAssigneeRequired, TwinClassEntity::setAssigneeRequired, TwinClassEntity.Fields.assigneeRequired, changesHelper);
            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getPermissionSchemaSpace, TwinClassEntity::setPermissionSchemaSpace, TwinClassEntity.Fields.permissionSchemaSpace, changesHelper);
            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getViewPermissionId, TwinClassEntity::setViewPermissionId, TwinClassEntity.Fields.viewPermissionId, changesHelper);
            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getEditPermissionId, TwinClassEntity::setEditPermissionId, TwinClassEntity.Fields.editPermissionId, changesHelper);
            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getCreatePermissionId, TwinClassEntity::setCreatePermissionId, TwinClassEntity.Fields.createPermissionId, changesHelper);
            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getDeletePermissionId, TwinClassEntity::setDeletePermissionId, TwinClassEntity.Fields.deletePermissionId, changesHelper);
            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getKey, TwinClassEntity::setKey, TwinClassEntity.Fields.key, changesHelper);
            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getExternalId, TwinClassEntity::setExternalId, TwinClassEntity.Fields.externalId, changesHelper);
            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getExternalProperties, TwinClassEntity::setExternalProperties, TwinClassEntity.Fields.externalProperties, changesHelper);
            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getExternalJson, TwinClassEntity::setExternalJson, TwinClassEntity.Fields.externalJson, changesHelper);
            updateEntityFieldByEntity(twinClassUpdate.getTwinClass(), dbTwinClassEntity, TwinClassEntity::getTwinClassFreezeId, TwinClassEntity::setTwinClassFreezeId, TwinClassEntity.Fields.twinClassFreezeId, changesHelper);


            updateTwinClassFeaturer(dbTwinClassEntity, twinClassUpdate.getTwinClass().getHeadHunterFeaturerId(), twinClassUpdate.getTwinClass().getHeadHunterParams(), changesHelper);
            i18nService.updateI18nFieldForEntity(twinClassUpdate.getNameI18n(), I18nType.TWIN_CLASS_NAME, dbTwinClassEntity, TwinClassEntity::getNameI18NId, TwinClassEntity::setNameI18NId, TwinClassEntity.Fields.nameI18NId, changesHelper);
            i18nService.updateI18nFieldForEntity(twinClassUpdate.getDescriptionI18n(), I18nType.TWIN_CLASS_DESCRIPTION, dbTwinClassEntity, TwinClassEntity::getDescriptionI18NId, TwinClassEntity::setDescriptionI18NId, TwinClassEntity.Fields.descriptionI18NId, changesHelper);
            updateTwinClassHeadTwinClass(dbTwinClassEntity, twinClassUpdate.getHeadTwinClassUpdate(), changesHelper);
            updateTwinClassExtendsTwinClass(dbTwinClassEntity, twinClassUpdate.getExtendsTwinClassUpdate(), changesHelper);
            updateTwinClassMarkerDataList(dbTwinClassEntity, twinClassUpdate.getMarkerDataListUpdate(), changesHelper);
            updateTwinClassTagDataList(dbTwinClassEntity, twinClassUpdate.getTagDataListUpdate(), changesHelper);
            updateTwinClassIcons(dbTwinClassEntity, iconLight, iconDark, changesHelper);

            if (changesHelper.hasChanges()) {
                changes.add(dbTwinClassEntity, changesHelper);
                cacheEvictCollector.add(dbTwinClassEntity.getId(),
                        TwinClassRepository.CACHE_TWIN_CLASS_BY_ID,
                        TwinClassEntity.class.getSimpleName());
            }
        }

        if (!changes.entrySet().isEmpty()) {
            updateSafe(changes);

            evictCache(cacheManager, cacheEvictCollector);
        }

        return allEntities;
    }

    public void updateTwinClassIcons(TwinClassEntity dbTwinClassEntity, FileData iconLight, FileData iconDark, ChangesHelper changesHelper) throws ServiceException {
        if (iconLight != null) {
            ResourceEntity newValue = saveIconResourceIfExist(iconLight);
            if (changesHelper.isChanged(TwinClassEntity.Fields.iconLightResourceId, dbTwinClassEntity.getIconLightResourceId(), newValue.getId())) {
                dbTwinClassEntity
                        .setIconLightResourceId(newValue.getId())
                        .setIconLightResource(newValue);
            }
        }
        if (iconDark != null) {
            ResourceEntity newValue = saveIconResourceIfExist(iconDark);
            if (changesHelper.isChanged(TwinClassEntity.Fields.iconDarkResourceId, dbTwinClassEntity.getIconDarkResourceId(), newValue.getId())) {
                dbTwinClassEntity
                        .setIconLightResourceId(newValue.getId())
                        .setIconLightResource(newValue);
            }
        }
    }

    public void updateTwinClassFeaturer(TwinClassEntity dbTwinClassEntity, Integer newHeadhunterFeaturerId, HashMap<String, String> headHunterParams, ChangesHelper changesHelper) throws ServiceException {
        if (changesHelper.isChanged(TwinClassEntity.Fields.headHunterFeaturerId, dbTwinClassEntity.getHeadHunterFeaturerId(), newHeadhunterFeaturerId)) {
            FeaturerEntity newHeadHunterFeaturer = featurerService.checkValid(newHeadhunterFeaturerId, headHunterParams, HeadHunter.class);
            dbTwinClassEntity
                    .setHeadHunterFeaturerId(newHeadHunterFeaturer.getId())
                    .setHeadHunterFeaturer(newHeadHunterFeaturer);
        }
        featurerService.prepareForStore(dbTwinClassEntity.getHeadHunterFeaturerId(), headHunterParams);
        if (!MapUtils.areEqual(dbTwinClassEntity.getHeadHunterParams(), headHunterParams)) {
            changesHelper.add(TwinClassEntity.Fields.headHunterParams, dbTwinClassEntity.getHeadHunterParams(), headHunterParams);
            dbTwinClassEntity
                    .setHeadHunterParams(headHunterParams);
        }
    }

    public void updateTwinClassTagDataList(TwinClassEntity dbTwinClassEntity, EntityRelinkOperation tagsRelinkOperation, ChangesHelper changesHelper) throws ServiceException {
        if (tagsRelinkOperation == null || !changesHelper.isChanged(TwinClassEntity.Fields.tagDataListId, dbTwinClassEntity.getTagDataListId(), tagsRelinkOperation.getNewId()))
            return;
        twinTagService.replaceTagsForTwinsOfClass(dbTwinClassEntity, tagsRelinkOperation);
    }

    public void updateTwinClassMarkerDataList(TwinClassEntity dbTwinClassEntity, EntityRelinkOperation updateOperation, ChangesHelper changesHelper) throws ServiceException {
        if (updateOperation == null || !changesHelper.isChanged(TwinClassEntity.Fields.markerDataListId, dbTwinClassEntity.getMarkerDataListId(), updateOperation.getNewId()))
            return;
        twinMarkerService.replaceMarkersForTwinsOfClass(dbTwinClassEntity, updateOperation);
    }

    public void updateTwinClassExtendsTwinClass(TwinClassEntity dbTwinClassEntity, EntityRelinkOperation extendsRelinkOperation, ChangesHelper changesHelper) throws ServiceException {
        if (extendsRelinkOperation == null || !changesHelper.isChanged(TwinClassEntity.Fields.extendsTwinClassId, dbTwinClassEntity.getExtendsTwinClassId(), extendsRelinkOperation.getNewId()))
            return;
        if (UuidUtils.isNullifyMarker(extendsRelinkOperation.getNewId()))
            extendsRelinkOperation.setNewId(authService.getApiUser().getDomain().getAncestorTwinClassId());
        TwinClassEntity newExtendsTwinClass = findEntitySafe(extendsRelinkOperation.getNewId());
        if (newExtendsTwinClass.getExtendedClassIdSet().contains(dbTwinClassEntity.getId()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, dbTwinClassEntity.logNormal() + " can not extend " + newExtendsTwinClass.logNormal() + " because of cycling");
        if (dbTwinClassEntity.getExtendsTwinClassId() == null || !twinRepository.existsByTwinClassId(dbTwinClassEntity.getId())) {
            setNewExtendsTwinClass(dbTwinClassEntity, newExtendsTwinClass);
            return;
        }
        KitGrouped<TwinClassFieldEntity, UUID, UUID> inheritedAndUsedTwinClassFields = twinService.findInheritedTwinClassFields(dbTwinClassEntity, newExtendsTwinClass, true);
        if (KitUtils.isEmpty(inheritedAndUsedTwinClassFields)) {
            //we will lose nothing
            setNewExtendsTwinClass(dbTwinClassEntity, newExtendsTwinClass);
            return;
        }
        if (extendsRelinkOperation.getStrategy() == EntityRelinkOperationStrategy.restrict
                && MapUtils.isEmpty(extendsRelinkOperation.getReplaceMap()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide extendsReplaceMap for classFields: " + StringUtils.join(inheritedAndUsedTwinClassFields.getIdSet()));
        KitGrouped<TwinClassFieldEntity, UUID, UUID> replacementKit = twinClassFieldService.findTwinClassFields(extendsRelinkOperation.getReplaceMap().values());
        Set<TwinClassFieldEntity> twinClassFieldsForDeletion = new HashSet<>();
        for (TwinClassFieldEntity twinClassFieldForReplace : inheritedAndUsedTwinClassFields.getCollection()) {
            UUID replacement = extendsRelinkOperation.getReplaceMap().get(twinClassFieldForReplace.getId());
            if (replacement == null) {
                if (extendsRelinkOperation.getStrategy() == EntityRelinkOperationStrategy.restrict)
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide extendsReplaceMap value for " + twinClassFieldForReplace.logShort());
                else
                    replacement = UuidUtils.NULLIFY_MARKER;
            }
            if (UuidUtils.isNullifyMarker(replacement)) {
                twinClassFieldsForDeletion.add(twinClassFieldForReplace);
                continue;
            }
            TwinClassFieldEntity twinClassFieldReplacement = replacementKit.get(replacement);
            // we need to check if replacement field correct
            if (!twinClassFieldReplacement.getTwinClassId().equals(dbTwinClassEntity.getId()) &&
                    !newExtendsTwinClass.getExtendedClassIdSet().contains(twinClassFieldReplacement.getTwinClassId()))
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, twinClassFieldReplacement.logNormal() + " is incorrect replacement for " + twinClassFieldForReplace.logNormal());

            twinService.convertFieldsForTwinsOfClass(dbTwinClassEntity, twinClassFieldForReplace, twinClassFieldReplacement);
        }
        if (CollectionUtils.isNotEmpty(twinClassFieldsForDeletion))
            twinService.deleteTwinFieldsOfClass(twinClassFieldsForDeletion, dbTwinClassEntity.getId());
        setNewExtendsTwinClass(dbTwinClassEntity, newExtendsTwinClass);
    }

    public TwinClassEntity setNewExtendsTwinClass(TwinClassEntity twinClassEntity, TwinClassEntity newExtendsTwinClass) throws ServiceException {
        twinClassEntity
                .setExtendsTwinClassId(newExtendsTwinClass != null ? newExtendsTwinClass.getId() : null)
                .setExtendsTwinClass(newExtendsTwinClass)
                .setTwinClassFieldKit(null); //invalidating
        refreshExtendsHierarchyTree(twinClassEntity);
        return twinClassEntity;
    }

    // we can refresh tree from code. because db trigger will do this only after transaction commit, but perhaps we will need this field earlier
    public void refreshExtendsHierarchyTree(TwinClassEntity twinClassEntity) throws ServiceException {
        if (twinClassEntity.getExtendsTwinClassId() == null) {
            twinClassEntity.setExtendsHierarchyTree(LTreeUtils.convertToLTreeFormat(twinClassEntity.getId()));
        } else {
            loadExtendsTwinClass(twinClassEntity);
            twinClassEntity.setExtendsHierarchyTree(twinClassEntity.getExtendsTwinClass().getExtendsHierarchyTree() + "." + LTreeUtils.convertToLTreeFormat(twinClassEntity.getId()));
        }
        twinClassEntity.setExtendedClassIdSet(null);
    }

    private TwinClassEntity setNewHeadTwinClass(TwinClassEntity twinClassEntity, TwinClassEntity newHeadTwinClass) throws ServiceException {
        twinClassEntity
                .setHeadTwinClassId(newHeadTwinClass != null ? newHeadTwinClass.getId() : null)
                .setHeadTwinClass(newHeadTwinClass);
        refreshHeadHierarchyTree(twinClassEntity);
        return twinClassEntity;
    }

    // we can refresh tree from code. because db trigger will do this only after transaction commit, but perhaps we will need this field earlier
    public void refreshHeadHierarchyTree(TwinClassEntity twinClassEntity) throws ServiceException {
        if (twinClassEntity.getHeadTwinClassId() == null) {
            twinClassEntity.setHeadHierarchyTree(LTreeUtils.convertToLTreeFormat(twinClassEntity.getId()));
        } else {
            loadHeadTwinClass(twinClassEntity);
            twinClassEntity.setHeadHierarchyTree(twinClassEntity.getHeadTwinClass().getHeadHierarchyTree() + "." + LTreeUtils.convertToLTreeFormat(twinClassEntity.getId()));
        }
        twinClassEntity.setHeadHierarchyClassIdSet(null);
    }


    public void updateTwinClassHeadTwinClass(TwinClassEntity dbTwinClassEntity, EntityRelinkOperation headRelinkOperation, ChangesHelper changesHelper) throws ServiceException {
        if (headRelinkOperation == null || !changesHelper.isChanged(TwinClassEntity.Fields.headTwinClassId, dbTwinClassEntity.getHeadTwinClassId(), headRelinkOperation.getNewId()))
            return;
        TwinClassEntity newHeadTwinClassEntity = UuidUtils.isNullifyMarker(headRelinkOperation.getNewId()) ? null : findEntitySafe(headRelinkOperation.getNewId());
        if (newHeadTwinClassEntity != null && newHeadTwinClassEntity.getHeadHierarchyClassIdSet().contains(dbTwinClassEntity.getId()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, newHeadTwinClassEntity.logNormal() + " can not be set as head of " + dbTwinClassEntity.logNormal() + " because of cycling");
        if (dbTwinClassEntity.getHeadTwinClassId() == null || !twinRepository.existsByTwinClassId(dbTwinClassEntity.getId())) {
            setNewHeadTwinClass(dbTwinClassEntity, newHeadTwinClassEntity);
            return;
        }
        Set<UUID> existedTwinHeadIds = findExistedTwinHeadIdsOfClass(dbTwinClassEntity.getId());
        if (CollectionUtils.isEmpty(existedTwinHeadIds)) {
            setNewHeadTwinClass(dbTwinClassEntity, newHeadTwinClassEntity);
            return;
        }
        if (headRelinkOperation.getStrategy() == EntityRelinkOperationStrategy.restrict
                && MapUtils.isEmpty(headRelinkOperation.getReplaceMap()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "Please provide headReplaceMap for heads: " + StringUtils.join(existedTwinHeadIds));
        Set<UUID> twinsForDeletion = new HashSet<>();
        Set<UUID> newValidTwinHeadIds = twinRepository.findIdByTwinClassIdAndIdIn(headRelinkOperation.getNewId(), headRelinkOperation.getReplaceMap().values());
        for (UUID headForReplace : existedTwinHeadIds) {
            UUID replacement = headRelinkOperation.getReplaceMap().get(headForReplace);
            if (replacement == null) {
                if (headRelinkOperation.getStrategy() == EntityRelinkOperationStrategy.restrict)
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "Please provide headReplaceMap value for head: " + headForReplace);
                else
                    replacement = UuidUtils.NULLIFY_MARKER;
            }
            if (UuidUtils.isNullifyMarker(replacement)) {
                twinsForDeletion.add(headForReplace);
                continue;
            }
            if (!newValidTwinHeadIds.contains(replacement))
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide correct headReplaceMap value for head: " + headForReplace);
            twinRepository.replaceHeadTwinForTwinsOfClass(dbTwinClassEntity.getId(), headForReplace, replacement);
        }
        if (CollectionUtils.isNotEmpty(twinsForDeletion)) {
            //todo support deletion
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "twin auto deletion is currently not implemented. please provide headReplaceMap value for heads: " + StringUtils.join(twinsForDeletion));
        }
        dbTwinClassEntity
                .setHeadTwinClassId(headRelinkOperation.getNewId())
                .setHeadTwinClass(findEntitySafe(headRelinkOperation.getNewId()));
    }

    public void loadMarkerDataList(TwinClassEntity twinClassEntity) throws ServiceException {
        if (twinClassEntity.getMarkerDataList() != null || twinClassEntity.getMarkerDataListId() == null)
            return;
        twinClassEntity.setMarkerDataList(dataListService.findEntitySafe(twinClassEntity.getMarkerDataListId()));
    }

    public void loadMarkerDataList(Collection<TwinClassEntity> twinClassCollection, boolean loadOptions) throws ServiceException {
        KitGrouped<TwinClassEntity, UUID, UUID> needLoad = new KitGrouped<>(TwinClassEntity::getId, TwinClassEntity::getMarkerDataListId);
        for (TwinClassEntity twinClassEntity : twinClassCollection) {
            if (twinClassEntity.getMarkerDataListId() != null && twinClassEntity.getMarkerDataList() == null)
                needLoad.add(twinClassEntity);
        }
        if (KitUtils.isEmpty(needLoad))
            return;
        List<DataListEntity> markers = dataListRepository.findByDomainIdAndIdIn(authService.getApiUser().getDomainId(), needLoad.getGroupedMap().keySet());
        for (DataListEntity dataListEntity : markers) {
            for (TwinClassEntity twinClassEntity : needLoad.getGrouped(dataListEntity.getId())) {
                twinClassEntity.setMarkerDataList(dataListEntity);
            }
        }
        if (loadOptions)
            dataListService.loadDataListOptions(markers);
    }

    private Set<UUID> findExistedTwinHeadIdsOfClass(UUID twinClassId) {
        return twinRepository.findDistinctHeadTwinIdByTwinClassId(twinClassId);
    }

    public boolean isStatusAllowedForTwinClass(UUID twinClassId, UUID twinStatusId) throws ServiceException {
        TwinClassEntity twinClassEntity = findEntitySafe(twinClassId);
        return isStatusAllowedForTwinClass(twinClassEntity, twinStatusId);
    }

    public boolean isStatusAllowedForTwinClass(TwinClassEntity twinClassEntity, UUID twinStatusId) throws ServiceException {
        twinStatusService.loadStatusesForTwinClasses(twinClassEntity);
        return twinClassEntity.getTwinStatusKit().getIdSet().contains(twinStatusId);
    }

    public boolean isOwnerSystemType(TwinClassEntity entity) {
        return entity.getOwnerType().equals(OwnerType.SYSTEM);
    }

    public Set<TwinClassOwnerTypeEntity> findTwinClassOwnerType() throws ServiceException {
        DomainType domainType = authService.getApiUser().getDomain().getDomainType();
        return domainTypeTwinClassOwnerTypeRepository.findAllTwinClassOwnerTypesByDomainTypeId(domainType);
    }

    public void loadHeadHunter(TwinClassEntity twinClassEntity) {
        loadHeadHunter(Collections.singletonList(twinClassEntity));
    }

    public void loadHeadHunter(Collection<TwinClassEntity> collection) {
        featurerService.loadFeaturers(collection,
                TwinClassEntity::getId,
                TwinClassEntity::getHeadHunterFeaturerId,
                TwinClassEntity::getHeadHunterFeaturer,
                TwinClassEntity::setHeadHunterFeaturer);
    }

    public void loadFreeze(TwinClassEntity src) throws ServiceException {
        loadFreeze(Collections.singletonList(src));
    }

    public void loadFreeze(Collection<TwinClassEntity> twinClassCollection) throws ServiceException {
        twinClassFreezeService.load(twinClassCollection,
                TwinClassEntity::getId,
                TwinClassEntity::getTwinClassFreezeId,
                TwinClassEntity::getTwinClassFreeze,
                TwinClassEntity::setTwinClassFreeze);
    }

    public void loadSegments(TwinClassEntity src) {
        loadSegments(Collections.singletonList(src));
    }

    public void loadSegments(Collection<TwinClassEntity> twinClassCollection) {
        Kit<TwinClassEntity, UUID> needLoad = new Kit<>(TwinClassEntity::getId);
        for (TwinClassEntity twinClass : twinClassCollection) {
            if (twinClass.getSegmentTwinsClassKit() != null) {
                continue;
            } else if (Boolean.FALSE.equals(twinClass.getHasSegment())) {
                twinClass.setSegmentTwinsClassKit(Kit.EMPTY);
            } else {
                needLoad.add(twinClass);
            }
        }
        if (KitUtils.isEmpty(needLoad))
            return;
        KitGrouped<TwinClassEntity, UUID, UUID> segments = new KitGrouped<>(twinClassRepository.findByHeadTwinClassIdInAndSegmentTrue(needLoad.getIdSet()), TwinClassEntity::getId, TwinClassEntity::getHeadTwinClassId);
        for (var twinClass : twinClassCollection) {
            if (segments.containsGroupedKey(twinClass.getId())) {
                twinClass.setSegmentTwinsClassKit(new Kit<>(segments.getGrouped(twinClass.getId()), TwinClassEntity::getId));
            } else {
                twinClass.setSegmentTwinsClassKit(Kit.EMPTY);
            }
        }
    }

    //todo replace immutable stored procedure
    public Set<UUID> loadExtendsHierarchyClasses(Map<UUID, Boolean> twinClassIdMap) throws ServiceException {
        if (MapUtils.isEmpty(twinClassIdMap))
            return Collections.emptySet();
        List<UUID> needLoad = new ArrayList<>();
        Set<UUID> ret = new HashSet<>();
        for (var twinClass : twinClassIdMap.entrySet()) {
            if (Boolean.TRUE.equals(twinClass.getValue()))
                needLoad.add(twinClass.getKey());
            else
                ret.add(twinClass.getKey());
        }
        if (CollectionUtils.isEmpty(needLoad))
            return ret;
        List<TwinClassExtendsProjection> twinClassExtendsProjectionList = twinClassRepository.findByDomainIdAndIdIn(authService.getApiUser().getDomainId(), needLoad);
        for (TwinClassExtendsProjection childClass : twinClassExtendsProjectionList) {
            for (String hierarchyItem : convertUuidFromLtreeFormat(childClass.getExtendsHierarchyTree()).split("\\."))
                ret.add(UUID.fromString(hierarchyItem));
        }
        return ret;
    }

    public boolean allExist(Set<UUID> twinClassIds) {
        return twinClassRepository.existsAll(twinClassIds);
    }

    protected TwinClassEntity processIcons(TwinClassEntity twinClassEntity, FileData lightIcon, FileData darkIcon) throws ServiceException {
        var lightIconEntity = saveIconResourceIfExist(lightIcon);
        var darkIconEntity = saveIconResourceIfExist(darkIcon);
        if (lightIconEntity != null) {
            twinClassEntity.setIconLightResourceId(lightIconEntity.getId());
            twinClassEntity.setIconLightResource(lightIconEntity);
        }
        if (darkIconEntity != null) {
            twinClassEntity.setIconDarkResourceId(darkIconEntity.getId());
            twinClassEntity.setIconDarkResource(darkIconEntity);
        }
        return twinClassEntity;
    }


    private ResourceEntity saveIconResourceIfExist(FileData icon) throws ServiceException {
        if (icon != null) {
            return resourceService.addResource(icon.originalFileName(), icon.content());
        } else {
            return null;
        }
    }
}
