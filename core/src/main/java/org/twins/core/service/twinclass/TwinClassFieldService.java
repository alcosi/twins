package org.twins.core.service.twinclass;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.CacheEvictCollector;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.*;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerRepository;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.permission.PermissionRepository;
import org.twins.core.dao.twinclass.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.TwinSort;
import org.twins.core.domain.twinclass.TwinClassFieldSave;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldinitializer.FieldInitializer;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperDateTime;
import org.twins.core.featurer.fieldtyper.FieldTyperLink;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.twin.sorter.TwinSorter;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
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
    @Lazy
    private final FeaturerService featurerService;
    @Lazy
    private final AuthService authService;

    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private TwinClassRepository twinClassRepository;
    @Autowired
    private TwinClassFieldRuleMapService twinClassFieldRuleMapService;

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
                featurerService.prepareForStore(entity.getFieldTyperFeaturerId(), entity.getFieldTyperParams());
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

    public void loadTwinClassFields(TwinClassEntity twinClassEntity) {
        loadTwinClassFields(Collections.singleton(twinClassEntity));
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
        forClasses.remove(SystemEntityService.TWIN_CLASS_GLOBAL_ANCESTOR);
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

    public void loadTwinClassFieldsForTwinSorts(Collection<TwinSort> twinSorts) {
        if (twinSorts == null || twinSorts.isEmpty()) return;

        Set<UUID> fieldIdsToLoad = new HashSet<>();
        for (TwinSort twinSort : twinSorts)
            if (twinSort.getTwinClassField() == null && twinSort.getTwinClassFieldId() != null)
                fieldIdsToLoad.add(twinSort.getTwinClassFieldId());

        if (fieldIdsToLoad.isEmpty()) return;

        Kit<TwinClassFieldEntity, UUID> loadedFields = new Kit<>(twinClassFieldRepository.findByIdIn(new ArrayList<>(fieldIdsToLoad)), TwinClassFieldEntity::getId);
        for (TwinSort twinSort : twinSorts)
            if (twinSort.getTwinClassField() == null)
                twinSort.setTwinClassField(loadedFields.get(twinSort.getTwinClassFieldId()));
    }


    public void loadFields(Collection<TwinAttachmentEntity> attachments) throws ServiceException {
        KitGrouped<TwinAttachmentEntity, UUID, UUID> needLoad = new KitGrouped<>(TwinAttachmentEntity::getId, TwinAttachmentEntity::getTwinClassFieldId);
        for (TwinAttachmentEntity attachmentEntity : attachments) {
            if (attachmentEntity.getTwinClassFieldId() != null && attachmentEntity.getTwinClassField() == null)
                needLoad.add(attachmentEntity);
        }
        if (needLoad.isEmpty())
            return;
        var twinClassFieldKit = findEntitiesSafe(needLoad.getGroupedMap().keySet());
        for (var entry : needLoad.getGroupedMap().entrySet()) {
            for (var attachmentEntity : entry.getValue()) {
                attachmentEntity.setTwinClassField(twinClassFieldKit.get(attachmentEntity.getTwinClassFieldId()));
            }
        }
    }

    public void loadFieldStorages(TwinClassEntity twinClassEntity) throws ServiceException {
        if (twinClassEntity.getFieldStorageSet() != null) {
            return;
        }
        twinClassEntity.setFieldStorageSet(new HashSet<>());
        for (TwinClassFieldEntity twinClassField : twinClassEntity.getTwinClassFieldKit().getCollection()) {
            FieldTyper fieldTyper = featurerService.getFeaturer(twinClassField.getFieldTyperFeaturerId(), FieldTyper.class);
            //storage hashCode is important here, this will help to do bulk load
            TwinFieldStorage storage = fieldTyper.getStorage(twinClassField);
            twinClassEntity.getFieldStorageSet().add(storage);
        }
    }

    public void loadRuleFields(TwinClassFieldRuleEntity ruleEntity) throws ServiceException {
        loadRuleFields(Collections.singleton(ruleEntity));
    }

    public void loadRuleFields(Collection<TwinClassFieldRuleEntity> ruleEntities) throws ServiceException {
        Kit<TwinClassFieldRuleEntity, UUID> needLoad = new Kit<>(TwinClassFieldRuleEntity::getId);
        ruleEntities.stream()
                .filter(rule -> rule.getFieldKit() == null)
                .forEach(needLoad::add);

        if (needLoad.isEmpty()) return;

        KitGrouped<TwinClassFieldRuleMapEntity, UUID, UUID> ruleMaps = new KitGrouped<>(
                twinClassFieldRuleMapService.findByTwinClassFieldRuleIdIn(needLoad.getIdSet()),
                TwinClassFieldRuleMapEntity::getId,
                TwinClassFieldRuleMapEntity::getTwinClassFieldRuleId
        );

        if (ruleMaps.isEmpty()) {
            needLoad.forEach(rule -> rule.setFieldKit(Kit.EMPTY));
            return;
        }

        for (TwinClassFieldRuleEntity ruleEntity : needLoad) {
            if (ruleMaps.containsGroupedKey(ruleEntity.getId())) {
                List<TwinClassFieldEntity> fields = ruleMaps.getGrouped(ruleEntity.getId()).stream()
                        .map(TwinClassFieldRuleMapEntity::getTwinClassField)
                        .collect(Collectors.toList());
                ruleEntity.setFieldKit(new Kit<>(fields, TwinClassFieldEntity::getId));
            } else {
                ruleEntity.setFieldKit(Kit.EMPTY);
            }
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

    public List<TwinClassFieldEntity> findByTwinClassIdAndKeysIncludeParents(UUID twinClassId, Set<String> setKeys) {
        TwinClassEntity twinClass = twinClassRepository.findById(twinClassId).orElse(null);
        return findByTwinClassIdAndKeysIncludeParents(twinClass, setKeys);
    }

    public List<TwinClassFieldEntity> findByTwinClassIdAndKeysIncludeParents(TwinClassEntity twinClass, Set<String> setKeys) {
        List<TwinClassFieldEntity> twinClassFieldList = null;
        if (twinClass != null) {
            Set<UUID> extendedClassIds = twinClass.getExtendedClassIdSet();
            twinClassFieldList = twinClassFieldRepository.findByKeyInAndTwinClassIdIn(setKeys, extendedClassIds);
        }
        return twinClassFieldList;
    }

    public List<TwinClassFieldEntity> findByTwinClassIdAndIdsIncludeParents(UUID twinClassId, Set<UUID> setIds) {
        TwinClassEntity twinClass = twinClassRepository.findById(twinClassId).orElse(null);
        return findByTwinClassIdAndIdsIncludeParents(twinClass, setIds);
    }

    public List<TwinClassFieldEntity> findByTwinClassIdAndIdsIncludeParents(TwinClassEntity twinClass, Set<UUID> setIds) {
        List<TwinClassFieldEntity> twinClassFieldList = null;
        if (twinClass != null) {
            Set<UUID> extendedClassIds = twinClass.getExtendedClassIdSet();
            twinClassFieldList = twinClassFieldRepository.findByIdInAndTwinClassIdIn(setIds, extendedClassIds);
        }
        return twinClassFieldList;
    }

    public TwinClassFieldEntity findByTwinClassFieldId(UUID twinClassFieldId) {
        return twinClassFieldRepository.findById(twinClassFieldId).orElse(null);
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
                .setFieldTyperFeaturerId(srcFieldEntity.getFieldTyperFeaturerId())
                .setFieldTyperParams(srcFieldEntity.getFieldTyperParams())
                .setTwinSorterFeaturerId(srcFieldEntity.getTwinSorterFeaturerId())
                .setTwinSorterParams(srcFieldEntity.getTwinSorterParams())
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

    public Kit<TwinClassFieldEntity, UUID> getBaseFieldsKit() {
        return new Kit<>(
                twinClassFieldRepository.findBaseFields(SystemEntityService.TWIN_CLASS_GLOBAL_ANCESTOR),
                TwinClassFieldEntity::getId
        );
    }

    public TwinClassFieldEntity getBaseField(UUID twinClassFieldId) {
        for (var baseField : twinClassFieldRepository.findBaseFields(SystemEntityService.TWIN_CLASS_GLOBAL_ANCESTOR)) {
            if (baseField.getId().equals(twinClassFieldId)) {
                return baseField;
            }
        }
        return null;
    }

    public TwinClassFieldEntity getTwinClassFieldOrNull(TwinClassEntity twinClass, UUID twinClassFieldId) {
        if (SystemEntityService.isSystemField(twinClassFieldId))
            return getBaseField(twinClassFieldId);
        loadTwinClassFields(twinClass);
        return twinClass.getTwinClassFieldKit().get(twinClassFieldId);
    }


    public static final HashMap<String, String> SIMPLE_FIELD_PARAMS = new HashMap<>() {{
        put("regexp", ".*");
    }};

    @Transactional(rollbackFor = Throwable.class)
    public TwinClassFieldEntity createFields(TwinClassFieldSave twinClassFieldSave) throws ServiceException {
        return createFields(List.of(twinClassFieldSave)).getFirst();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassFieldEntity> createFields(List<TwinClassFieldSave> twinClassFieldSaves) throws ServiceException {
        if (CollectionUtils.isEmpty(twinClassFieldSaves)) {
            return Collections.emptyList();
        }

        final ApiUser apiUser = authService.getApiUser();
        final List<TwinClassFieldEntity> fieldsToCreate = new ArrayList<>(twinClassFieldSaves.size());
        final CacheEvictCollector cacheEvictCollector = new CacheEvictCollector();

        for (TwinClassFieldSave save : twinClassFieldSaves) {
            TwinClassFieldEntity field = save.getField();

            String fieldKey = KeyUtils.lowerCaseNullSafe(field.getKey(), ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT);
            field.setKey(fieldKey);

            if (twinClassFieldRepository.existsByKeyAndTwinClassId(fieldKey, field.getTwinClassId())) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_INCORRECT,
                        "Field key already exists: [" + fieldKey + "] for twin class [" + field.getTwinClassId() + "]");
            }

            if (field.getTwinClassId() == null) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_ID_UNKNOWN);
            }

            if (field.getViewPermissionId() != null &&
                    !permissionRepository.existsByIdAndPermissionGroup_DomainId(field.getViewPermissionId(), apiUser.getDomainId())) {
                throw new ServiceException(ErrorCodeTwins.PERMISSION_ID_UNKNOWN, "unknown view permission id");
            }
            if (field.getEditPermissionId() != null &&
                    !permissionRepository.existsByIdAndPermissionGroup_DomainId(field.getEditPermissionId(), apiUser.getDomainId())) {
                throw new ServiceException(ErrorCodeTwins.PERMISSION_ID_UNKNOWN, "unknown edit permission id");
            }

            if (field.getFieldTyperFeaturerId() != null) {
                featurerService.checkValid(field.getFieldTyperFeaturerId(), field.getFieldTyperParams(), FieldTyper.class);
                featurerService.prepareForStore(field.getFieldTyperFeaturerId(), field.getFieldTyperParams());
            } else {
                field
                        .setFieldTyperFeaturerId(1301)
                        .setFieldTyperParams(SIMPLE_FIELD_PARAMS);
            }

            if (field.getTwinSorterFeaturerId() != null) {
                featurerService.checkValid(field.getTwinSorterFeaturerId(), field.getTwinSorterParams(), TwinSorter.class);
                featurerService.prepareForStore(field.getTwinSorterFeaturerId(), field.getTwinSorterParams());
            } else {
                field
                        .setTwinSorterFeaturerId(FeaturerTwins.ID_4101)
                        .setTwinSorterParams(null);
            }

            if (field.getFieldInitializerFeaturerId() != null) {
                featurerService.checkValid(field.getFieldInitializerFeaturerId(), field.getFieldInitializerParams(), FieldInitializer.class);
                featurerService.prepareForStore(field.getFieldInitializerFeaturerId(), field.getFieldInitializerParams());
            } else {
                field
                        .setFieldInitializerFeaturerId(FeaturerTwins.ID_5301)
                        .setFieldInitializerParams(null);
            }

            if (field.getSystem() == null) {
                field.setSystem(false);
            }
            field
                    .setDependentField(false)
                    .setHasDependentFields(false)
                    .setProjectionField(false)
                    .setHasProjectedFields(false)
                    .setNameI18nId(i18nService.createI18nAndTranslations(I18nType.TWIN_CLASS_FIELD_NAME, save.getNameI18n()).getId())
                    .setDescriptionI18nId(i18nService.createI18nAndTranslations(I18nType.TWIN_CLASS_FIELD_DESCRIPTION, save.getDescriptionI18n()).getId())
                    .setFeValidationErrorI18nId(i18nService.createI18nAndTranslations(I18nType.TWIN_CLASS_FIELD_FE_VALIDATION_ERROR, save.getFeValidationErrorI18n()).getId())
                    .setBeValidationErrorI18nId(i18nService.createI18nAndTranslations(I18nType.TWIN_CLASS_FIELD_BE_VALIDATION_ERROR, save.getBeValidationErrorI18n()).getId());

            validateEntityAndThrow(field, EntitySmartService.EntityValidateMode.beforeSave);
            fieldsToCreate.add(field);

            cacheEvictCollector
                    .add(field.getTwinClassId(),
                            TwinClassRepository.CACHE_TWIN_CLASS_BY_ID,
                            TwinClassEntity.class.getSimpleName())
                    .add(TwinClassFieldRepository.CACHE_TWIN_CLASS_FIELD_BY_TWIN_CLASS_ID_IN);
        }

        Iterable<TwinClassFieldEntity> savedEntities = entitySmartService.saveAllAndLog(
                fieldsToCreate,
                twinClassFieldRepository
        );

        List<TwinClassFieldEntity> result = new ArrayList<>();
        for (var savedEntity : savedEntities) {
            if (savedEntity.getTwinClass() != null)
                savedEntity.getTwinClass().setTwinClassFieldKit(null); //invalidating kit cache
            result.add(savedEntity);
        }
        CacheUtils.evictCache(cacheManager, cacheEvictCollector);

        return result;
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
    public TwinClassFieldEntity updateFields(TwinClassFieldSave twinClassFieldSave) throws ServiceException {
        return updateFields(List.of(twinClassFieldSave)).getFirst();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassFieldEntity> updateFields(List<TwinClassFieldSave> twinClassFieldSaves) throws ServiceException {
        if (CollectionUtils.isEmpty(twinClassFieldSaves)) {
            return Collections.emptyList();
        }

        Kit<TwinClassFieldEntity, UUID> dbFieldsKit = findEntitiesSafe(
                twinClassFieldSaves.stream()
                        .map(s -> s.getField().getId())
                        .collect(Collectors.toList())
        );

        ChangesHelperMulti<TwinClassFieldEntity> changes = new ChangesHelperMulti<>();
        CacheEvictCollector cacheEvictCollector = new CacheEvictCollector();

        List<TwinClassFieldEntity> allEntities = dbFieldsKit.getList();

        for (TwinClassFieldSave save : twinClassFieldSaves) {
            TwinClassFieldEntity dbField = dbFieldsKit.get(save.getField().getId());
            if (dbField == null) {
                throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN,
                        "TwinClassField with id: [" + save.getField().getId() + "] not found");
            }

            ChangesHelper changesHelper = new ChangesHelper();

            updateTwinClassFieldTwinClass(dbField, save.getField().getTwinClassId(), changesHelper);
            updateTwinClassField_FieldTyperFeaturerId(dbField, save.getField().getFieldTyperFeaturerId(), save.getField().getFieldTyperParams(), changesHelper);
            updateTwinClassField_TwinSorterFeaturerId(dbField, save.getField().getTwinSorterFeaturerId(), save.getField().getTwinSorterParams(), changesHelper);
            i18nService.updateI18nFieldForEntity(save.getNameI18n(), I18nType.TWIN_CLASS_FIELD_NAME, dbField, TwinClassFieldEntity::getNameI18nId, TwinClassFieldEntity::setNameI18nId, TwinClassFieldEntity.Fields.nameI18nId, changesHelper);
            i18nService.updateI18nFieldForEntity(save.getDescriptionI18n(), I18nType.TWIN_CLASS_FIELD_DESCRIPTION, dbField, TwinClassFieldEntity::getDescriptionI18nId, TwinClassFieldEntity::setDescriptionI18nId, TwinClassFieldEntity.Fields.descriptionI18nId, changesHelper);
            i18nService.updateI18nFieldForEntity(save.getFeValidationErrorI18n(), I18nType.TWIN_CLASS_FIELD_FE_VALIDATION_ERROR, dbField, TwinClassFieldEntity::getFeValidationErrorI18nId, TwinClassFieldEntity::setFeValidationErrorI18nId, TwinClassFieldEntity.Fields.feValidationErrorI18nId, changesHelper);
            i18nService.updateI18nFieldForEntity(save.getBeValidationErrorI18n(), I18nType.TWIN_CLASS_FIELD_BE_VALIDATION_ERROR, dbField, TwinClassFieldEntity::getBeValidationErrorI18nId, TwinClassFieldEntity::setBeValidationErrorI18nId, TwinClassFieldEntity.Fields.beValidationErrorI18nId, changesHelper);
            updateTwinClassFieldViewPermission(dbField, save.getField().getViewPermissionId(), changesHelper);
            updateTwinClassFieldEditPermission(dbField, save.getField().getEditPermissionId(), changesHelper);
            updateTwinClassFieldRequiredFlag(dbField, save.getField().getRequired(), changesHelper);
            updateEntityFieldByEntity(save.getField(), dbField, TwinClassFieldEntity::getSystem, TwinClassFieldEntity::setSystem, TwinClassFieldEntity.Fields.system, changesHelper);
            updateEntityFieldByEntity(save.getField(), dbField, TwinClassFieldEntity::getExternalId, TwinClassFieldEntity::setExternalId, TwinClassFieldEntity.Fields.externalId, changesHelper);
            updateEntityFieldByEntity(save.getField(), dbField, TwinClassFieldEntity::getExternalProperties, TwinClassFieldEntity::setExternalProperties, TwinClassFieldEntity.Fields.externalProperties, changesHelper);
            updateEntityFieldByEntity(save.getField(), dbField, TwinClassFieldEntity::getOrder, TwinClassFieldEntity::setOrder, TwinClassFieldEntity.Fields.order, changesHelper);

            if (changesHelper.hasChanges()) {
                changes.add(dbField, changesHelper);
                cacheEvictCollector.add(dbField.getTwinClassId(),
                        TwinClassRepository.CACHE_TWIN_CLASS_BY_ID,
                        TwinClassEntity.class.getSimpleName());
            }
        }


        if (!changes.entrySet().isEmpty()) {
            updateSafe(changes);

            cacheEvictCollector.add(
                    TwinClassFieldRepository.CACHE_TWIN_CLASS_FIELD_BY_ID_IN,
                    TwinClassFieldRepository.CACHE_TWIN_CLASS_FIELD_BY_TWIN_CLASS_ID_IN,
                    TwinClassFieldRepository.CACHE_TWIN_CLASS_FIELD_BY_KEY_AND_TWIN_CLASS_ID_IN,
                    CACHE_TWIN_CLASS_FIELD_FOR_LINK,
                    TwinClassFieldRepository.CACHE_TWIN_CLASS_FIELD_BY_TWIN_CLASS_AND_KEY,
                    TwinClassFieldRepository.CACHE_TWIN_CLASS_FIELD_BY_TWIN_CLASS_AND_PARENT_KEY);

            CacheUtils.evictCache(cacheManager, cacheEvictCollector);
        }

        return allEntities;
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
        if (newFeaturerId == null || newFeaturerId == 0) {
            if (MapUtils.isEmpty(newFeaturerParams))
                return; //nothing was changed
            else
                newFeaturerId = dbTwinClassFieldEntity.getFieldTyperFeaturerId(); // only params where changed
        }
        if (changesHelper.isChanged(TwinClassFieldEntity.Fields.fieldTyperFeaturerId, dbTwinClassFieldEntity.getFieldTyperFeaturerId(), newFeaturerId)) {
            if (twinService.areFieldsOfTwinClassFieldExists(dbTwinClassFieldEntity))
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_UPDATE_RESTRICTED, "class field can not change fieldtyper featurer, because some twins with fields of given class are already exist");
            featurerService.checkValid(newFeaturerId, newFeaturerParams, FieldTyper.class);
            dbTwinClassFieldEntity
                    .setFieldTyperFeaturerId(newFeaturerId);
        }
        featurerService.prepareForStore(newFeaturerId, newFeaturerParams);
        if (!MapUtils.areEqual(dbTwinClassFieldEntity.getFieldTyperParams(), newFeaturerParams)) {
            changesHelper.add(TwinClassFieldEntity.Fields.fieldTyperParams, dbTwinClassFieldEntity.getFieldTyperParams(), newFeaturerParams);
            dbTwinClassFieldEntity
                    .setFieldTyperParams(newFeaturerParams);
        }
    }

    public void updateTwinClassField_TwinSorterFeaturerId(TwinClassFieldEntity dbTwinClassFieldEntity, Integer newFeaturerId, HashMap<String, String> newFeaturerParams, ChangesHelper changesHelper) throws ServiceException {
        if (newFeaturerId == null || newFeaturerId == 0) {
            if (MapUtils.isEmpty(newFeaturerParams))
                return; //nothing was changed
            else
                newFeaturerId = dbTwinClassFieldEntity.getTwinSorterFeaturerId(); // only params where changed
        }
        if (changesHelper.isChanged(TwinClassFieldEntity.Fields.twinSorterFeaturerId, dbTwinClassFieldEntity.getFieldTyperFeaturerId(), newFeaturerId)) {
            featurerService.checkValid(newFeaturerId, newFeaturerParams, TwinSorter.class);
            dbTwinClassFieldEntity
                    .setTwinSorterFeaturerId(newFeaturerId);
        }
        featurerService.prepareForStore(newFeaturerId, newFeaturerParams);
        if (!MapUtils.areEqual(dbTwinClassFieldEntity.getTwinSorterParams(), newFeaturerParams)) {
            changesHelper.add(TwinClassFieldEntity.Fields.twinSorterParams, dbTwinClassFieldEntity.getTwinSorterParams(), newFeaturerParams);
            dbTwinClassFieldEntity
                    .setTwinSorterParams(newFeaturerParams);
        }
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

    public void updateTwinClassFieldSystemFlag(TwinClassFieldEntity dbTwinClassFieldEntity, Boolean newSystemFlag, ChangesHelper changesHelper) throws ServiceException {
        if (newSystemFlag == null || !changesHelper.isChanged(TwinClassFieldEntity.Fields.system, dbTwinClassFieldEntity.getRequired(), newSystemFlag))
            return;
        dbTwinClassFieldEntity.setRequired(newSystemFlag);
    }

    public String getDateFieldPattern(TwinClassFieldEntity twinClassField) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(twinClassField.getFieldTyperFeaturerId(), FieldTyper.class);
        if (!(fieldTyper instanceof FieldTyperDateTime fieldTyperDateTime))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, twinClassField.logNormal() + " is not datetime");
        Properties properties = featurerService.extractProperties(fieldTyper, twinClassField.getFieldTyperParams());
        return fieldTyperDateTime.getPattern(properties);
    }
}
