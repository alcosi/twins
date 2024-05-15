package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.StringUtils;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.featurer.dao.FeaturerRepository;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.dao.I18nType;
import org.cambium.i18n.service.I18nService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.permission.PermissionRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyperLink;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;

import java.util.*;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class TwinClassFieldService extends EntitySecureFindServiceImpl<TwinClassFieldEntity> {
    final TwinClassFieldRepository twinClassFieldRepository;
    @Lazy
    final TwinClassService twinClassService;
    final I18nService i18nService;
    final EntitySmartService entitySmartService;
    final PermissionRepository permissionRepository;
    final FeaturerRepository featurerRepository;
    @Lazy
    final AuthService authService;

    @Override
    public CrudRepository<TwinClassFieldEntity, UUID> entityRepository() {
        return twinClassFieldRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getTwinClass().getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + apiUser.getDomain().easyLog(EasyLoggable.Level.NORMAL));
            return true;
        }
        //todo check permission schema
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFieldEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public List<TwinClassFieldEntity> findTwinClassFields(UUID twinClassId) {
        return twinClassFieldRepository.findByTwinClassId(twinClassId).stream().filter(twinClassFieldEntity -> !isEntityReadDenied(twinClassFieldEntity)).toList();
    }

    public Kit<TwinClassFieldEntity, UUID> loadTwinClassFields(TwinClassEntity twinClassEntity) {
        if (twinClassEntity.getTwinClassFieldKit() != null)
            return twinClassEntity.getTwinClassFieldKit();
        List<TwinClassFieldEntity> ret = twinClassFieldRepository.findByTwinClassIdIn(twinClassEntity.getExtendedClassIdSet());
        ret = ret.stream().filter(twinClassFieldEntity -> !isEntityReadDenied(twinClassFieldEntity)).toList();
        twinClassEntity.setTwinClassFieldKit(new Kit<>(ret, TwinClassFieldEntity::getId));
        return twinClassEntity.getTwinClassFieldKit();
    }

    public void loadTwinClassFields(Collection<TwinClassEntity> twinClassEntities) {
        Map<UUID, TwinClassEntity> needLoad = new HashMap<>();
        Set<UUID> forClasses = new HashSet<>();
        for (TwinClassEntity twinClassEntity : twinClassEntities)
            if (twinClassEntity.getTwinClassFieldKit() == null) {
                needLoad.put(twinClassEntity.getId(), twinClassEntity);
                forClasses.addAll(twinClassEntity.getExtendedClassIdSet());
            }
        if (needLoad.isEmpty())
            return;
        KitGrouped<TwinClassFieldEntity, UUID, UUID> fields = new KitGrouped<>(twinClassFieldRepository.findByTwinClassIdIn(forClasses), TwinClassFieldEntity::getId, TwinClassFieldEntity::getTwinClassId);
        for (TwinClassEntity twinClassEntity : needLoad.values()) {
            List<TwinClassFieldEntity> classFields = new ArrayList<>();
            for (UUID twinClassId : twinClassEntity.getExtendedClassIdSet()) {
                if (fields.containsGroupedKey(twinClassId))
                    classFields.addAll(fields.getGrouped(twinClassId));
            }
            twinClassEntity.setTwinClassFieldKit(new Kit<>(classFields, TwinClassFieldEntity::getId));
        }
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
                .setEditPermissionId(srcFieldEntity.getEditPermissionId())
                .setRequired(srcFieldEntity.isRequired());
        I18nEntity i18nEntity;
        if (srcFieldEntity.getNameI18NId() != null) {
            i18nEntity = i18nService.duplicateI18n(srcFieldEntity.getNameI18NId());
            duplicateFieldEntity
                    .setNameI18NId(i18nEntity.getId());
        }
        if (srcFieldEntity.getDescriptionI18NId() != null) {
            i18nEntity = i18nService.duplicateI18n(srcFieldEntity.getDescriptionI18NId());
            duplicateFieldEntity
                    .setDescriptionI18NId(i18nEntity.getId());
        }
        entitySmartService.save(duplicateFieldEntity, twinClassFieldRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
    }

    public TwinClassFieldEntity getFieldIdConfiguredForLink(UUID twinClassId, UUID linkId) {
        return twinClassFieldRepository.findByTwinClassIdAndFieldTyperIdInAndFieldTyperParamsLike(twinClassId, Set.of(FieldTyperLink.ID), "%" + linkId + "%");
    }

    public TwinClassFieldEntity getTwinClassFieldOrNull(TwinClassEntity twinClass, UUID twinClassFieldId) {
        loadTwinClassFields(twinClass);
        return twinClass.getTwinClassFieldKit().get(twinClassFieldId);
    }


    public static final HashMap<String, String> SIMPLE_FIELD_PARAMS = new HashMap<>() {{
        put("regexp", ".*");
    }};

    @Transactional
    public TwinClassFieldEntity createSimpleField(TwinClassFieldEntity twinClassFieldEntity, String name, String description) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (StringUtils.isBlank(twinClassFieldEntity.getKey()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT);
        twinClassFieldEntity.setKey(twinClassFieldEntity.getKey().trim().replaceAll("\\s", ""));
        if (twinClassFieldEntity.getTwinClassId() == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN);
        if (twinClassFieldEntity.getViewPermissionId() != null
                && permissionRepository.existsByIdAndPermissionGroup_DomainId(twinClassFieldEntity.getViewPermissionId(), apiUser.getDomainId()))
            throw new ServiceException(ErrorCodeTwins.PERMISSION_ID_UNKNOWN, "unknown view permission id");
        if (twinClassFieldEntity.getEditPermissionId() != null
                && permissionRepository.existsByIdAndPermissionGroup_DomainId(twinClassFieldEntity.getEditPermissionId(), apiUser.getDomainId()))
            throw new ServiceException(ErrorCodeTwins.PERMISSION_ID_UNKNOWN, "unknown edit permission id");
        FeaturerEntity fieldTyperSimple = featurerRepository.getById(1301);
        twinClassFieldEntity
                .setNameI18NId(i18nService.createI18nAndDefaultTranslation(I18nType.TWIN_CLASS_FIELD_NAME, name).getI18nId())
                .setDescriptionI18NId(i18nService.createI18nAndDefaultTranslation(I18nType.TWIN_CLASS_FIELD_DESCRIPTION, description).getI18nId())
                .setFieldTyperFeaturerId(fieldTyperSimple.getId())
                .setFieldTyperFeaturer(fieldTyperSimple)
                .setFieldTyperParams(SIMPLE_FIELD_PARAMS);
        twinClassFieldEntity = entitySmartService.save(twinClassFieldEntity, twinClassFieldRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        return twinClassFieldEntity;
    }
}
