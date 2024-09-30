package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.MapUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.featurer.dao.FeaturerRepository;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.dao.I18nType;
import org.cambium.i18n.service.I18nService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
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
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperLink;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.*;


@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class TwinClassFieldService extends EntitySecureFindServiceImpl<TwinClassFieldEntity> {

    private final TwinClassFieldRepository twinClassFieldRepository;
    private final I18nService i18nService;
    private final EntitySmartService entitySmartService;
    private final PermissionRepository permissionRepository;
    private final FeaturerRepository featurerRepository;
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final TwinClassService twinClassService;
    private final FeaturerService featurerService;
    @Lazy
    private final AuthService authService;

    @Override
    public CrudRepository<TwinClassFieldEntity, UUID> entityRepository() {
        return twinClassFieldRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (checkOwnerTypeIsSystem(entity))
            return false;
        if (!entity.getTwinClass().getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + apiUser.getDomain().easyLog(EasyLoggable.Level.NORMAL));
            return true;
        }
        //todo check permission schema
        return false;
    }

    private boolean checkOwnerTypeIsSystem(TwinClassFieldEntity entity) {
        return entity.getTwinClass().getOwnerType().equals(TwinClassEntity.OwnerType.SYSTEM);
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
    public TwinClassFieldEntity createField(TwinClassFieldEntity twinClassFieldEntity, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (StringUtils.isBlank(twinClassFieldEntity.getKey()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT);
        if (twinClassFieldRepository.existsByKeyAndTwinClassId(twinClassFieldEntity.getKey(), twinClassFieldEntity.getTwinClassId()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT, "Twin class field with key[" + twinClassFieldEntity.getKey() + "] already exists for twin class: " + twinClassFieldEntity.getTwinClassId());
        twinClassFieldEntity.setKey(twinClassFieldEntity.getKey().trim().replaceAll("\\s", ""));
        if (twinClassFieldEntity.getTwinClassId() == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN);
        if (twinClassFieldEntity.getViewPermissionId() != null
                && permissionRepository.existsByIdAndPermissionGroup_DomainId(twinClassFieldEntity.getViewPermissionId(), apiUser.getDomainId()))
            throw new ServiceException(ErrorCodeTwins.PERMISSION_ID_UNKNOWN, "unknown view permission id");
        if (twinClassFieldEntity.getEditPermissionId() != null
                && permissionRepository.existsByIdAndPermissionGroup_DomainId(twinClassFieldEntity.getEditPermissionId(), apiUser.getDomainId()))
            throw new ServiceException(ErrorCodeTwins.PERMISSION_ID_UNKNOWN, "unknown edit permission id");

        FeaturerEntity fieldTyper;
        HashMap<String, String> params;
        if(null != twinClassFieldEntity.getFieldTyperFeaturerId()) {
            params = twinClassFieldEntity.getFieldTyperParams();
            fieldTyper = featurerService.checkValid(twinClassFieldEntity.getFieldTyperFeaturerId(), params, FieldTyper.class);
        } else {
            params = SIMPLE_FIELD_PARAMS;
            fieldTyper = featurerRepository.getById(1301);
        }

        twinClassFieldEntity
                .setNameI18NId(i18nService.createI18nAndTranslations(I18nType.TWIN_CLASS_FIELD_NAME, nameI18n).getId())
                .setDescriptionI18NId(i18nService.createI18nAndTranslations(I18nType.TWIN_CLASS_FIELD_DESCRIPTION, descriptionI18n).getId())
                .setFieldTyperFeaturerId(fieldTyper.getId())
                .setFieldTyperFeaturer(fieldTyper)
                .setFieldTyperParams(params);


        twinClassFieldEntity = entitySmartService.save(twinClassFieldEntity, twinClassFieldRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        twinClassService.evictCache(twinClassFieldEntity.getTwinClassId());
        return twinClassFieldEntity;
    }

    public KitGrouped<TwinClassFieldEntity, UUID, UUID> findTwinClassFields(Collection<UUID> ids) {
        KitGrouped<TwinClassFieldEntity, UUID, UUID> result = new KitGrouped<>(TwinClassFieldEntity::getId, TwinClassFieldEntity::getTwinClassId);
        if (CollectionUtils.isEmpty(ids))
            return result;
        result.addAll(twinClassFieldRepository.findByIdIn(ids));
        return result;
    }

    public boolean isConvertable(TwinClassFieldEntity fromTwinClassField, TwinClassFieldEntity toTwinClassField) {
        //todo move logic to FieldTyper and make it more smart (not all FieldTyperParams are important)
        return fromTwinClassField.getFieldTyperFeaturerId() == toTwinClassField.getFieldTyperFeaturerId()
                && MapUtils.areEqual(fromTwinClassField.getFieldTyperParams(), toTwinClassField.getFieldTyperParams());
    }

    @Transactional
    public TwinClassFieldEntity updateField(TwinClassFieldEntity twinClassFieldEntity, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        TwinClassFieldEntity dbTwinClassFieldEntity = findEntitySafe(twinClassFieldEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateTwinClassFieldTwinClass(dbTwinClassFieldEntity, twinClassFieldEntity.getTwinClassId(), changesHelper);
        updateTwinClassField_FieldTyperFeaturerId(dbTwinClassFieldEntity, twinClassFieldEntity.getFieldTyperFeaturerId(), twinClassFieldEntity.getFieldTyperParams(), changesHelper);
        updateTwinClassFieldName(dbTwinClassFieldEntity, nameI18n, changesHelper);
        updateTwinClassFieldDescription(dbTwinClassFieldEntity, descriptionI18n, changesHelper);
        updateTwinClassFieldViewPermission(dbTwinClassFieldEntity, twinClassFieldEntity.getViewPermissionId(), changesHelper);
        updateTwinClassFieldEditPermission(dbTwinClassFieldEntity, twinClassFieldEntity.getEditPermissionId(), changesHelper);
        updateTwinClassFieldRequiredFlag(dbTwinClassFieldEntity, twinClassFieldEntity.isRequired(), changesHelper);
        entitySmartService.saveAndLogChanges(dbTwinClassFieldEntity, twinClassFieldRepository, changesHelper);
        if(changesHelper.hasChanges()) twinClassService.evictCache(dbTwinClassFieldEntity.getTwinClassId());
        return twinClassFieldEntity;
    }

    @Transactional
    public void updateTwinClassFieldTwinClass(TwinClassFieldEntity dbTwinClassFieldEntity, UUID newTwinClassId, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("twinClassId", dbTwinClassFieldEntity.getTwinClassId(), newTwinClassId))
            return;
        if (twinService.areFieldsOfTwinClassFieldExists(dbTwinClassFieldEntity) &&
                !twinClassService.isInstanceOf(dbTwinClassFieldEntity.getTwinClass(), newTwinClassId))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_UPDATE_RESTRICTED, "twin-class of twin-class-field can not be updated, because some twins with fields of given class are already exist, " +
                    "and you can only change the class to the parent class from which the current class inherits.");
        dbTwinClassFieldEntity
                .setTwinClassId(newTwinClassId)
                .setTwinClass(twinClassService.findEntitySafe(newTwinClassId));
    }


    @Transactional
    public void updateTwinClassField_FieldTyperFeaturerId(TwinClassFieldEntity dbTwinClassFieldEntity, Integer newFeaturerId, HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        if (changesHelper.isChanged("fieldTyperFeaturerId", dbTwinClassFieldEntity.getFieldTyperFeaturerId(), newFeaturerId)) {
            if (twinService.areFieldsOfTwinClassFieldExists(dbTwinClassFieldEntity))
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_UPDATE_RESTRICTED, "class field can not change fieldtyper featurer, because some twins with fields of given class are already exist");
            FeaturerEntity newFieldTyperFeaturer = featurerService.checkValid(newFeaturerId, newFeaturerParams, FieldTyper.class);
            dbTwinClassFieldEntity
                    .setFieldTyperFeaturerId(newFieldTyperFeaturer.getId())
                    .setFieldTyperFeaturer(newFieldTyperFeaturer);
        }
        if (!MapUtils.areEqual(dbTwinClassFieldEntity.getFieldTyperParams(), newFeaturerParams))
            changesHelper.add("fieldTyperParams", dbTwinClassFieldEntity.getFieldTyperParams(), newFeaturerParams);
            dbTwinClassFieldEntity
                    .setFieldTyperParams(newFeaturerParams);
    }

    @Transactional
    public void updateTwinClassFieldName(TwinClassFieldEntity dbTwinClassFieldEntity, I18nEntity nameI18n, ChangesHelper changesHelper) throws ServiceException {
        if (nameI18n == null)
            return;
        if (dbTwinClassFieldEntity.getNameI18NId() != null)
            nameI18n.setId(dbTwinClassFieldEntity.getNameI18NId());
        i18nService.saveTranslations(I18nType.TWIN_CLASS_NAME, nameI18n);
        dbTwinClassFieldEntity.setNameI18NId(nameI18n.getId());
    }

    @Transactional
    public void updateTwinClassFieldDescription(TwinClassFieldEntity dbTwinClassFieldEntity, I18nEntity descriptionI18n, ChangesHelper changesHelper) throws ServiceException {
        if (descriptionI18n == null)
            return;
        if (dbTwinClassFieldEntity.getDescriptionI18NId() != null)
            descriptionI18n.setId(dbTwinClassFieldEntity.getDescriptionI18NId());
        i18nService.saveTranslations(I18nType.TWIN_CLASS_NAME, descriptionI18n);
        dbTwinClassFieldEntity.setDescriptionI18NId(descriptionI18n.getId());
    }

    @Transactional
    public void updateTwinClassFieldViewPermission(TwinClassFieldEntity dbTwinClassFieldEntity, UUID newViewPermissionId, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("viewPermissionId", dbTwinClassFieldEntity.getViewPermissionId(), newViewPermissionId))
            return;
        dbTwinClassFieldEntity.setViewPermissionId(UuidUtils.nullifyIfNecessary(newViewPermissionId));
    }

    @Transactional
    public void updateTwinClassFieldEditPermission(TwinClassFieldEntity dbTwinClassFieldEntity, UUID newEditPermissionId, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("editPermissionId", dbTwinClassFieldEntity.getEditPermissionId(), newEditPermissionId))
            return;
        dbTwinClassFieldEntity.setEditPermissionId(UuidUtils.nullifyIfNecessary(newEditPermissionId));
    }

    @Transactional
    public void updateTwinClassFieldRequiredFlag(TwinClassFieldEntity dbTwinClassFieldEntity, Boolean newRequiredFlag, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged("isRequired", dbTwinClassFieldEntity.isRequired(), newRequiredFlag))
            return;
        dbTwinClassFieldEntity.setRequired(newRequiredFlag);
    }

}
