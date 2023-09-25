package org.twins.core.service.twinclass;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySmartService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassService {
    final TwinRepository twinRepository;
    final TwinClassRepository twinClassRepository;
    final TwinClassSchemaRepository twinClassSchemaRepository;
    final TwinClassFieldService twinClassFieldService;
    final EntitySmartService entitySmartService;
    final I18nService i18nService;
    final EntityManager entityManager;

    public List<TwinClassEntity> findTwinClasses(ApiUser apiUser, List<UUID> uuidLists) {
        if (CollectionUtils.isNotEmpty(uuidLists))
            return twinClassRepository.findByDomainIdAndIdIn(apiUser.getDomain().getId(), uuidLists);
        else
            return twinClassRepository.findByDomainId(apiUser.getDomain().getId());
    }

    public TwinClassEntity findTwinClass(ApiUser apiUser, UUID twinClassIs) {
        return twinClassRepository.findByDomainIdAndId(apiUser.getDomain().getId(), twinClassIs);
    }

    public TwinClassEntity findTwinClassByKey(ApiUser apiUser, String twinClassKey) {
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

    public UUID checkHeadTwinAllowedForClass(UUID headTwinId, TwinClassEntity subClass) throws ServiceException {
        if (subClass.getHeadTwinClassId() != null)
            if (headTwinId != null) {
                TwinEntity headTwinEntity = entitySmartService.findById(headTwinId, "headTwinId", twinRepository, EntitySmartService.FindMode.ifEmptyThrows);
//            if (!headTwinEntity.twinClass().space())
//                throw new ServiceException(ErrorCodeTwins.SPACE_TWIN_ID_INCORRECT, headTwinEntity.logShort() + " is not a space");
                if (!headTwinEntity.getTwinClassId().equals(subClass.getHeadTwinClassId()))
                    throw new ServiceException(ErrorCodeTwins.HEAD_TWIN_ID_NOT_ALLOWED, headTwinEntity.logShort() + " is not allowed for twinClass[" + subClass.getId() + "]");
                return headTwinId;
            } else {
                throw new ServiceException(ErrorCodeTwins.HEAD_TWIN_NOT_SPECIFIED, subClass.logShort() + " should be linked to head");
            }
        return headTwinId;
    }

    public void checkTwinClassPermission(ApiUser apiUser, UUID twinclassId) {

    }

    @Transactional
    public TwinClassEntity duplicateTwinClass(ApiUser apiUser, UUID twinClassId, String newKey) {
        TwinClassEntity srcTwinClassEntity = findTwinClass(apiUser, twinClassId);
        TwinClassEntity duplicateTwinClassEntity = new TwinClassEntity()
                .setKey(newKey)
                .setCreatedByUserId(apiUser.getUser().getId())
                .setSpace(srcTwinClassEntity.isSpace())
                .setAbstractt(srcTwinClassEntity.isAbstractt())
                .setLogo(srcTwinClassEntity.getLogo())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setDomainId(srcTwinClassEntity.getDomainId())
                .setOwnerType(srcTwinClassEntity.getOwnerType())
                .setDomain(srcTwinClassEntity.getDomain());
        I18nEntity i18nDuplicate;
        if (srcTwinClassEntity.getNameI18n() != null) {
            i18nDuplicate = i18nService.duplicateI18n(srcTwinClassEntity.getNameI18n());
            duplicateTwinClassEntity
                    .setNameI18n(i18nDuplicate)
                    .setNameI18NId(i18nDuplicate.getId());
        }
        if (srcTwinClassEntity.getDescriptionI18n() != null) {
            i18nDuplicate = i18nService.duplicateI18n(srcTwinClassEntity.getDescriptionI18n());
            duplicateTwinClassEntity
                    .setDescriptionI18n(i18nDuplicate)
                    .setDescriptionI18NId(i18nDuplicate.getId());
        }
        duplicateTwinClassEntity = twinClassRepository.save(duplicateTwinClassEntity);
        twinClassFieldService.duplicateFieldsForClass(apiUser, twinClassId, duplicateTwinClassEntity.getId());
        return duplicateTwinClassEntity;
    }
}

