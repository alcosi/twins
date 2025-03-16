package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.*;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.dao.I18nType;
import org.cambium.i18n.service.I18nService;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.dao.domain.DomainType;
import org.twins.core.dao.domain.DomainTypeTwinClassOwnerTypeRepository;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.*;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaMapEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.EntityRelinkOperation;
import org.twins.core.domain.twinclass.TwinClassUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.headhunter.HeadHunter;
import org.twins.core.featurer.headhunter.HeadHunterImpl;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.permission.PermissionService;
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

import static org.cambium.common.util.CacheUtils.evictCache;
import static org.twins.core.dao.twinclass.TwinClassEntity.convertUuidFromLtreeFormat;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class TwinClassService extends TwinsEntitySecureFindService<TwinClassEntity> {
    private final TwinRepository twinRepository;
    private final TwinClassRepository twinClassRepository;
    private final TwinClassSchemaRepository twinClassSchemaRepository;
    private final TwinClassSchemaMapRepository twinClassSchemaMapRepository;
    private final TwinClassFieldService twinClassFieldService;
    private final EntitySmartService entitySmartService;
    private final I18nService i18nService;
    private final DataListRepository dataListRepository;
    private final PermissionRepository permissionRepository;
    private final DomainTypeTwinClassOwnerTypeRepository domainTypeTwinClassOwnerTypeRepository;
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
                .setPermissionSchemaSpace(srcTwinClassEntity.isPermissionSchemaSpace())
                .setTwinflowSchemaSpace(srcTwinClassEntity.isTwinflowSchemaSpace())
                .setTwinClassSchemaSpace(srcTwinClassEntity.isTwinClassSchemaSpace())
                .setAliasSpace(srcTwinClassEntity.isAliasSpace())
                .setAbstractt(srcTwinClassEntity.isAbstractt())
                .setExtendsTwinClassId(srcTwinClassEntity.getExtendsTwinClassId())
                .setHeadTwinClassId(srcTwinClassEntity.getHeadTwinClassId())
                .setLogo(srcTwinClassEntity.getLogo())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setDomainId(srcTwinClassEntity.getDomainId())
                .setOwnerType(srcTwinClassEntity.getOwnerType());
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

    public boolean isInstanceOf(TwinClassEntity instanceClass, UUID ofClass) throws ServiceException {
        if (!instanceClass.getId().equals(ofClass)) {
            return instanceClass.getExtendedClassIdSet().contains(ofClass);
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinClassEntity createInDomainClass(TwinClassEntity twinClassEntity, I18nEntity nameI18n, I18nEntity descriptionI18n, Boolean autoCreatePermissions) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        twinClassEntity.setKey(KeyUtils.upperCaseNullFriendly(twinClassEntity.getKey(), ErrorCodeTwins.TWIN_CLASS_KEY_INCORRECT));
        if (twinClassRepository.existsByDomainIdAndKey(apiUser.getDomainId(), twinClassEntity.getKey()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_KEY_ALREADY_IN_USE);

        if (twinClassEntity.getHeadTwinClassId() == null || SystemEntityService.isSystemClass(twinClassEntity.getHeadTwinClassId())) {
            twinClassEntity
                    .setHeadHunterFeaturerId(null)
                    .setHeadHunterParams(null);
        } else {
            if (!twinClassRepository.existsByDomainIdAndId(apiUser.getDomainId(), twinClassEntity.getHeadTwinClassId()))
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN, "unknown head twin class id[" + twinClassEntity.getExtendsTwinClassId() + "]");

            if (twinClassEntity.getHeadHunterFeaturerId() == null) { // we will use default
                twinClassEntity
                        .setHeadHunterFeaturerId(HeadHunterImpl.ID_2601)
                        .setHeadHunterParams(null);
            }
        }
        if (twinClassEntity.getHeadHunterFeaturerId() != null)
            featurerService.checkValid(twinClassEntity.getHeadHunterFeaturerId(), twinClassEntity.getHeadHunterParams(), HeadHunter.class);
        if (twinClassEntity.getExtendsTwinClassId() != null) {
            if (!twinClassRepository.existsByDomainIdAndId(apiUser.getDomainId(), twinClassEntity.getExtendsTwinClassId()))
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN, "unknown extends twin class id[" + twinClassEntity.getExtendsTwinClassId() + "]");
        } else {
            twinClassEntity.setExtendsTwinClassId(apiUser.getDomain().getAncestorTwinClassId());
        }
        twinClassEntity
                .setNameI18NId(i18nService.createI18nAndTranslations(I18nType.TWIN_CLASS_NAME, nameI18n).getId())
                .setDescriptionI18NId(i18nService.createI18nAndTranslations(I18nType.TWIN_CLASS_DESCRIPTION, descriptionI18n).getId())
                .setDomainId(apiUser.getDomainId())
                .setOwnerType(domainService.checkDomainSupportedTwinClassOwnerType(apiUser.getDomain(), twinClassEntity.getOwnerType()))
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(apiUser.getUserId());
        validateEntityAndThrow(twinClassEntity, EntitySmartService.EntityValidateMode.beforeSave);
        twinClassEntity = entitySmartService.save(twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.saveAndThrowOnException);

        if (autoCreatePermissions) {
            Map<PermissionService.DefaultClassPermissionsPrefix, PermissionEntity> newPermissions = permissionService.createDefaultPermissionsForNewInDomainClass(twinClassEntity);
            boolean classPrermissionsChanged = false;
            if (twinClassEntity.getViewPermissionId() == null) {
                twinClassEntity.setViewPermissionId(newPermissions.get(PermissionService.DefaultClassPermissionsPrefix.VIEW).getId());
                twinClassEntity.setViewPermission(newPermissions.get(PermissionService.DefaultClassPermissionsPrefix.VIEW));
                classPrermissionsChanged = true;
            }
            if (twinClassEntity.getEditPermissionId() == null) {
                twinClassEntity.setEditPermissionId(newPermissions.get(PermissionService.DefaultClassPermissionsPrefix.EDIT).getId());
                twinClassEntity.setEditPermission(newPermissions.get(PermissionService.DefaultClassPermissionsPrefix.EDIT));
                classPrermissionsChanged = true;
            }
            if (twinClassEntity.getCreatePermissionId() == null) {
                twinClassEntity.setCreatePermissionId(newPermissions.get(PermissionService.DefaultClassPermissionsPrefix.CREATE).getId());
                twinClassEntity.setCreatePermission(newPermissions.get(PermissionService.DefaultClassPermissionsPrefix.CREATE));
                classPrermissionsChanged = true;
            }
            if (twinClassEntity.getDeletePermissionId() == null) {
                twinClassEntity.setDeletePermissionId(newPermissions.get(PermissionService.DefaultClassPermissionsPrefix.DELETE).getId());
                twinClassEntity.setDeletePermission(newPermissions.get(PermissionService.DefaultClassPermissionsPrefix.DELETE));
                classPrermissionsChanged = true;
            }
            if(classPrermissionsChanged)
                twinClassEntity = entitySmartService.save(twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        }

        refreshExtendsHierarchyTree(twinClassEntity);
        refreshHeadHierarchyTree(twinClassEntity);
        TwinStatusEntity twinStatusEntity = twinStatusService.createStatus(twinClassEntity, "init", "Initial status");
        TwinflowEntity twinflowEntity = twinflowService.createTwinflow(twinClassEntity, twinStatusEntity);
        TwinflowSchemaMapEntity twinflowSchemaMapEntity = twinflowService.registerTwinflow(twinflowEntity, apiUser.getDomain(), twinClassEntity);
        return twinClassEntity;
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
    public void updateTwinClass(TwinClassUpdate twinClassUpdate) throws ServiceException {
        TwinClassEntity dbTwinClassEntity = twinClassUpdate.getDbTwinClassEntity();
        if (dbTwinClassEntity.getOwnerType().isSystemLevel())
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "system class can be edited");
        ChangesHelper changesHelper = new ChangesHelper();
        updateTwinClassName(dbTwinClassEntity, twinClassUpdate.getNameI18n(), changesHelper);
        updateTwinClassDescription(dbTwinClassEntity, twinClassUpdate.getDescriptionI18n(), changesHelper);
        updateTwinClassHeadTwinClass(dbTwinClassEntity, twinClassUpdate.getHeadTwinClassUpdate(), changesHelper);
        updateTwinClassHeadHunterFeaturer(dbTwinClassEntity, twinClassUpdate.getHeadHunterFeaturerId(), twinClassUpdate.getHeadHunterParams(), changesHelper);
        updateTwinClassExtendsTwinClass(dbTwinClassEntity, twinClassUpdate.getExtendsTwinClassUpdate(), changesHelper);
        updateTwinClassAbstractFlag(dbTwinClassEntity, twinClassUpdate.getAbstractt(), changesHelper);
        updateTwinClassTwinClassSchemaSpaceFlag(dbTwinClassEntity, twinClassUpdate.getTwinClassSchemaSpace(), changesHelper);
        updateTwinClassTwinflowSchemaSpaceFlag(dbTwinClassEntity, twinClassUpdate.getTwinflowSchemaSpace(), changesHelper);
        updateTwinClassAliasSpaceFlag(dbTwinClassEntity, twinClassUpdate.getAliasSpace(), changesHelper);
        updateTwinClassPermissionSchemaSpaceFlag(dbTwinClassEntity, twinClassUpdate.getPermissionSchemaSpace(), changesHelper);
        updateTwinClassViewPermission(dbTwinClassEntity, twinClassUpdate.getViewPermissionId(), changesHelper);
        updateTwinClassEditPermission(dbTwinClassEntity, twinClassUpdate.getEditPermissionId(), changesHelper);
        updateTwinClassCreatePermission(dbTwinClassEntity, twinClassUpdate.getCreatePermissionId(), changesHelper);
        updateTwinClassDeletePermission(dbTwinClassEntity, twinClassUpdate.getDeletePermissionId(), changesHelper);
        updateTwinClassKey(dbTwinClassEntity, twinClassUpdate.getKey(), changesHelper);
        updateTwinClassLogo(dbTwinClassEntity, twinClassUpdate.getLogo(), changesHelper);
        updateTwinClassMarkerDataList(dbTwinClassEntity, twinClassUpdate.getMarkerDataListUpdate(), changesHelper);
        updateTwinClassTagDataList(dbTwinClassEntity, twinClassUpdate.getTagDataListUpdate(), changesHelper);

        updateSafe(dbTwinClassEntity, changesHelper);
        if (changesHelper.hasChanges()) {
            Map<String, List<Object>> cacheEntries = Map.of(
                    TwinClassRepository.CACHE_TWIN_CLASS_BY_ID, List.of(twinClassUpdate.getDbTwinClassEntity().getId()),
                    ENTITY_CACHE, Collections.emptyList()
            );
            evictCache(cacheManager, cacheEntries);
        }
    }

    public void updateTwinClassDescription(TwinClassEntity dbTwinClassEntity, I18nEntity descriptionI18n, ChangesHelper changesHelper) throws ServiceException {
        if (descriptionI18n == null)
            return;
        if (dbTwinClassEntity.getDescriptionI18NId() != null)
            descriptionI18n.setId(dbTwinClassEntity.getDescriptionI18NId());
        i18nService.saveTranslations(I18nType.TWIN_CLASS_DESCRIPTION, descriptionI18n);
        if (changesHelper.isChanged(TwinClassEntity.Fields.descriptionI18NId, dbTwinClassEntity.getDescriptionI18NId(), descriptionI18n.getId()))
            dbTwinClassEntity.setDescriptionI18NId(descriptionI18n.getId());
    }


    public void updateTwinClassName(TwinClassEntity dbTwinClassEntity, I18nEntity nameI18n, ChangesHelper changesHelper) throws ServiceException {
        if (nameI18n == null)
            return;
        if (dbTwinClassEntity.getNameI18NId() != null)
            nameI18n.setId(dbTwinClassEntity.getNameI18NId());
        i18nService.saveTranslations(I18nType.TWIN_CLASS_NAME, nameI18n);
        if (changesHelper.isChanged(TwinClassEntity.Fields.nameI18NId, dbTwinClassEntity.getNameI18NId(), nameI18n.getId()))
            dbTwinClassEntity.setNameI18NId(nameI18n.getId());
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

    public void updateTwinClassLogo(TwinClassEntity dbTwinClassEntity, String newLogo, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinClassEntity.Fields.logo, dbTwinClassEntity.getLogo(), newLogo))
            return;
        dbTwinClassEntity.setLogo(newLogo);
    }

    public void updateTwinClassKey(TwinClassEntity dbTwinClassEntity, String newKey, ChangesHelper changesHelper) throws ServiceException {
        newKey = KeyUtils.upperCaseNullFriendly(newKey, ErrorCodeTwins.TWIN_CLASS_KEY_INCORRECT);
        if (!changesHelper.isChanged(TwinClassEntity.Fields.key, dbTwinClassEntity.getKey(), newKey))
            return;
        if (twinClassRepository.existsByDomainIdAndKey(authService.getApiUser().getDomainId(), newKey))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "class key is already exist");
        if (twinRepository.existsByTwinClassId(dbTwinClassEntity.getId()))
            //todo generate new aliases for all existed twins. old class twin aliases should not be deleted, until we will detect conflicts
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "class key change is not implemented fully ");
        dbTwinClassEntity
                .setKey(newKey);
    }

    public void updateTwinClassViewPermission(TwinClassEntity dbTwinClassEntity, UUID newViewPermissionId, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinClassEntity.Fields.viewPermissionId, dbTwinClassEntity.getViewPermissionId(), newViewPermissionId))
            return;
        dbTwinClassEntity
                .setViewPermissionId(UuidUtils.nullifyIfNecessary(newViewPermissionId));
    }

    public void updateTwinClassEditPermission(TwinClassEntity dbTwinClassEntity, UUID newEditPermissionId, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinClassEntity.Fields.editPermissionId, dbTwinClassEntity.getEditPermissionId(), newEditPermissionId))
            return;
        dbTwinClassEntity
                .setEditPermissionId(UuidUtils.nullifyIfNecessary(newEditPermissionId));
    }

    public void updateTwinClassCreatePermission(TwinClassEntity dbTwinClassEntity, UUID newCreatePermissionId, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinClassEntity.Fields.createPermissionId, dbTwinClassEntity.getCreatePermissionId(), newCreatePermissionId))
            return;
        dbTwinClassEntity
                .setCreatePermissionId(UuidUtils.nullifyIfNecessary(newCreatePermissionId));
    }

    public void updateTwinClassDeletePermission(TwinClassEntity dbTwinClassEntity, UUID newDeletePermissionId, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinClassEntity.Fields.deletePermissionId, dbTwinClassEntity.getDeletePermissionId(), newDeletePermissionId))
            return;
        dbTwinClassEntity
                .setDeletePermissionId(UuidUtils.nullifyIfNecessary(newDeletePermissionId));
    }

    public void updateTwinClassPermissionSchemaSpaceFlag(TwinClassEntity dbTwinClassEntity, Boolean newTwinClassPermissionSchemaSpaceFlag, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinClassEntity.Fields.permissionSchemaSpace, dbTwinClassEntity.isPermissionSchemaSpace(), newTwinClassPermissionSchemaSpaceFlag))
            return;
        //we have db trigger which will update twin.twinflow_schema_space_id column for twins of given class
        dbTwinClassEntity
                .setPermissionSchemaSpace(newTwinClassPermissionSchemaSpaceFlag);
    }

    public void updateTwinClassAliasSpaceFlag(TwinClassEntity dbTwinClassEntity, Boolean newTwinClassAliasSpaceFlag, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinClassEntity.Fields.aliasSpace, dbTwinClassEntity.isAliasSpace(), newTwinClassAliasSpaceFlag))
            return;
        //we have db trigger which will update twin.alias_space_id column for twins of given class
        dbTwinClassEntity
                .setAliasSpace(newTwinClassAliasSpaceFlag);
    }

    public void updateTwinClassTwinflowSchemaSpaceFlag(TwinClassEntity dbTwinClassEntity, Boolean newTwinClassTwinflowSchemaSpaceFlag, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinClassEntity.Fields.twinflowSchemaSpace, dbTwinClassEntity.isTwinflowSchemaSpace(), newTwinClassTwinflowSchemaSpaceFlag))
            return;
        //we have db trigger which will update twin.twinflow_schema_space_id column for twins of given class
        dbTwinClassEntity
                .setTwinflowSchemaSpace(newTwinClassTwinflowSchemaSpaceFlag);
    }

    public void updateTwinClassTwinClassSchemaSpaceFlag(TwinClassEntity dbTwinClassEntity, Boolean newTwinClassSchemaSpaceFlag, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinClassEntity.Fields.twinClassSchemaSpace, dbTwinClassEntity.isTwinClassSchemaSpace(), newTwinClassSchemaSpaceFlag))
            return;
        //we have db trigger which will update twin.twin_class_schema_space_id column for twins of given class
        dbTwinClassEntity
                .setTwinClassSchemaSpace(newTwinClassSchemaSpaceFlag);
    }

    public void updateTwinClassAbstractFlag(TwinClassEntity dbTwinClassEntity, Boolean newAbstractFlag, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged(TwinClassEntity.Fields.abstractt, dbTwinClassEntity.isAbstractt(), newAbstractFlag))
            return;
        if (newAbstractFlag && twinRepository.existsByTwinClassId(dbTwinClassEntity.getId()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "class can not be marked abstract, because some twins are already exist");
        dbTwinClassEntity
                .setAbstractt(newAbstractFlag);
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
        if (extendsRelinkOperation.getStrategy() == EntityRelinkOperation.Strategy.restrict
                && MapUtils.isEmpty(extendsRelinkOperation.getReplaceMap()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide extendsReplaceMap for classFields: " + StringUtils.join(inheritedAndUsedTwinClassFields.getIdSet()));
        KitGrouped<TwinClassFieldEntity, UUID, UUID> replacementKit = twinClassFieldService.findTwinClassFields(extendsRelinkOperation.getReplaceMap().values());
        Set<TwinClassFieldEntity> twinClassFieldsForDeletion = new HashSet<>();
        for (TwinClassFieldEntity twinClassFieldForReplace : inheritedAndUsedTwinClassFields.getCollection()) {
            UUID replacement = extendsRelinkOperation.getReplaceMap().get(twinClassFieldForReplace.getId());
            if (replacement == null) {
                if (extendsRelinkOperation.getStrategy() == EntityRelinkOperation.Strategy.restrict)
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


    public void updateTwinClassHeadHunterFeaturer(TwinClassEntity dbTwinClassEntity, Integer newHeadhunterFeaturerId, HashMap<String, String> headHunterParams, ChangesHelper changesHelper) throws ServiceException {
        if (changesHelper.isChanged(TwinClassEntity.Fields.headHunterFeaturerId, dbTwinClassEntity.getHeadHunterFeaturerId(), newHeadhunterFeaturerId)) {
            FeaturerEntity newHeadHunterFeaturer = featurerService.checkValid(newHeadhunterFeaturerId, headHunterParams, HeadHunter.class);
            dbTwinClassEntity
                    .setHeadHunterFeaturerId(newHeadHunterFeaturer.getId())
                    .setHeadHunterFeaturer(newHeadHunterFeaturer);
        }
        if (!MapUtils.areEqual(dbTwinClassEntity.getHeadHunterParams(), headHunterParams)) {
            changesHelper.add(TwinClassEntity.Fields.headHunterParams, dbTwinClassEntity.getHeadHunterParams(), headHunterParams);
            dbTwinClassEntity
                    .setHeadHunterParams(headHunterParams);
        }
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
        if (headRelinkOperation.getStrategy() == EntityRelinkOperation.Strategy.restrict
                && MapUtils.isEmpty(headRelinkOperation.getReplaceMap()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "Please provide headReplaceMap for heads: " + StringUtils.join(existedTwinHeadIds));
        Set<UUID> twinsForDeletion = new HashSet<>();
        Set<UUID> newValidTwinHeadIds = twinRepository.findIdByTwinClassIdAndIdIn(headRelinkOperation.getNewId(), headRelinkOperation.getReplaceMap().values());
        for (UUID headForReplace : existedTwinHeadIds) {
            UUID replacement = headRelinkOperation.getReplaceMap().get(headForReplace);
            if (replacement == null) {
                if (headRelinkOperation.getStrategy() == EntityRelinkOperation.Strategy.restrict)
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
        return entity.getOwnerType().equals(TwinClassEntity.OwnerType.SYSTEM);
    }

    public Set<TwinClassOwnerTypeEntity> findTwinClassOwnerType() throws ServiceException {
        DomainType domainType = authService.getApiUser().getDomain().getDomainType();
        return domainTypeTwinClassOwnerTypeRepository.findAllTwinClassOwnerTypesByDomainTypeId(domainType);
    }

    public void loadHeadHunter(TwinClassEntity twinClassEntity) {
        loadHeadHunter(Collections.singletonList(twinClassEntity));
    }

    public void loadHeadHunter(Collection<TwinClassEntity> twinClassCollection) {
        Map<Integer, TwinClassEntity> needLoad = new HashMap<>();
        for (TwinClassEntity twinClass : twinClassCollection) {
            if (twinClass.getHeadHunterFeaturer() == null && twinClass.getHeadHunterFeaturerId() != null)
                needLoad.put(twinClass.getHeadHunterFeaturerId(), twinClass);
        }
        if (MapUtils.isEmpty(needLoad))
            return;
        List<FeaturerEntity> featurerList = featurerService.findByIdIn(needLoad.keySet());
        for (Map.Entry<Integer, TwinClassEntity> map : needLoad.entrySet()) {
            map.getValue().setHeadHunterFeaturer(featurerList.get(map.getKey()));
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
}
