package org.twins.core.service.twinclass;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.service.I18nService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class TwinClassService extends EntitySecureFindServiceImpl<TwinClassEntity> {
    final TwinRepository twinRepository;
    final TwinClassRepository twinClassRepository;
    final TwinClassExtendsMapRepository twinClassExtendsMapRepository;
    final TwinClassChildMapRepository twinClassChildMapRepository;
    final TwinClassSchemaRepository twinClassSchemaRepository;
    final TwinClassFieldService twinClassFieldService;
    final EntitySmartService entitySmartService;
    final I18nService i18nService;
    final EntityManager entityManager;
    @Lazy
    final AuthService authService;

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

    public List<TwinClassEntity> findTwinClasses(ApiUser apiUser, List<UUID> uuidLists) throws ServiceException {
        if (CollectionUtils.isNotEmpty(uuidLists))
            return twinClassRepository.findByDomainIdAndIdIn(apiUser.getDomain().getId(), uuidLists);
        else
            return twinClassRepository.findByDomainId(apiUser.getDomain().getId());
    }

    public TwinClassEntity findTwinClassByKey(ApiUser apiUser, String twinClassKey) throws ServiceException {
        return twinClassRepository.findByDomainIdAndKey(apiUser.getDomain().getId(), twinClassKey);
    }

    public UUID checkTwinClassSchemaAllowed(UUID domainId, UUID twinClassSchemaId) throws ServiceException {
        Optional<TwinClassSchemaEntity> twinClassSchemaEntity = twinClassSchemaRepository.findById(twinClassSchemaId);
        if (twinClassSchemaEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown twinClassSchemaId[" + twinClassSchemaId + "]");
        if (twinClassSchemaEntity.get().domainId() != domainId)
            throw new ServiceException(ErrorCodeTwins.PERMISSION_SCHEMA_NOT_ALLOWED, "twinClassSchemaId[" + twinClassSchemaId + "] is not allows in domain[" + domainId + "]");
        return twinClassSchemaId;
    }

    @Transactional
    public TwinClassEntity duplicateTwinClass(ApiUser apiUser, UUID twinClassId, String newKey) throws ServiceException {
        TwinClassEntity srcTwinClassEntity = findEntity(twinClassId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
        log.info(srcTwinClassEntity + " will be duplicated with ne key[" + newKey + "]");
        TwinClassEntity duplicateTwinClassEntity = new TwinClassEntity()
                .setKey(newKey)
                .setCreatedByUserId(apiUser.getUser().getId())
                .setSpace(srcTwinClassEntity.isSpace())
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
                log.warn(twinClassEntity.easyLog(EasyLoggable.Level.NORMAL) + " inheritance recursion");
                break;
            }
            ret.add(extendedTwinClassId);
        }
        return ret;
    }

    public Set<UUID> loadExtendedClasses(TwinClassEntity twinClassEntity) {
        if (twinClassEntity.getExtendedClassIdSet() != null)
            return twinClassEntity.getExtendedClassIdSet();
        Set<UUID> extendedClassIdSet = twinClassExtendsMapRepository.findAllByTwinClassId(twinClassEntity.getId())
                .stream().map(TwinClassExtendsMapEntity::getExtendsTwinClassId).collect(Collectors.toSet());
        twinClassEntity.setExtendedClassIdSet(extendedClassIdSet);
        return extendedClassIdSet;
    }

    public Set<UUID> loadChildClasses(TwinClassEntity twinClassEntity) {
        if (twinClassEntity.getChildClassIdSet() != null)
            return twinClassEntity.getChildClassIdSet();
        Set<UUID> childClassIdSet = twinClassChildMapRepository.findAllByTwinClassId(twinClassEntity.getId())
                .stream().map(TwinClassChildMapEntity::getChildTwinClassId).collect(Collectors.toSet());
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
            loadExtendedClasses(instanceClass);
            return instanceClass.getExtendedClassIdSet().contains(ofClass);
        }
        return true;
    }
}

