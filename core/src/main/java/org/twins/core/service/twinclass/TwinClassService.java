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

    public UUID checkHeadTwinAllowedForClass(UUID headTwinId, UUID subClassId) throws ServiceException {
        TwinClassEntity twinClassEntity = entitySmartService.findById(subClassId, "twinClassId", twinClassRepository, EntitySmartService.FindMode.ifEmptyThrows);
        if (twinClassEntity.headTwinClassId() != null)
            if (headTwinId != null) {
                TwinEntity headTwinEntity = entitySmartService.findById(headTwinId, "headTwinId", twinRepository, EntitySmartService.FindMode.ifEmptyThrows);
//            if (!headTwinEntity.twinClass().space())
//                throw new ServiceException(ErrorCodeTwins.SPACE_TWIN_ID_INCORRECT, headTwinEntity.logShort() + " is not a space");
                if (!headTwinEntity.twinClassId().equals(twinClassEntity.headTwinClassId()))
                    throw new ServiceException(ErrorCodeTwins.HEAD_TWIN_ID_NOT_ALLOWED, headTwinEntity.logShort() + " is not allowed for twinClass[" + subClassId + "]");
                return headTwinId;
            } else {
                throw new ServiceException(ErrorCodeTwins.HEAD_TWIN_NOT_SPECIFIED, twinClassEntity.logShort() + " should be linked to head");
            }
        return headTwinId;
    }

    public void checkTwinClassPermission(ApiUser apiUser, UUID twinclassId) {

    }

    @Transactional
    public TwinClassEntity duplicateTwinClass(ApiUser apiUser, UUID twinClassId, String newKey) {
        TwinClassEntity srcTwinClassEntity = findTwinClass(apiUser, twinClassId);
        TwinClassEntity duplicateTwinClassEntity = new TwinClassEntity()
                .key(newKey)
                .createdByUserId(apiUser.getUser().getId())
                .space(srcTwinClassEntity.space())
                .abstractt(srcTwinClassEntity.abstractt())
                .logo(srcTwinClassEntity.logo())
                .createdAt(Timestamp.from(Instant.now()))
                .domainId(srcTwinClassEntity.domainId())
                .domain(srcTwinClassEntity.domain());
        I18nEntity i18nDuplicate;
        if (srcTwinClassEntity.nameI18n() != null) {
            i18nDuplicate = i18nService.duplicateI18n(srcTwinClassEntity.nameI18n());
            duplicateTwinClassEntity
                    .nameI18n(i18nDuplicate)
                    .nameI18NId(i18nDuplicate.getId());
        }
        if (srcTwinClassEntity.descriptionI18n() != null) {
            i18nDuplicate = i18nService.duplicateI18n(srcTwinClassEntity.descriptionI18n());
            duplicateTwinClassEntity
                    .descriptionI18n(i18nDuplicate)
                    .descriptionI18NId(i18nDuplicate.getId());
        }
        duplicateTwinClassEntity = twinClassRepository.save(duplicateTwinClassEntity);
        twinClassFieldService.duplicateFieldsForClass(apiUser, twinClassId, duplicateTwinClassEntity.id());
        return duplicateTwinClassEntity;
    }
}

