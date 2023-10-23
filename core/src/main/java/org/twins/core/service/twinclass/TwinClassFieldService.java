package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.service.I18nService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class TwinClassFieldService extends EntitySecureFindServiceImpl<TwinClassFieldEntity> {
    final TwinClassFieldRepository twinClassFieldRepository;
    final PermissionService permissionService;
    @Lazy
    final TwinClassService twinClassService;
    final I18nService i18nService;
    final EntitySmartService entitySmartService;
    @Lazy
    final AuthService authService;

    @Override
    public String entityName() {
        return "twinClassField";
    }

    @Override
    public CrudRepository<TwinClassFieldEntity, UUID> entityRepository() {
        return twinClassFieldRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getTwinClass().getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allowed in domain[" + apiUser.getDomain().logShort());
            return true;
        }
        //todo check permission schema
        return false;
    }

    public List<TwinClassFieldEntity> findTwinClassFields(UUID twinClassId) {
        return twinClassFieldRepository.findByTwinClassId(twinClassId).stream().filter(twinClassFieldEntity -> !isEntityReadDenied(twinClassFieldEntity)).toList();
    }

    public List<TwinClassFieldEntity> findTwinClassFieldsIncludeParent(TwinClassEntity twinClassEntity) {
        List<UUID> twinClassList = twinClassService.findExtendedClasses(twinClassEntity, true);
        List<TwinClassFieldEntity> ret = twinClassFieldRepository.findByTwinClassIdIn(twinClassList);
        return ret.stream().filter(twinClassFieldEntity -> !isEntityReadDenied(twinClassFieldEntity)).toList();
    }

    public TwinClassFieldEntity findByTwinClassIdAndKey(UUID twinClassId, String key) {
        return twinClassFieldRepository.findByTwinClassIdAndKey(twinClassId, key);
    }

    public TwinClassFieldEntity findByTwinClassIdAndKeyIncludeParent(UUID twinClassId, String key) {
        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldRepository.findByTwinClassIdAndKey(twinClassId, key);
        if (twinClassFieldEntity == null)
            twinClassFieldEntity = twinClassFieldRepository.findByTwinClassIdAndParentKey(twinClassId, key);
        return twinClassFieldEntity;
    }

    @Transactional
    public void duplicateFieldsForClass(ApiUser apiUser, UUID srcTwinClassId, UUID duplicateTwinClassId) throws ServiceException {
        List<TwinClassFieldEntity> fieldEntityList = findTwinClassFields(srcTwinClassId);
        if (CollectionUtils.isNotEmpty(fieldEntityList)) {
            for (TwinClassFieldEntity fieldEntity : fieldEntityList) {
                duplicateField(fieldEntity, duplicateTwinClassId);
            }
        }
    }

    @Transactional
    public void duplicateField(TwinClassFieldEntity srcFieldEntity, UUID duplicateTwinClassId) throws ServiceException {
        log.info(srcFieldEntity.logShort() + " will be duplicated for class[" + duplicateTwinClassId + "]");
        TwinClassFieldEntity duplicateFieldEntity = new TwinClassFieldEntity()
                .setKey(srcFieldEntity.getKey())
                .setTwinClassId(duplicateTwinClassId)
                .setTwinClass(srcFieldEntity.getTwinClass())
                .setFieldTyperFeaturer(srcFieldEntity.getFieldTyperFeaturer())
                .setFieldTyperFeaturerId(srcFieldEntity.getFieldTyperFeaturerId())
                .setFieldTyperParams(srcFieldEntity.getFieldTyperParams())
                .setViewPermissionId(srcFieldEntity.getViewPermissionId())
                .setViewPermission(srcFieldEntity.getViewPermission())
                .setEditPermissionId(srcFieldEntity.getEditPermissionId())
                .setEditPermission(srcFieldEntity.getEditPermission())
                .setRequired(srcFieldEntity.isRequired());
        I18nEntity i18nEntity;
        if (srcFieldEntity.getNameI18n() != null) {
            i18nEntity = i18nService.duplicateI18n(srcFieldEntity.getNameI18n());
            duplicateFieldEntity
                    .setNameI18n(i18nEntity)
                    .setNameI18NId(i18nEntity.getId());
        }
        if (srcFieldEntity.getDescriptionI18n() != null) {
            i18nEntity = i18nService.duplicateI18n(srcFieldEntity.getDescriptionI18n());
            duplicateFieldEntity
                    .setDescriptionI18n(i18nEntity)
                    .setDescriptionI18NId(i18nEntity.getId());
        }
        entitySmartService.save(duplicateFieldEntity, twinClassFieldRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
    }
}
