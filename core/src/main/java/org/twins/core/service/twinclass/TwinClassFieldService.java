package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.KeyUtils;
import org.cambium.common.util.MapUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.featurer.dao.FeaturerRepository;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.i18n.I18nType;
import org.twins.core.service.i18n.I18nService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.permission.PermissionRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRepository;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperLink;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.*;
import java.util.function.Function;

import static org.cambium.common.util.CacheUtils.evictCache;


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

    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private TwinClassRepository twinClassRepository;

    @Override
    public CrudRepository<TwinClassFieldEntity, UUID> entityRepository() {
        return twinClassFieldRepository;
    }

    @Override
    public Function<TwinClassFieldEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (twinClassService.isOwnerSystemType(entity.getTwinClass()))
            return false;
        if (!entity.getTwinClass().getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + apiUser.getDomain().easyLog(EasyLoggable.Level.NORMAL));
            return true;
        }
        //todo check permission schema
        return false;
    }


    @Override
    public boolean validateEntity(TwinClassFieldEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (null == entity.getTwinClassId())
            return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_FIELD_TWIN_CLASS_NOT_SPECIFIED.getMessage());
        if (null == entity.getFieldTyperFeaturerId())
            return logErrorAndReturnFalse(ErrorCodeTwins.TWIN_CLASS_FIELD_FEATURER_NOT_SPECIFIED.getMessage());
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getTwinClass() == null || !entity.getTwinClass().getId().equals(entity.getTwinClassId()))
                    entity.setTwinClass(twinClassService.findEntitySafe(entity.getTwinClassId()));
                if (entity.getFieldTyperFeaturer() == null || !(entity.getFieldTyperFeaturer().getId() == entity.getFieldTyperFeaturerId()))
                    entity.setFieldTyperFeaturer(featurerService.checkValid(entity.getFieldTyperFeaturerId(), entity.getFieldTyperParams(), FieldTyper.class));
            default:
        }
        return true;
    }

    @Override
    public CacheSupportType getCacheSupportType() {
        return CacheSupportType.REQUEST;
    }

    // only direct fields
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

    public TwinClassFieldEntity findByTwinClassKeyAndKey(String twinClassKey, String fieldKey) throws ServiceException {
        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldRepository.findByTwinClass_KeyAndKey(twinClassKey, fieldKey);
        return checkEntityReadAllow(twinClassFieldEntity);
    }

    public TwinClassFieldEntity findByTwinClassIdAndKeyIncludeParents(UUID twinClassId, String key) {
        TwinClassEntity twinClass = twinClassRepository.findById(twinClassId).orElse(null);
        return findByTwinClassIdAndKeyIncludeParents(twinClass, key);
    }

    public TwinClassFieldEntity findByTwinClassIdAndKeyIncludeParents(TwinClassEntity twinClass, String key) {
        TwinClassFieldEntity twinClassFieldEntity = null;
        if (twinClass != null) {
            Set<UUID> extendedClassIds = twinClass.getExtendedClassIdSet();
            twinClassFieldEntity = twinClassFieldRepository.findByKeyAndTwinClassIdIn(key, extendedClassIds);
        }
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
                .setKey(KeyUtils.lowerCaseNullSafe(srcFieldEntity.getKey(), ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT))
                .setTwinClassId(duplicateTwinClassId)
                .setTwinClass(srcFieldEntity.getTwinClass())
                .setFieldTyperFeaturer(srcFieldEntity.getFieldTyperFeaturer())
                .setFieldTyperFeaturerId(srcFieldEntity.getFieldTyperFeaturerId())
                .setFieldTyperParams(srcFieldEntity.getFieldTyperParams())
                .setViewPermissionId(srcFieldEntity.getViewPermissionId())
                .setEditPermissionId(srcFieldEntity.getEditPermissionId())
                .setRequired(srcFieldEntity.getRequired());
        I18nEntity i18nEntity;
        if (srcFieldEntity.getNameI18nId() != null) {
            i18nEntity = i18nService.duplicateI18n(srcFieldEntity.getNameI18nId());
            duplicateFieldEntity
                    .setNameI18nId(i18nEntity.getId());
        }
        if (srcFieldEntity.getDescriptionI18nId() != null) {
            i18nEntity = i18nService.duplicateI18n(srcFieldEntity.getDescriptionI18nId());
            duplicateFieldEntity
                    .setDescriptionI18nId(i18nEntity.getId());
        }
        entitySmartService.save(duplicateFieldEntity, twinClassFieldRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
    }


    public static final String CACHE_TWIN_CLASS_FIELD_FOR_LINK = "TwinClassFieldService.getFieldIdConfiguredForLink";
    @Cacheable(value = CACHE_TWIN_CLASS_FIELD_FOR_LINK, key = "#twinClassId + '' + #linkId")
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

    @Transactional(rollbackFor = Throwable.class)
    public TwinClassFieldEntity createField(TwinClassFieldEntity twinClassFieldEntity, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        twinClassFieldEntity.setKey(KeyUtils.lowerCaseNullSafe(twinClassFieldEntity.getKey(), ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT));
        if (twinClassFieldRepository.existsByKeyAndTwinClassId(twinClassFieldEntity.getKey(), twinClassFieldEntity.getTwinClassId()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT, "Twin class field with key[" + twinClassFieldEntity.getKey() + "] already exists for twin class: " + twinClassFieldEntity.getTwinClassId());

        if (twinClassFieldEntity.getTwinClassId() == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN);
        if (twinClassFieldEntity.getViewPermissionId() != null
                && !permissionRepository.existsByIdAndPermissionGroup_DomainId(twinClassFieldEntity.getViewPermissionId(), apiUser.getDomainId()))
            throw new ServiceException(ErrorCodeTwins.PERMISSION_ID_UNKNOWN, "unknown view permission id");
        if (twinClassFieldEntity.getEditPermissionId() != null
                && !permissionRepository.existsByIdAndPermissionGroup_DomainId(twinClassFieldEntity.getEditPermissionId(), apiUser.getDomainId()))
            throw new ServiceException(ErrorCodeTwins.PERMISSION_ID_UNKNOWN, "unknown edit permission id");

        FeaturerEntity fieldTyper;
        HashMap<String, String> params;
        if (null != twinClassFieldEntity.getFieldTyperFeaturerId()) {
            params = twinClassFieldEntity.getFieldTyperParams();
            fieldTyper = featurerService.checkValid(twinClassFieldEntity.getFieldTyperFeaturerId(), params, FieldTyper.class);
        } else {
            params = SIMPLE_FIELD_PARAMS;
            fieldTyper = featurerRepository.getById(1301);
        }

        twinClassFieldEntity
                .setNameI18nId(i18nService.createI18nAndTranslations(I18nType.TWIN_CLASS_FIELD_NAME, nameI18n).getId())
                .setDescriptionI18nId(i18nService.createI18nAndTranslations(I18nType.TWIN_CLASS_FIELD_DESCRIPTION, descriptionI18n).getId())
                .setFieldTyperFeaturerId(fieldTyper.getId())
                .setFieldTyperFeaturer(fieldTyper)
                .setFieldTyperParams(params);


        validateEntityAndThrow(twinClassFieldEntity, EntitySmartService.EntityValidateMode.beforeSave);
        twinClassFieldEntity = entitySmartService.save(twinClassFieldEntity, twinClassFieldRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        Map<String, List<Object>> cacheEntries = Map.of(
                TwinClassRepository.CACHE_TWIN_CLASS_BY_ID, List.of(twinClassFieldEntity.getTwinClassId()),
                TwinClassEntity.class.getSimpleName(), List.of(twinClassFieldEntity.getTwinClassId())
        );
        evictCache(cacheManager, cacheEntries);
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

    @Transactional(rollbackFor = Throwable.class)
    public TwinClassFieldEntity updateField(TwinClassFieldEntity twinClassFieldEntity, I18nEntity nameI18n, I18nEntity descriptionI18n) throws ServiceException {
        TwinClassFieldEntity dbTwinClassFieldEntity = findEntitySafe(twinClassFieldEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateTwinClassFieldTwinClass(dbTwinClassFieldEntity, twinClassFieldEntity.getTwinClassId(), changesHelper);
        updateTwinClassField_FieldTyperFeaturerId(dbTwinClassFieldEntity, twinClassFieldEntity.getFieldTyperFeaturerId(), twinClassFieldEntity.getFieldTyperParams(), changesHelper);
        updateTwinClassFieldName(dbTwinClassFieldEntity, nameI18n, changesHelper);
        updateTwinClassFieldDescription(dbTwinClassFieldEntity, descriptionI18n, changesHelper);
        updateTwinClassFieldViewPermission(dbTwinClassFieldEntity, twinClassFieldEntity.getViewPermissionId(), changesHelper);
        updateTwinClassFieldEditPermission(dbTwinClassFieldEntity, twinClassFieldEntity.getEditPermissionId(), changesHelper);
        updateTwinClassFieldRequiredFlag(dbTwinClassFieldEntity, twinClassFieldEntity.getRequired(), changesHelper);

        dbTwinClassFieldEntity = updateSafe(dbTwinClassFieldEntity, changesHelper);
        if (changesHelper.hasChanges()) {
            Map<String, List<Object>> cacheEntries = Map.of(
                    TwinClassRepository.CACHE_TWIN_CLASS_BY_ID, List.of(dbTwinClassFieldEntity.getTwinClassId()),
                    TwinClassEntity.class.getSimpleName(), List.of(dbTwinClassFieldEntity.getTwinClassId()),
                    TwinClassFieldRepository.CACHE_TWIN_CLASS_FIELD_BY_ID_IN, Collections.emptyList(),
                    TwinClassFieldRepository.CACHE_TWIN_CLASS_FIELD_BY_TWIN_CLASS_ID_IN, Collections.emptyList(),
                    TwinClassFieldRepository.CACHE_TWIN_CLASS_FIELD_BY_KEY_AND_TWIN_CLASS_ID_IN, Collections.emptyList(),
                    CACHE_TWIN_CLASS_FIELD_FOR_LINK, Collections.emptyList(),
                    TwinClassFieldRepository.CACHE_TWIN_CLASS_FIELD_BY_TWIN_CLASS_AND_KEY, Collections.emptyList(),
                    TwinClassFieldRepository.CACHE_TWIN_CLASS_FIELD_BY_TWIN_CLASS_AND_PARENT_KEY, Collections.emptyList()
            );
            evictCache(cacheManager, cacheEntries);
        }
        return dbTwinClassFieldEntity;
    }

    public void updateTwinClassFieldTwinClass(TwinClassFieldEntity dbTwinClassFieldEntity, UUID newTwinClassId, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged(TwinClassFieldEntity.Fields.twinClassId, dbTwinClassFieldEntity.getTwinClassId(), newTwinClassId))
            return;
        if (twinService.areFieldsOfTwinClassFieldExists(dbTwinClassFieldEntity) &&
                !twinClassService.isInstanceOf(dbTwinClassFieldEntity.getTwinClass(), newTwinClassId))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_UPDATE_RESTRICTED, "twin-class of twin-class-field can not be updated, because some twins with fields of given class are already exist, " +
                    "and you can only change the class to the parent class from which the current class inherits.");
        dbTwinClassFieldEntity
                .setTwinClassId(newTwinClassId);
    }


    public void updateTwinClassField_FieldTyperFeaturerId(TwinClassFieldEntity dbTwinClassFieldEntity, Integer newFeaturerId, HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        FeaturerEntity newFieldTyperFeaturer = null;
        if (newFeaturerId == null || newFeaturerId == 0) {
            if (MapUtils.isEmpty(newFeaturerParams))
                return; //nothing was changed
            else
                newFeaturerId = dbTwinClassFieldEntity.getFieldTyperFeaturerId(); // only params where changed
        }
        if (!MapUtils.areEqual(dbTwinClassFieldEntity.getFieldTyperParams(), newFeaturerParams)) {
            newFieldTyperFeaturer = featurerService.checkValid(newFeaturerId, newFeaturerParams, FieldTyper.class);
            changesHelper.add(TwinClassFieldEntity.Fields.fieldTyperParams, dbTwinClassFieldEntity.getFieldTyperParams(), newFeaturerParams);
            dbTwinClassFieldEntity
                    .setFieldTyperParams(newFeaturerParams);
        }
        if (changesHelper.isChanged(TwinClassFieldEntity.Fields.fieldTyperFeaturerId, dbTwinClassFieldEntity.getFieldTyperFeaturerId(), newFeaturerId)) {
            if (twinService.areFieldsOfTwinClassFieldExists(dbTwinClassFieldEntity))
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_UPDATE_RESTRICTED, "class field can not change fieldtyper featurer, because some twins with fields of given class are already exist");
            if (newFieldTyperFeaturer == null)
                newFieldTyperFeaturer = featurerService.getFeaturerEntity(newFeaturerId);
            dbTwinClassFieldEntity
                    .setFieldTyperFeaturerId(newFieldTyperFeaturer.getId())
                    .setFieldTyperFeaturer(newFieldTyperFeaturer);
        }
    }

    public void updateTwinClassFieldName(TwinClassFieldEntity dbTwinClassFieldEntity, I18nEntity nameI18n, ChangesHelper changesHelper) throws ServiceException {
        if (nameI18n == null)
            return;
        if (dbTwinClassFieldEntity.getNameI18nId() != null)
            nameI18n.setId(dbTwinClassFieldEntity.getNameI18nId());
        i18nService.saveTranslations(I18nType.TWIN_CLASS_NAME, nameI18n);
        if (changesHelper.isChanged(TwinClassFieldEntity.Fields.nameI18nId, dbTwinClassFieldEntity.getNameI18nId(), nameI18n.getId()))
            dbTwinClassFieldEntity.setNameI18nId(nameI18n.getId());
    }

    public void updateTwinClassFieldDescription(TwinClassFieldEntity dbTwinClassFieldEntity, I18nEntity descriptionI18n, ChangesHelper changesHelper) throws ServiceException {
        if (descriptionI18n == null)
            return;
        if (dbTwinClassFieldEntity.getDescriptionI18nId() != null)
            descriptionI18n.setId(dbTwinClassFieldEntity.getDescriptionI18nId());
        i18nService.saveTranslations(I18nType.TWIN_CLASS_NAME, descriptionI18n);
        if (changesHelper.isChanged(TwinClassFieldEntity.Fields.descriptionI18nId, dbTwinClassFieldEntity.getDescriptionI18nId(), descriptionI18n.getId()))
            dbTwinClassFieldEntity.setDescriptionI18nId(descriptionI18n.getId());
    }

    public void updateTwinClassFieldViewPermission(TwinClassFieldEntity dbTwinClassFieldEntity, UUID newViewPermissionId, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinClassFieldEntity.Fields.viewPermissionId, dbTwinClassFieldEntity.getViewPermissionId(), newViewPermissionId))
            return;
        dbTwinClassFieldEntity.setViewPermissionId(UuidUtils.nullifyIfNecessary(newViewPermissionId));
    }

    public void updateTwinClassFieldEditPermission(TwinClassFieldEntity dbTwinClassFieldEntity, UUID newEditPermissionId, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinClassFieldEntity.Fields.editPermissionId, dbTwinClassFieldEntity.getEditPermissionId(), newEditPermissionId))
            return;
        dbTwinClassFieldEntity.setEditPermissionId(UuidUtils.nullifyIfNecessary(newEditPermissionId));
    }

    public void updateTwinClassFieldRequiredFlag(TwinClassFieldEntity dbTwinClassFieldEntity, Boolean newRequiredFlag, ChangesHelper changesHelper) throws ServiceException {
        if (newRequiredFlag == null || !changesHelper.isChanged(TwinClassFieldEntity.Fields.required, dbTwinClassFieldEntity.getRequired(), newRequiredFlag))
            return;
        dbTwinClassFieldEntity.setRequired(newRequiredFlag);
    }
}
