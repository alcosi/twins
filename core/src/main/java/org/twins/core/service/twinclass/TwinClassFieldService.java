package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.permission.PermissionService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinClassFieldService {
    final TwinClassFieldRepository twinClassFieldRepository;
    final PermissionService permissionService;
    final I18nService i18nService;

    public List<TwinClassFieldEntity> findTwinClassFields(ApiUser apiUser, UUID twinClassId) {
        permissionService.checkTwinClassPermission(apiUser, twinClassId);
        return findTwinClassFields(twinClassId);
    }

    public List<TwinClassFieldEntity> findTwinClassFields(UUID twinClassId) {
        return twinClassFieldRepository.findByTwinClassId(twinClassId);
    }

    public TwinClassFieldEntity findByTwinClassIdAndKey(UUID twinClassId, String key) {
        return twinClassFieldRepository.findByTwinClassIdAndKey(twinClassId, key);
    }

    @Transactional
    public void duplicateFieldsForClass(ApiUser apiUser, UUID srcTwinClassId, UUID duplicateTwinClassId) {
        List<TwinClassFieldEntity> fieldEntityList = findTwinClassFields(srcTwinClassId);
        if (CollectionUtils.isNotEmpty(fieldEntityList)) {
            for (TwinClassFieldEntity fieldEntity : fieldEntityList) {
                duplicateField(fieldEntity, duplicateTwinClassId);
            }
        }
    }

    @Transactional
    public void duplicateField(TwinClassFieldEntity srcFieldEntity, UUID duplicateTwinClassId) {
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
        twinClassFieldRepository.save(duplicateFieldEntity);
    }
}
