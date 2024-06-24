package org.twins.core.service.twinclass;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.PaginationUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.dao.I18nType;
import org.cambium.i18n.service.I18nService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
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
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.pagination.PaginationResult;
import org.twins.core.service.pagination.SimplePagination;
import org.twins.core.service.twin.TwinStatusService;
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
        Page<TwinClassEntity> twinClassList = twinClassRepository.findAll(createTwinClassEntitySearchSpecification(twinClassSearch), PaginationUtils.pageableOffset(pagination));
        return convertCollectionInPaginationResult(twinClassList, pagination);
    }

    public Specification<TwinClassEntity> createTwinClassEntitySearchSpecification(TwinClassSearch twinClassSearch){
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

    private PaginationResult<TwinClassEntity> convertCollectionInPaginationResult(Page<TwinClassEntity> twinClassList, SimplePagination pagination) {
        PaginationResult<TwinClassEntity> twinClassEntityPaginationResult = new PaginationResult<>();
        twinClassEntityPaginationResult
                .setList(twinClassList.toList())
                .setTotal(twinClassList.getTotalElements())
                .setOffset(pagination.getOffset())
                .setLimit(pagination.getLimit());
        return twinClassEntityPaginationResult;
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
                .setNameI18NId(i18nService.createI18nAndDefaultTranslation(I18nType.TWIN_CLASS_NAME, name).getId())
                .setDescriptionI18NId(i18nService.createI18nAndDefaultTranslation(I18nType.TWIN_CLASS_DESCRIPTION, description).getId())
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
}

