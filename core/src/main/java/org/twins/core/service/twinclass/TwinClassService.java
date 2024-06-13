package org.twins.core.service.twinclass;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.*;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.dao.I18nType;
import org.cambium.i18n.service.I18nService;
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
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaRepository;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaMapEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.ReplaceOperation;
import org.twins.core.domain.TwinClassUpdate;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.twinclass.HeadHunter;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.twin.TwinMarkerService;
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
    public TwinClassEntity createInDomainClassTransactional(TwinClassEntity twinClassEntity, String name, String description) throws ServiceException {
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
        twinClassEntity
                .setKey(twinClassEntity.getKey().toUpperCase())
                .setNameI18NId(i18nService.createI18nAndDefaultTranslation(I18nType.TWIN_CLASS_NAME, name).getI18nId())
                .setDescriptionI18NId(i18nService.createI18nAndDefaultTranslation(I18nType.TWIN_CLASS_DESCRIPTION, description).getI18nId())
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

    public TwinClassEntity createInDomainClass(TwinClassEntity twinClassEntity, String name, String description) throws ServiceException {
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

    @Transactional
    public void updateTwinClass(TwinClassUpdate updateTwinClass) throws ServiceException {
        TwinClassEntity updateTwinClassEntity = updateTwinClass.getUpdateTwinClassEntity();
        TwinClassEntity dbTwinClassEntity = updateTwinClass.getDbTwinClassEntity();
        ChangesHelper changesHelper = new ChangesHelper();
        updateTwinClassHeadTwinClass(updateTwinClassEntity, dbTwinClassEntity, changesHelper);
        updateTwinClassHeadHunterFeaturer(updateTwinClassEntity, dbTwinClassEntity, changesHelper);
        updateTwinClassExtendsTwinClass(updateTwinClassEntity, dbTwinClassEntity, changesHelper);
        updateTwinClassAbstractFlag(updateTwinClassEntity, dbTwinClassEntity, changesHelper);
        updateTwinClassTwinClassSchemaSpaceFlag(updateTwinClassEntity, dbTwinClassEntity, changesHelper);
        updateTwinClassTwinflowSchemaSpaceFlag(updateTwinClassEntity, dbTwinClassEntity, changesHelper);
        updateTwinClassAliasSpaceFlag(updateTwinClassEntity, dbTwinClassEntity, changesHelper);
        updateTwinClassPermissionSchemaSpaceFlag(updateTwinClassEntity, dbTwinClassEntity, changesHelper);
        updateTwinClassViewPermission(updateTwinClassEntity, dbTwinClassEntity, changesHelper);
        updateTwinClassKey(updateTwinClassEntity, dbTwinClassEntity, changesHelper);
        updateTwinClassLogo(updateTwinClassEntity, dbTwinClassEntity, changesHelper);
        updateTwinClassMarkerDataList(updateTwinClassEntity, dbTwinClassEntity, updateTwinClass.getMarkersReplace(), changesHelper);
        updateTwinClassTagDataList(updateTwinClassEntity, dbTwinClassEntity, updateTwinClass.getTagsReplace(), changesHelper);
    }

    public void updateTwinClassTagDataList(TwinClassEntity updateTwinClassEntity, TwinClassEntity dbTwinClassEntity, ReplaceOperation replaceOperation, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("tagsDataListId", dbTwinClassEntity.getTagDataListId(), updateTwinClassEntity.getTagDataListId()))
            return;
        if (UuidUtils.isNullifyMarker(updateTwinClassEntity.getTagDataListId()))
            //we have to delete all tags from twins of given class
            twinTagService.deleteAllTagsForTwinsOfClass(updateTwinClassEntity.getId());
        else {
            twinTagService.replaceTagsForTwinsOfClass(updateTwinClassEntity, replaceOperation);
        }
        dbTwinClassEntity
                .setTagDataListId(UuidUtils.nullifyIfNecessary(updateTwinClassEntity.getTagDataListId()));
    }

    @Transactional
    public void updateTwinClassMarkerDataList(TwinClassEntity updateTwinClassEntity, TwinClassEntity dbTwinClassEntity, ReplaceOperation replaceOperation, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("markerDataListId", dbTwinClassEntity.getMarkerDataListId(), updateTwinClassEntity.getMarkerDataListId()))
            return;
        if (UuidUtils.isNullifyMarker(updateTwinClassEntity.getMarkerDataListId()))
            //we have to delete all markers from twins of given class
            twinMarkerService.deleteAllMarkersForTwinsOfClass(updateTwinClassEntity.getId());
        else {
            twinMarkerService.replaceMarkersForTwinsOfClass(updateTwinClassEntity, replaceOperation);
        }
        dbTwinClassEntity
                .setMarkerDataListId(UuidUtils.nullifyIfNecessary(updateTwinClassEntity.getMarkerDataListId()));
    }

    public void updateTwinClassLogo(TwinClassEntity updateTwinClassEntity, TwinClassEntity dbTwinClassEntity, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("logo", dbTwinClassEntity.getLogo(), updateTwinClassEntity.getLogo()))
            return;
        dbTwinClassEntity
                .setLogo(updateTwinClassEntity.getLogo());
    }

    public void updateTwinClassKey(TwinClassEntity updateTwinClassEntity, TwinClassEntity dbTwinClassEntity, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("key", dbTwinClassEntity.getKey(), updateTwinClassEntity.getKey()))
            return;
        if (twinClassRepository.existsByDomainIdAndKey(authService.getApiUser().getDomainId(), updateTwinClassEntity.getKey()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "class key is already exist");
        //todo generate new aliases for all existed twins. old class twin aliases should not be deleted, until we will detect conflicts
        dbTwinClassEntity
                .setKey(updateTwinClassEntity.getKey());
    }

    public void updateTwinClassViewPermission(TwinClassEntity updateTwinClassEntity, TwinClassEntity dbTwinClassEntity, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("viewPermissionId", dbTwinClassEntity.getViewPermissionId(), updateTwinClassEntity.getViewPermissionId()))
            return;
        dbTwinClassEntity
                .setViewPermissionId(UuidUtils.nullifyIfNecessary(updateTwinClassEntity.getViewPermissionId()));
    }

    public void updateTwinClassPermissionSchemaSpaceFlag(TwinClassEntity updateTwinClassEntity, TwinClassEntity dbTwinClassEntity, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("isPermissionSchemaSpace", dbTwinClassEntity.isPermissionSchemaSpace(), updateTwinClassEntity.isPermissionSchemaSpace()))
            return;
        //todo check that we have trigger which will update twin.twinflow_schema_space_id column twins of given class
        dbTwinClassEntity
                .setPermissionSchemaSpace(updateTwinClassEntity.isPermissionSchemaSpace());
    }

    public void updateTwinClassAliasSpaceFlag(TwinClassEntity updateTwinClassEntity, TwinClassEntity dbTwinClassEntity, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("isAliasSpace", dbTwinClassEntity.isAliasSpace(), updateTwinClassEntity.isAliasSpace()))
            return;
        //todo check that we have trigger which will update twin.alias_space_id column twins of given class
        dbTwinClassEntity
                .setAliasSpace(updateTwinClassEntity.isAliasSpace());
    }

    public void updateTwinClassTwinflowSchemaSpaceFlag(TwinClassEntity updateTwinClassEntity, TwinClassEntity dbTwinClassEntity, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("isTwinflowSchemaSpace", dbTwinClassEntity.isTwinflowSchemaSpace(), updateTwinClassEntity.isTwinflowSchemaSpace()))
            return;
        //todo check that we have trigger which will update twin.twinflow_schema_space_id column twins of given class
        dbTwinClassEntity
                .setTwinflowSchemaSpace(updateTwinClassEntity.isTwinflowSchemaSpace());
    }

    public void updateTwinClassTwinClassSchemaSpaceFlag(TwinClassEntity updateTwinClassEntity, TwinClassEntity dbTwinClassEntity, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("isTwinClassSchemaSpace", dbTwinClassEntity.isTwinClassSchemaSpace(), updateTwinClassEntity.isTwinClassSchemaSpace()))
            return;
        //todo check that we have trigger which will update twin.twin_class_schema_space_id column twins of given class
        dbTwinClassEntity
                .setTwinClassSchemaSpace(updateTwinClassEntity.isTwinClassSchemaSpace());
    }

    public void updateTwinClassAbstractFlag(TwinClassEntity updateTwinClassEntity, TwinClassEntity dbTwinClassEntity, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("isAbstract", dbTwinClassEntity.isAbstractt(), updateTwinClassEntity.isAbstractt()))
            return;
        if (updateTwinClassEntity.isAbstractt() && twinRepository.existsByTwinClassId(dbTwinClassEntity.getExtendsTwinClassId()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "class can not be marked abstract, because some twins are already exist");
        dbTwinClassEntity
                .setAbstractt(updateTwinClassEntity.isAbstractt());
    }

    public void updateTwinClassExtendsTwinClass(TwinClassEntity updateTwinClassEntity, TwinClassEntity dbTwinClassEntity, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("extendsTwinClassId", dbTwinClassEntity.getExtendsTwinClassId(), updateTwinClassEntity.getExtendsTwinClassId()))
            return;
        if (dbTwinClassEntity.getExtendsTwinClassId() != null && twinRepository.existsByTwinClassId(dbTwinClassEntity.getExtendsTwinClassId()))
            //todo restrict only if there are fields
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "extends class can not be changed, because some twins are already exist");
        dbTwinClassEntity
                .setExtendsTwinClassId(updateTwinClassEntity.getExtendsTwinClassId())
                .setExtendsTwinClass(updateTwinClassEntity.getExtendsTwinClass());
    }

    public void updateTwinClassHeadHunterFeaturer(TwinClassEntity updateTwinClassEntity, TwinClassEntity dbTwinClassEntity, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("headHunterFeaturerId", dbTwinClassEntity.getHeadHunterFeaturerId(), updateTwinClassEntity.getHeadHunterFeaturerId()))
            return;
        FeaturerEntity newHeadHunterFeaturer = featurerService.checkValid(updateTwinClassEntity.getHeadHunterFeaturerId(), updateTwinClassEntity.getHeadHunterParams(), HeadHunter.class);
        dbTwinClassEntity
                .setHeadHunterFeaturerId(newHeadHunterFeaturer.getId())
                .setHeadHunterFeaturer(newHeadHunterFeaturer)
                .setHeadHunterParams(updateTwinClassEntity.getHeadHunterParams());
    }

    public void updateTwinClassHeadTwinClass(TwinClassEntity updateTwinClassEntity, TwinClassEntity dbTwinClassEntity, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("headTwinClassId", dbTwinClassEntity.getHeadTwinClassId(), updateTwinClassEntity.getHeadTwinClassId()))
            return;
        if (dbTwinClassEntity.getHeadTwinClassId() != null && twinRepository.existsByTwinClassId(dbTwinClassEntity.getHeadTwinClassId()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "head class can not be changed, because some twins are already exist");
        dbTwinClassEntity
                .setHeadTwinClassId(updateTwinClassEntity.getHeadTwinClassId())
                .setHeadTwinClass(updateTwinClassEntity.getHeadTwinClass());
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
}

