package org.twins.core.service.twinclass;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.*;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.dao.I18nType;
import org.cambium.i18n.service.I18nService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListRepository;
import org.twins.core.dao.permission.PermissionRepository;
import org.twins.core.dao.specifications.twin_class.TwinClassSpecification;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.*;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaMapEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.EntityRelinkOperation;
import org.twins.core.domain.TwinClassUpdate;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.twinclass.HeadHunter;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.twin.TwinMarkerService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twin.TwinTagService;
import org.twins.core.service.twinflow.TwinflowService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;
import static org.twins.core.dao.specifications.twin_class.TwinClassSpecification.*;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class TwinClassService extends EntitySecureFindServiceImpl<TwinClassEntity> {
    final TwinRepository twinRepository;
    final TwinClassRepository twinClassRepository;
    final TwinClassSchemaRepository twinClassSchemaRepository;
    final TwinClassFieldService twinClassFieldService;
    final EntitySmartService entitySmartService;
    final I18nService i18nService;
    final EntityManager entityManager;
    final DataListRepository dataListRepository;
    final PermissionRepository permissionRepository;
    @Lazy
    final TwinStatusService twinStatusService;
    @Lazy
    final TwinflowService twinflowService;
    @Lazy
    final DomainService domainService;
    @Lazy
    final AuthService authService;
    @Lazy
    final FeaturerService featurerService;
    @Lazy
    final TwinMarkerService twinMarkerService;
    @Lazy
    final TwinTagService twinTagService;
    @Lazy
    final DataListService dataListService;
    @Lazy
    final TwinService twinService;
    @Autowired
    private CacheManager cacheManager;

    @Override
    public CrudRepository<TwinClassEntity, UUID> entityRepository() {
        return twinClassRepository;
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
        return true;
    }

    public TwinClassResult findTwinClasses(TwinClassSearch twinClassSearch, int offset, int limit) throws ServiceException {
        Pageable pageable = PaginationUtils.paginationOffset(offset, limit, Sort.unsorted());
        if (twinClassSearch == null)
            twinClassSearch = new TwinClassSearch(); //no filters
        Page<TwinClassEntity> twinClassList = twinClassRepository.findAll(createTwinClassEntitySearchSpecification(twinClassSearch), pageable);
        return convertPageSearchResult(twinClassList, offset, limit);
    }

    public Specification<TwinClassEntity> createTwinClassEntitySearchSpecification(TwinClassSearch twinClassSearch) {
        return where(
                checkUuidIn(TwinClassEntity.Fields.id, twinClassSearch.getTwinClassIdList(), false)
                        .and(checkFieldLikeIn(TwinClassEntity.Fields.key, twinClassSearch.getTwinClassKeyLikeList(), true))
                        .and(checkUuidIn(TwinClassEntity.Fields.headTwinClassId, twinClassSearch.getHeadTwinClassIdList(), false))
                        .and(checkUuidIn(TwinClassEntity.Fields.extendsTwinClassId, twinClassSearch.getExtendsTwinClassIdList(), false))
                        .and(hasOwnerType(twinClassSearch.getOwnerType()))
                        .and(checkTernary(TwinClassEntity.Fields.abstractt, twinClassSearch.getAbstractt()))
                        .and(checkTernary(TwinClassEntity.Fields.permissionSchemaSpace, twinClassSearch.getPermissionSchemaSpace()))
                        .and(checkTernary(TwinClassEntity.Fields.twinflowSchemaSpace, twinClassSearch.getTwinflowSchemaSpace()))
                        .and(checkTernary(TwinClassEntity.Fields.twinClassSchemaSpace, twinClassSearch.getTwinClassSchemaSpace()))
                        .and(checkTernary(TwinClassEntity.Fields.aliasSpace, twinClassSearch.getAliasSpace()))
        );
    }

    private TwinClassResult convertPageSearchResult(Page<TwinClassEntity> twinClassList, int offset, int limit) {//todo change impl pagination
        return (TwinClassResult) new TwinClassResult()
                .setTwinClassList(twinClassList.toList())
                .setTotal(twinClassList.getTotalElements())
                .setOffset(offset)
                .setLimit(limit);
    }

    public TwinClassEntity findTwinClassByKey(ApiUser apiUser, String twinClassKey) throws ServiceException {
        return twinClassRepository.findByDomainIdAndKey(apiUser.getDomain().getId(), twinClassKey);
    }

    public UUID checkTwinClassSchemaAllowed(UUID domainId, UUID twinClassSchemaId) throws ServiceException {
        Optional<TwinClassSchemaEntity> twinClassSchemaEntity = twinClassSchemaRepository.findById(twinClassSchemaId);
        if (twinClassSchemaEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown twinClassSchemaId[" + twinClassSchemaId + "]");
        if (twinClassSchemaEntity.get().getDomainId() != domainId)
            throw new ServiceException(ErrorCodeTwins.PERMISSION_SCHEMA_NOT_ALLOWED, "twinClassSchemaId[" + twinClassSchemaId + "] is not allows in domain[" + domainId + "]");
        return twinClassSchemaId;
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

    public Set<UUID> findExtendedClasses(UUID twinClassId, boolean includeSelf) throws ServiceException {
        return findExtendedClasses(findEntitySafe(twinClassId), includeSelf);
    }

    public Set<UUID> findExtendedClasses(TwinClassEntity twinClassEntity, boolean includeSelf) {
        Set<UUID> ret = new LinkedHashSet<>();
        if (includeSelf)
            ret.add(twinClassEntity.getId());
        if (twinClassEntity.getExtendsTwinClassId() == null)
            return ret;
        UUID extendedTwinClassId = twinClassEntity.getExtendsTwinClassId();
        ret.add(extendedTwinClassId);
        for (int i = 0; i <= 10; i++) {
            extendedTwinClassId = twinClassRepository.findExtendedClassId(extendedTwinClassId);
            if (extendedTwinClassId == null)
                break;
            if (ret.contains(extendedTwinClassId)) {
                log.warn(twinClassEntity.logShort() + " inheritance recursion");
                break;
            }
            ret.add(extendedTwinClassId);
        }
        return ret;
    }

    public Set<UUID> loadChildClasses(TwinClassEntity twinClassEntity) {
        if (twinClassEntity.getChildClassIdSet() != null)
            return twinClassEntity.getChildClassIdSet();

        Set<UUID> childClassIdSet = twinClassRepository.findAll(
                        TwinClassSpecification.checkHierarchyIsChild(TwinClassEntity.Fields.extendsHierarchyTree, twinClassEntity.getId()))
                .stream().map(TwinClassEntity::getId).collect(Collectors.toSet());
        twinClassEntity.setChildClassIdSet(childClassIdSet);
        return childClassIdSet;
    }

    public boolean isInstanceOf(UUID instanceClassId, UUID ofClass) throws ServiceException {
        Set<UUID> parentClasses;
        if (!instanceClassId.equals(ofClass)) {
            parentClasses = findExtendedClasses(instanceClassId, true);
            return parentClasses.contains(ofClass);
        }
        return true;
    }

    public boolean isInstanceOf(TwinClassEntity instanceClass, UUID ofClass) throws ServiceException {
        Set<UUID> parentClasses;
        if (!instanceClass.getId().equals(ofClass)) {
            return instanceClass.getExtendedClassIdSet().contains(ofClass);
        }
        return true;
    }

    @Transactional
    public TwinClassEntity createInDomainClassTransactional(TwinClassEntity twinClassEntity, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (StringUtils.isBlank(twinClassEntity.getKey()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_KEY_INCORRECT);
        twinClassEntity.setKey(twinClassEntity.getKey().trim().toUpperCase().replaceAll("\\s", "_"));
        if (twinClassRepository.existsByDomainIdAndKey(apiUser.getDomainId(), twinClassEntity.getKey())) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_KEY_ALREADY_IN_USE);
        }
        if (twinClassEntity.getHeadTwinClassId() != null
                && !twinClassRepository.existsByDomainIdAndId(apiUser.getDomainId(), twinClassEntity.getHeadTwinClassId()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN, "unknown head twin class id");
        if (twinClassEntity.getHeadHunterFeaturerId() != null)
            featurerService.checkValid(twinClassEntity.getHeadHunterFeaturerId(), twinClassEntity.getHeadHunterParams(), HeadHunter.class);
        else
            twinClassEntity.setHeadHunterParams(null);
        if (twinClassEntity.getExtendsTwinClassId() != null) {
            if (!twinClassRepository.existsByDomainIdAndId(apiUser.getDomainId(), twinClassEntity.getExtendsTwinClassId()))
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN, "unknown extends twin class id");
        } else {
            twinClassEntity.setExtendsTwinClassId(apiUser.getDomain().getAncestorTwinClassId());
        }
        if (twinClassEntity.getMarkerDataListId() != null
                && dataListRepository.existsByDomainIdAndId(apiUser.getDomainId(), twinClassEntity.getMarkerDataListId()))
            throw new ServiceException(ErrorCodeTwins.DATALIST_LIST_UNKNOWN, "unknown marker data list id");
        if (twinClassEntity.getTagDataListId() != null
                && dataListRepository.existsByDomainIdAndId(apiUser.getDomainId(), twinClassEntity.getTagDataListId()))
            throw new ServiceException(ErrorCodeTwins.DATALIST_LIST_UNKNOWN, "unknown tag data list id");
        if (twinClassEntity.getViewPermissionId() != null
                && permissionRepository.existsByIdAndPermissionGroup_DomainId(twinClassEntity.getViewPermissionId(), apiUser.getDomainId()))
            throw new ServiceException(ErrorCodeTwins.PERMISSION_ID_UNKNOWN, "unknown view permission id");
        if (nameI18n == null)
            nameI18n = new I18nEntity().setType(I18nType.TWIN_CLASS_NAME);
        if (descriptionI18n == null)
            descriptionI18n = new I18nEntity().setType(I18nType.TWIN_CLASS_DESCRIPTION);
        twinClassEntity
                .setKey(twinClassEntity.getKey().toUpperCase())
                .setNameI18NId(i18nService.createI18nAndTranslations(nameI18n).getId())
                .setDescriptionI18NId(i18nService.createI18nAndTranslations(descriptionI18n).getId())
                .setDomainId(apiUser.getDomainId())
                .setOwnerType(domainService.checkDomainSupportedTwinClassOwnerType(apiUser.getDomain(), twinClassEntity.getOwnerType()))
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(apiUser.getUserId());
        twinClassEntity = entitySmartService.save(twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        TwinStatusEntity twinStatusEntity = twinStatusService.createStatus(twinClassEntity, "init", "Initial status");
        TwinflowEntity twinflowEntity = twinflowService.createTwinflow(twinClassEntity, twinStatusEntity);
        TwinflowSchemaMapEntity twinflowSchemaMapEntity = twinflowService.registerTwinflow(twinflowEntity, apiUser.getDomain(), twinClassEntity);
        return twinClassEntity;
    }

    public TwinClassEntity createInDomainClass(TwinClassEntity twinClassEntity, I18nEntity name, I18nEntity description) throws ServiceException {
        twinClassEntity = createInDomainClassTransactional(twinClassEntity, name, description);
        if (StringUtils.isBlank(twinClassEntity.getExtendsHierarchyTree())) // this field is filled by trigger only after transaction commit. So we have to reload entity from database
            twinClassEntity.setExtendsHierarchyTree(twinClassRepository.getExtendsHierarchyTree(twinClassEntity.getId()));
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
        updateTwinClassKey(dbTwinClassEntity, twinClassUpdate.getKey(), changesHelper);
        updateTwinClassLogo(dbTwinClassEntity, twinClassUpdate.getLogo(), changesHelper);
        updateTwinClassMarkerDataList(dbTwinClassEntity, twinClassUpdate.getMarkerDataListUpdate(), changesHelper);
        updateTwinClassTagDataList(dbTwinClassEntity, twinClassUpdate.getTagDataListUpdate(), changesHelper);
        twinClassRepository.save(twinClassUpdate.getDbTwinClassEntity());
        evictCache(twinClassUpdate.getDbTwinClassEntity().getId());
    }

    @Transactional
    public void updateTwinClassDescription(TwinClassEntity dbTwinClassEntity, I18nEntity descriptionI18n, ChangesHelper changesHelper) throws ServiceException {
        if (descriptionI18n == null)
            return;
        if (dbTwinClassEntity.getDescriptionI18NId() != null)
            descriptionI18n.setId(dbTwinClassEntity.getDescriptionI18NId());
        else
            descriptionI18n.setType(I18nType.TWIN_CLASS_DESCRIPTION);
        i18nService.saveTranslations(descriptionI18n);
        dbTwinClassEntity.setDescriptionI18NId(descriptionI18n.getId());
    }


    public void updateTwinClassName(TwinClassEntity dbTwinClassEntity, I18nEntity nameI18n, ChangesHelper changesHelper) throws ServiceException {
        if (nameI18n == null)
            return;
        if (dbTwinClassEntity.getNameI18NId() != null)
            nameI18n.setId(dbTwinClassEntity.getNameI18NId());
        else
            nameI18n.setType(I18nType.TWIN_CLASS_NAME);
        i18nService.saveTranslations(nameI18n);
        dbTwinClassEntity.setNameI18NId(nameI18n.getId());
    }

    @Transactional
    public void updateTwinClassTagDataList(TwinClassEntity dbTwinClassEntity, EntityRelinkOperation tagsRelinkOperation, ChangesHelper changesHelper) throws ServiceException {
        if (tagsRelinkOperation == null || !changesHelper.isChanged("tagsDataListId", dbTwinClassEntity.getTagDataListId(), tagsRelinkOperation.getNewId()))
            return;
        twinTagService.replaceTagsForTwinsOfClass(dbTwinClassEntity, tagsRelinkOperation);
    }

    @Transactional
    public void updateTwinClassMarkerDataList(TwinClassEntity dbTwinClassEntity, EntityRelinkOperation updateOperation, ChangesHelper changesHelper) throws ServiceException {
        if (updateOperation == null || !changesHelper.isChanged("markerDataListId", dbTwinClassEntity.getMarkerDataListId(), updateOperation.getNewId()))
            return;
        twinMarkerService.replaceMarkersForTwinsOfClass(dbTwinClassEntity, updateOperation);
    }

    @Transactional
    public void updateTwinClassLogo(TwinClassEntity dbTwinClassEntity, String newLogo, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("logo", dbTwinClassEntity.getLogo(), newLogo))
            return;
        dbTwinClassEntity.setLogo(newLogo);
    }

    @Transactional
    public void updateTwinClassKey(TwinClassEntity dbTwinClassEntity, String newKey, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("key", dbTwinClassEntity.getKey(), newKey))
            return;
        if (twinClassRepository.existsByDomainIdAndKey(authService.getApiUser().getDomainId(), newKey))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "class key is already exist");
        if (twinRepository.existsByTwinClassId(dbTwinClassEntity.getId()))
            //todo generate new aliases for all existed twins. old class twin aliases should not be deleted, until we will detect conflicts
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "class key change is not implemented fully ");
        dbTwinClassEntity
                .setKey(newKey);
    }

    @Transactional
    public void updateTwinClassViewPermission(TwinClassEntity dbTwinClassEntity, UUID newViewPermissionId, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("viewPermissionId", dbTwinClassEntity.getViewPermissionId(), newViewPermissionId))
            return;
        dbTwinClassEntity
                .setViewPermissionId(UuidUtils.nullifyIfNecessary(newViewPermissionId));
    }

    @Transactional
    public void updateTwinClassPermissionSchemaSpaceFlag(TwinClassEntity dbTwinClassEntity, Boolean newTwinClassPermissionSchemaSpaceFlag, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("isPermissionSchemaSpace", dbTwinClassEntity.isPermissionSchemaSpace(), newTwinClassPermissionSchemaSpaceFlag))
            return;
        //we have db trigger which will update twin.twinflow_schema_space_id column for twins of given class
        dbTwinClassEntity
                .setPermissionSchemaSpace(newTwinClassPermissionSchemaSpaceFlag);
    }

    @Transactional
    public void updateTwinClassAliasSpaceFlag(TwinClassEntity dbTwinClassEntity, Boolean newTwinClassAliasSpaceFlag, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("isAliasSpace", dbTwinClassEntity.isAliasSpace(), newTwinClassAliasSpaceFlag))
            return;
        //we have db trigger which will update twin.alias_space_id column for twins of given class
        dbTwinClassEntity
                .setAliasSpace(newTwinClassAliasSpaceFlag);
    }

    @Transactional
    public void updateTwinClassTwinflowSchemaSpaceFlag(TwinClassEntity dbTwinClassEntity, Boolean newTwinClassTwinflowSchemaSpaceFlag, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("isTwinflowSchemaSpace", dbTwinClassEntity.isTwinflowSchemaSpace(), newTwinClassTwinflowSchemaSpaceFlag))
            return;
        //we have db trigger which will update twin.twinflow_schema_space_id column for twins of given class
        dbTwinClassEntity
                .setTwinflowSchemaSpace(newTwinClassTwinflowSchemaSpaceFlag);
    }

    @Transactional
    public void updateTwinClassTwinClassSchemaSpaceFlag(TwinClassEntity dbTwinClassEntity, Boolean newTwinClassSchemaSpaceFlag, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("isTwinClassSchemaSpace", dbTwinClassEntity.isTwinClassSchemaSpace(), newTwinClassSchemaSpaceFlag))
            return;
        //we have db trigger which will update twin.twin_class_schema_space_id column for twins of given class
        dbTwinClassEntity
                .setTwinClassSchemaSpace(newTwinClassSchemaSpaceFlag);
    }

    @Transactional
    public void updateTwinClassAbstractFlag(TwinClassEntity dbTwinClassEntity, Boolean newAbstractFlag, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("isAbstract", dbTwinClassEntity.isAbstractt(), newAbstractFlag))
            return;
        if (newAbstractFlag && twinRepository.existsByTwinClassId(dbTwinClassEntity.getId()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "class can not be marked abstract, because some twins are already exist");
        dbTwinClassEntity
                .setAbstractt(newAbstractFlag);
    }

    @Transactional
    public void updateTwinClassExtendsTwinClass(TwinClassEntity dbTwinClassEntity, EntityRelinkOperation extendsRelinkOperation, ChangesHelper changesHelper) throws ServiceException {
        if (extendsRelinkOperation == null || !changesHelper.isChanged("extendsTwinClassId", dbTwinClassEntity.getExtendsTwinClassId(), extendsRelinkOperation.getNewId()))
            return;
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

    private TwinClassEntity setNewExtendsTwinClass(TwinClassEntity twinClassEntity, TwinClassEntity newExtendsTwinClass) {
        if (twinClassEntity.getExtendsTwinClassId() != null)
            twinClassEntity
                    .setExtendsHierarchyTree(
                            newExtendsTwinClass.getExtendsHierarchyTree() + StringUtils.substringAfter(twinClassEntity.getExtendsHierarchyTree(), TwinClassEntity.convertUuidToLtreeFormat(twinClassEntity.getExtendsTwinClassId())))
                    .setExtendedClassIdSet(null);
        twinClassEntity
                .setExtendsTwinClassId(newExtendsTwinClass.getId())
                .setExtendsTwinClass(newExtendsTwinClass)
                .setTwinClassFieldKit(null); //invalidating
        return twinClassEntity;
    }

    @Transactional
    public void updateTwinClassHeadHunterFeaturer(TwinClassEntity dbTwinClassEntity, Integer newHeadhunterFeaturerId, HashMap<String, String> headHunterParams, ChangesHelper changesHelper) throws ServiceException {
        if (changesHelper.isChanged("headHunterFeaturerId", dbTwinClassEntity.getHeadHunterFeaturerId(), newHeadhunterFeaturerId)) {
            FeaturerEntity newHeadHunterFeaturer = featurerService.checkValid(newHeadhunterFeaturerId, headHunterParams, HeadHunter.class);
            dbTwinClassEntity
                    .setHeadHunterFeaturerId(newHeadHunterFeaturer.getId())
                    .setHeadHunterFeaturer(newHeadHunterFeaturer);
        }
        if (!MapUtils.areEqual(dbTwinClassEntity.getHeadHunterParams(), headHunterParams))
            dbTwinClassEntity
                    .setHeadHunterParams(headHunterParams);
    }

    @Transactional
    public void updateTwinClassHeadTwinClass(TwinClassEntity dbTwinClassEntity, EntityRelinkOperation headRelinkOperation, ChangesHelper changesHelper) throws ServiceException {
        if (headRelinkOperation == null || !changesHelper.isChanged("headTwinClassId", dbTwinClassEntity.getHeadTwinClassId(), headRelinkOperation.getNewId()))
            return;
        if (dbTwinClassEntity.getHeadTwinClassId() == null || !twinRepository.existsByTwinClassId(dbTwinClassEntity.getId())) {
            dbTwinClassEntity
                    .setHeadTwinClassId(headRelinkOperation.getNewId())
                    .setHeadTwinClass(findEntitySafe(headRelinkOperation.getNewId()));
            return;
        }
        Set<UUID> existedTwinHeadIds = findExistedTwinHeadIdsOfClass(dbTwinClassEntity.getId());
        if (CollectionUtils.isEmpty(existedTwinHeadIds))
            return;
        if (headRelinkOperation.getStrategy() == EntityRelinkOperation.Strategy.restrict
                && MapUtils.isEmpty(headRelinkOperation.getReplaceMap()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide headReplaceMap for heads: " + StringUtils.join(existedTwinHeadIds));
        Set<UUID> twinsForDeletion = new HashSet<>();
        Set<UUID> newValidTwinHeadIds = twinRepository.findIdByTwinClassId(headRelinkOperation.getNewId());
        for (UUID headForReplace : existedTwinHeadIds) {
            UUID replacement = headRelinkOperation.getReplaceMap().get(headForReplace);
            if (replacement == null) {
                if (headRelinkOperation.getStrategy() == EntityRelinkOperation.Strategy.restrict)
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide headReplaceMap value for head: " + headForReplace);
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

    public void evictCache(UUID twinClassId) {
        Cache cache = cacheManager.getCache(TwinClassRepository.CACHE_TWIN_CLASS_BY_ID);
        if (cache != null)
            cache.evictIfPresent(twinClassId);
    }
}

