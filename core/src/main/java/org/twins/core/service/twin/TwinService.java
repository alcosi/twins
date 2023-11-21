package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.twin.*;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRepository;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.AttachmentAUD;
import org.twins.core.domain.EntitiesChangesCollector;
import org.twins.core.domain.TwinLinkAUD;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.EntityChangesService;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinflow.TwinflowService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinService extends EntitySecureFindServiceImpl<TwinEntity> {
    final TwinRepository twinRepository;
    final TwinFieldRepository twinFieldRepository;
    final TwinFieldDataListRepository twinFieldDataListRepository;
    final TwinClassFieldRepository twinClassFieldRepository;
    final TwinBusinessAccountAliasRepository twinBusinessAccountAliasRepository;
    final TwinDomainAliasRepository twinDomainAliasRepository;
    final TwinClassFieldService twinClassFieldService;
    final EntityManager entityManager;
    final EntitySmartService entitySmartService;
    final TwinflowService twinflowService;
    final TwinClassService twinClassService;
    @Lazy
    final TwinHeadService twinHeadService;
    final TwinStatusService twinStatusService;
    final FeaturerService featurerService;
    final AttachmentService attachmentService;
    @Lazy
    final TwinLinkService twinLinkService;
    @Lazy
    final AuthService authService;
    @Lazy
    final SystemEntityService systemEntityService;
    final EntityChangesService entityChangesService;

    @Override
    public CrudRepository<TwinEntity, UUID> entityRepository() {
        return twinRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (entity.getTwinClass().getDomainId() != null //system twinClasses can be out of any domain
                && !entity.getTwinClass().getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed for " + apiUser.getDomain().easyLog(EasyLoggable.Level.NORMAL));
            return true;
        }
        if (entity.getTwinClass().getOwnerType().isBusinessAccountLevel()
                && !entity.getOwnerBusinessAccountId().equals(apiUser.getBusinessAccount().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed for " + apiUser.getBusinessAccount().easyLog(EasyLoggable.Level.NORMAL));
            return true;
        }
        if (entity.getTwinClass().getOwnerType().isUserLevel()
                && !entity.getOwnerUserId().equals(apiUser.getUser().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed for " + apiUser.getUser().easyLog(EasyLoggable.Level.NORMAL));
            return true;
        }
        if (entity.getTwinClass().getOwnerType().isSystemLevel()) {
            if (systemEntityService.isTwinClassForUser(entity.getTwinClassId()))
                return false;  //todo check if entity.id is in domain businessAccount users scope. should be cached
            if (systemEntityService.isTwinClassForBusinessAccount(entity.getTwinClassId()))
                return false;  //todo check if entity.id is in domain businessAccount users scope. should be cached
            return false;
        }
        //todo check permission schema
        return false;
    }

    @Override
    public boolean validateEntity(TwinEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getTwinClassId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinClassId");
        if (entity.getTwinStatusId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinStatusId");
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getTwinClass() == null)
                    entity.setTwinClass(twinClassService.findEntitySafe(entity.getTwinClassId()));
                if (entity.getTwinStatus() == null)
                    entity.setTwinStatus(twinStatusService.findEntitySafe(entity.getTwinStatusId()));
            default:
                if (!twinClassService.isInstanceOf(entity.getTwinClassId(), entity.getTwinStatus().getTwinClassId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect twinStatusId[" + entity.getTwinStatusId() + "]");
        }
        return true;
    }

    public TwinEntity findTwinByAlias(ApiUser apiUser, String twinAlias) throws ServiceException {
        if (apiUser.getBusinessAccount() != null) {
            TwinBusinessAccountAliasEntity twinBusinessAccountAliasEntity = twinBusinessAccountAliasRepository.findByBusinessAccountIdAndAlias(apiUser.getBusinessAccount().getId(), twinAlias);
            if (twinBusinessAccountAliasEntity != null)
                return twinBusinessAccountAliasEntity.getTwin();
        }
        TwinDomainAliasEntity twinDomainAliasEntity = twinDomainAliasRepository.findByDomainIdAndAlias(apiUser.getDomain().getId(), twinAlias);
        if (twinDomainAliasEntity == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_ALIAS_UNKNOWN, "unknown twin alias[" + twinAlias + "]");
        return twinDomainAliasEntity.getTwin();
    }

    public List<TwinFieldEntity> findTwinFields(UUID twinId) {
        return twinFieldRepository.findByTwinId(twinId);
    }

    public TwinFieldEntity findTwinField(UUID twinFieldId) throws ServiceException {
        return entitySmartService.findById(twinFieldId, twinFieldRepository, EntitySmartService.FindMode.ifEmptyThrows);
    }

    public TwinFieldEntity findTwinFieldIncludeMissing(UUID twinId, String fieldKey) throws ServiceException {
        TwinFieldEntity twinFieldEntity = twinFieldRepository.findByTwinIdAndTwinClassField_Key(twinId, fieldKey);
        if (twinFieldEntity != null)
            return twinFieldEntity;
        TwinEntity twinEntity = entitySmartService.findById(twinId, twinRepository, EntitySmartService.FindMode.ifEmptyThrows);
        TwinClassFieldEntity twinClassField = twinClassFieldService.findByTwinClassIdAndKeyIncludeParent(twinEntity.getTwinClassId(), fieldKey);
        if (twinClassField == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_UNKNOWN, "unknown fieldKey[" + fieldKey + "] for twin["
                    + twinId + "] of class[" + twinEntity.getTwinClass().getKey() + " : " + twinEntity.getTwinClassId() + "]");
        twinFieldEntity = new TwinFieldEntity()
                .setTwinClassField(twinClassField)
                .setTwinClassFieldId(twinClassField.getId())
                .setTwin(twinEntity)
                .setTwinId(twinEntity.getId())
                .setValue("");
        return twinFieldEntity;
    }

    public TwinFieldEntity findTwinFieldIncludeMissing(UUID twinId, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        TwinFieldEntity twinFieldEntity = twinFieldRepository.findByTwinIdAndTwinClassFieldId(twinId, twinClassFieldEntity.getId());
        if (twinFieldEntity != null)
            return twinFieldEntity;
        TwinEntity twinEntity = findEntitySafe(twinId);
        twinFieldEntity = new TwinFieldEntity()
                .setTwinClassField(twinClassFieldEntity)
                .setTwinClassFieldId(twinClassFieldEntity.getId())
                .setTwin(twinEntity)
                .setTwinId(twinEntity.getId())
                .setValue("");
        return twinFieldEntity;
    }

    public List<TwinFieldEntity> findTwinFieldsIncludeMissing(TwinEntity twinEntity) {
        Map<UUID, TwinFieldEntity> twinFieldEntityMap = twinFieldRepository.findByTwinId(twinEntity.getId()).stream().collect(Collectors.toMap(TwinFieldEntity::getTwinClassFieldId, Function.identity()));
        List<TwinClassFieldEntity> twinFieldClassEntityList = twinClassFieldService.findTwinClassFieldsIncludeParent(twinEntity.getTwinClass());
        List<TwinFieldEntity> ret = new ArrayList<>();
        for (TwinClassFieldEntity twinClassField : twinFieldClassEntityList) {
            if (twinFieldEntityMap.containsKey(twinClassField.getId()))
                ret.add(twinFieldEntityMap.get(twinClassField.getId()));
            else
                ret.add(createTwinFieldEntity(twinEntity, twinClassField, ""));
        }
        return ret;
    }

    public TwinFieldEntity createTwinFieldEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity, String value) {
        return new TwinFieldEntity()
                .setTwinClassField(twinClassFieldEntity)
                .setTwinClassFieldId(twinClassFieldEntity.getId())
                .setTwin(twinEntity)
                .setTwinId(twinEntity.getId())
                .setValue(value);
    }

    public TwinFieldEntity updateField(UUID twinFieldId, FieldValue fieldValue) throws ServiceException {
        return updateField(entitySmartService.findById(twinFieldId, twinFieldRepository, EntitySmartService.FindMode.ifEmptyThrows), fieldValue);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinCreateResult createTwin(ApiUser apiUser, TwinEntity twinEntity, List<FieldValue> values, List<TwinAttachmentEntity> attachmentEntityList, List<TwinLinkEntity> linksEntityList) throws ServiceException {
        TwinflowEntity twinflowEntity = twinflowService.loadTwinflow(twinEntity.getTwinClass());
        TwinClassEntity twinClassEntity = twinClassService.findEntity(twinEntity.getTwinClassId(), EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
        twinEntity
                .setTwinClass(twinClassEntity)
                .setHeadTwinId(twinHeadService.checkHeadTwinAllowedForClass(twinEntity.getHeadTwinId(), twinClassEntity))
                .setTwinStatusId(twinflowEntity.getInitialTwinStatusId())
                .setTwinStatus(twinflowEntity.getInitialTwinStatus());
        fillOwner(twinEntity, apiUser.getBusinessAccount(), apiUser.getUser());
        twinEntity = saveTwin(twinEntity);
        saveTwinFields(twinEntity, values);
        if (CollectionUtils.isNotEmpty(attachmentEntityList)) {
            attachmentService.addAttachments(twinEntity.getId(), apiUser.getUser(), attachmentEntityList);
        }
        if (CollectionUtils.isNotEmpty(linksEntityList))
            twinLinkService.addLinks(twinEntity, linksEntityList);
        twinflowService.runTwinStatusTransitionTriggers(twinEntity, null, twinEntity.getTwinStatus());
        return new TwinCreateResult()
                .setCreatedTwin(twinEntity)
                .setBusinessAccountAliasEntityList(createTwinBusinessAccountAliases(twinEntity))
                .setDomainAliasEntityList(createTwinDomainAliases(twinEntity));
    }

    public TwinEntity fillOwner(TwinEntity twinEntity, BusinessAccountEntity businessAccountEntity, UserEntity userEntity) throws ServiceException {
        TwinClassEntity twinClassEntity = twinEntity.getTwinClass();
        switch (twinClassEntity.getOwnerType()) {
            case DOMAIN:
                //twin will not be owned neither businessAccount, neither user
                break;
            case BUSINESS_ACCOUNT:
            case DOMAIN_BUSINESS_ACCOUNT:
                if (businessAccountEntity == null)
                    throw new ServiceException(ErrorCodeTwins.BUSINESS_ACCOUNT_UNKNOWN, twinClassEntity.easyLog(EasyLoggable.Level.NORMAL) + " can not be created without businessAccount owner");
                twinEntity
                        .setOwnerBusinessAccountId(businessAccountEntity.getId())
                        .setOwnerBusinessAccount(businessAccountEntity)
                        .setOwnerUserId(null);
                break;
            case USER:
            case DOMAIN_USER:
                if (userEntity == null)
                    throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, twinClassEntity.easyLog(EasyLoggable.Level.NORMAL) + " can not be created without user owner");
                twinEntity
                        .setOwnerUserId(userEntity.getId())
                        .setOwnerUser(userEntity)
                        .setOwnerBusinessAccountId(null);
        }
        return twinEntity;
    }

    @Transactional
    public TwinEntity saveTwin(TwinEntity twinEntity) throws ServiceException {
        twinEntity
                .setCreatedAt(Timestamp.from(Instant.now()));
        validateEntityAndThrow(twinEntity, EntitySmartService.EntityValidateMode.beforeSave);
        return entitySmartService.save(twinEntity, twinRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
    }

    @Transactional
    public void saveTwinFields(TwinEntity twinEntity, List<FieldValue> values) throws ServiceException {
        Map<UUID, FieldValue> twinClassFieldValuesMap = values.stream().collect(Collectors.toMap(f -> f.getTwinClassField().getId(), Function.identity()));
        List<TwinClassFieldEntity> twinClassFieldEntityList = twinClassFieldService.findTwinClassFieldsIncludeParent(twinEntity.getTwinClass());
        TwinFieldEntity twinFieldEntity;
        FieldValue fieldValue;
        EntitiesChangesCollector entitiesChangesCollector = new EntitiesChangesCollector(); //all fields will be saved at once, in one transaction
        for (TwinClassFieldEntity twinClassFieldEntity : twinClassFieldEntityList) {
            fieldValue = twinClassFieldValuesMap.get(twinClassFieldEntity.getId());
            if (fieldValue == null)
                if (twinClassFieldEntity.isRequired())
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinClassFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " is required");
                else
                    continue;
            twinFieldEntity = createTwinFieldEntity(twinEntity, twinClassFieldEntity, null);
            var fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturer(), FieldTyper.class);
            fieldTyper.serializeValue(twinFieldEntity, fieldValue, entitiesChangesCollector);
        }
        entityChangesService.saveEntities(entitiesChangesCollector);
    }

    @Transactional
    public void updateTwin(TwinEntity updateTwinEntity, TwinEntity dbTwinEntity, List<FieldValue> values, AttachmentAUD attachmentManage, TwinLinkAUD twinLinkManage) throws ServiceException {
        ChangesHelper changesHelper = new ChangesHelper();
        if (changesHelper.isChanged("headTwinId", dbTwinEntity.getHeadTwinId(), updateTwinEntity.getHeadTwinId())) {
            dbTwinEntity.setHeadTwinId(twinHeadService.checkHeadTwinAllowedForClass(updateTwinEntity.getHeadTwinId(), dbTwinEntity.getTwinClass()));
        }
        if (changesHelper.isChanged("name", dbTwinEntity.getName(), updateTwinEntity.getName())) {
            dbTwinEntity.setName(updateTwinEntity.getName());
        }
        if (changesHelper.isChanged("description", dbTwinEntity.getDescription(), updateTwinEntity.getDescription())) {
            dbTwinEntity.setDescription(updateTwinEntity.getDescription());
        }
        if (changesHelper.isChanged("assignerUser", dbTwinEntity.getAssignerUserId(), updateTwinEntity.getAssignerUserId())) {
            dbTwinEntity.setAssignerUserId(updateTwinEntity.getAssignerUserId());
        }
        entitySmartService.saveAndLogChanges(dbTwinEntity, twinRepository, changesHelper);
        updateTwinFields(dbTwinEntity, values);
        if (CollectionUtils.isNotEmpty(attachmentManage.getAddEntityList())) {
            attachmentService.addAttachments(dbTwinEntity.getId(), dbTwinEntity.getCreatedByUser(), attachmentManage.getAddEntityList());
        }
        if (CollectionUtils.isNotEmpty(attachmentManage.getUpdateEntityList())) {
            attachmentService.updateAttachments(attachmentManage.getUpdateEntityList());
        }
        if (CollectionUtils.isNotEmpty(attachmentManage.getDeleteUUIDList())) {
            attachmentService.deleteAttachments(dbTwinEntity.getId(), attachmentManage.getDeleteUUIDList());
        }
        if (CollectionUtils.isNotEmpty(twinLinkManage.getAddEntityList())) {
            twinLinkService.addLinks(dbTwinEntity, twinLinkManage.getAddEntityList());
        }
        if (CollectionUtils.isNotEmpty(twinLinkManage.getUpdateEntityList())) {
            twinLinkService.updateTwinLinks(dbTwinEntity, twinLinkManage.getUpdateEntityList());
        }
        if (CollectionUtils.isNotEmpty(twinLinkManage.getDeleteUUIDList())) {
            twinLinkService.deleteTwinLinks(dbTwinEntity.getId(), twinLinkManage.getDeleteUUIDList());
        }
    }

//    @Transactional
//    public void updateTwinFields(TwinEntity twinEntity, List<FieldValue> values) throws ServiceException {
//        List<TwinFieldEntity> twinFieldEntityList = new ArrayList<>();
//        TwinFieldEntity twinFieldEntity;
//        ChangesHelper twinChangesHelper = new ChangesHelper();
//        EntitiesChangesHelper entitiesChangesHelper = new EntitiesChangesHelper();
//        for (FieldValue fieldValue : values) {
//            ChangesHelper fieldChangesHelper = new ChangesHelper();
//            twinFieldEntity = findTwinFieldIncludeMissing(twinEntity.getId(), fieldValue.getTwinClassField());
//            var fieldTyper = featurerService.getFeaturer(twinFieldEntity.getTwinClassField().getFieldTyperFeaturer(), FieldTyper.class);
//            fieldTyper.serializeValue(twinFieldEntity, fieldValue, fieldChangesHelper);
//            if (fieldChangesHelper.hasChanges()) {
//                twinFieldEntityList.add(twinFieldEntity);
//                twinChangesHelper.addAll(fieldChangesHelper);
//            }
//        }
//        saveTwinFields(twinFieldEntityList, twinChangesHelper);
//    }

    @Transactional
    public void updateTwinFields(TwinEntity twinEntity, List<FieldValue> values) throws ServiceException {
        List<TwinFieldEntity> twinFieldEntityList = new ArrayList<>();
        TwinFieldEntity twinFieldEntity;
        EntitiesChangesCollector entitiesChangesCollector = new EntitiesChangesCollector();
        for (FieldValue fieldValue : values) {
            twinFieldEntity = findTwinFieldIncludeMissing(twinEntity.getId(), fieldValue.getTwinClassField());
            var fieldTyper = featurerService.getFeaturer(twinFieldEntity.getTwinClassField().getFieldTyperFeaturer(), FieldTyper.class);
            fieldTyper.serializeValue(twinFieldEntity, fieldValue, entitiesChangesCollector);
        }
        entityChangesService.saveEntities(entitiesChangesCollector);
    }

    public List<TwinBusinessAccountAliasEntity> createTwinBusinessAccountAliases(TwinEntity twinEntity) {
        if (twinEntity.getTwinClass().getOwnerType() != TwinClassEntity.OwnerType.DOMAIN_BUSINESS_ACCOUNT && twinEntity.getTwinClass().getOwnerType() != TwinClassEntity.OwnerType.DOMAIN_BUSINESS_ACCOUNT_USER)
            return new ArrayList<>(); // businessAccountAliases can not be created for this twin
        twinBusinessAccountAliasRepository.createAliasByClass(twinEntity.getId());
        TwinEntity spaceTwin = findSpaceForTwin(twinEntity);
        if (spaceTwin != null) {
            twinBusinessAccountAliasRepository.createAliasBySpace(twinEntity.getId(), spaceTwin.getId());
        }
        return twinBusinessAccountAliasRepository.findAllByTwinId(twinEntity.getId());
    }

    public List<TwinDomainAliasEntity> createTwinDomainAliases(TwinEntity twinEntity) {
        twinDomainAliasRepository.createAliasByClass(twinEntity.getId());
        TwinEntity spaceTwin = findSpaceForTwin(twinEntity);
        if (spaceTwin != null) {
            twinDomainAliasRepository.createAliasBySpace(twinEntity.getId(), spaceTwin.getId());
        }
        return twinDomainAliasRepository.findAllByTwinId(twinEntity.getId());
    }

    public TwinEntity findSpaceForTwin(TwinEntity twinEntity) {
        if (twinEntity.getSpaceTwin() != null)
            return twinEntity.getSpaceTwin();
        if (twinEntity.getHeadTwin() == null && twinEntity.getHeadTwinId() != null)
            twinEntity.setHeadTwin(twinRepository.findById(twinEntity.getHeadTwinId()).get()); //todo fix
        twinEntity.setSpaceTwin(findSpaceForTwin(twinEntity, twinEntity.getHeadTwin(), 10));
        return twinEntity.getSpaceTwin();
    }

    protected TwinEntity findSpaceForTwin(TwinEntity twinEntity, TwinEntity headTwin, int recursionDepth) {
        if (headTwin == null)
            return null;
        else if (headTwin.getTwinClass().isSpace())
            return headTwin;
        else if (recursionDepth == 0) {
            log.warn("Can not detect space for " + twinEntity);
            return null;
        } else
            return findSpaceForTwin(twinEntity, headTwin.getHeadTwin(), recursionDepth - 1);
    }

    public TwinFieldEntity updateField(TwinFieldEntity twinFieldEntity, FieldValue fieldValue) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(twinFieldEntity.getTwinClassField().getFieldTyperFeaturer(), FieldTyper.class);
        EntitiesChangesCollector entitiesChangesCollector = new EntitiesChangesCollector();
        fieldTyper.serializeValue(twinFieldEntity, fieldValue, entitiesChangesCollector);
        entityChangesService.saveEntities(entitiesChangesCollector);
        return twinFieldEntity;
    }

    public void deleteTwin(UUID twinId) throws ServiceException {
        entitySmartService.deleteAndLog(twinId, twinRepository);// all linked data will be deleted by fk cascading
    }

    public TwinEntity cloneTwin(TwinEntity twinEntity) {
        return new TwinEntity()
                .setTwinClass(twinEntity.getTwinClass())
                .setTwinClassId(twinEntity.getTwinClassId())
                .setTwinStatus(twinEntity.getTwinStatus())
                .setTwinStatusId(twinEntity.getTwinStatusId())
                .setName(twinEntity.getName())
                .setDescription(twinEntity.getDescription())
                .setHeadTwinId(twinEntity.getHeadTwinId())
                .setHeadTwin(twinEntity.getHeadTwin())
                .setSpaceTwin(twinEntity.getSpaceTwin())
                .setAssignerUser(twinEntity.getAssignerUser())
                .setAssignerUserId(twinEntity.getAssignerUserId())
                .setCreatedByUser(twinEntity.getCreatedByUser())
                .setCreatedByUserId(twinEntity.getCreatedByUserId())
                .setOwnerBusinessAccount(twinEntity.getOwnerBusinessAccount())
                .setOwnerBusinessAccountId(twinEntity.getOwnerBusinessAccountId())
                .setOwnerUser(twinEntity.getOwnerUser())
                .setOwnerUserId(twinEntity.getOwnerUserId())
                ;
    }

    public TwinFieldEntity cloneTwinField(TwinFieldEntity twinFieldEntity) {
        return new TwinFieldEntity()
                .setTwinClassField(twinFieldEntity.getTwinClassField())
                .setTwinClassFieldId(twinFieldEntity.getTwinClassFieldId())
                .setValue(twinFieldEntity.getValue());
    }

    public List<TwinFieldDataListEntity> cloneTwinFieldDataList(List<TwinFieldDataListEntity> srcTwinFieldDataListEntityList, TwinFieldEntity toField) {
        if (CollectionUtils.isEmpty(srcTwinFieldDataListEntityList))
            return srcTwinFieldDataListEntityList;
        List<TwinFieldDataListEntity> cloneList = new ArrayList<>();
        for (TwinFieldDataListEntity srcFieldDataListEntity : srcTwinFieldDataListEntityList) {
            cloneList.add(
                    new TwinFieldDataListEntity()
                            .setTwinFieldId(toField.getId()) // toField.getId() must be filled!
                            .setDataListOptionId(srcFieldDataListEntity.getDataListOptionId())
            );
        }
        return cloneList;
    }

    public List<TwinFieldEntity> cloneAndSaveTwinFieldList(TwinEntity srcTwin, TwinEntity dstTwinEntity) {
        List<TwinFieldEntity> srcTwinFieldEntityList = findTwinFields(srcTwin.getId());
        if (CollectionUtils.isEmpty(srcTwinFieldEntityList))
            return srcTwinFieldEntityList;
        List<TwinFieldDataListEntity> twinFieldDataListEntityList = twinFieldDataListRepository.findByTwinField_TwinId(srcTwin.getId()); // we also need to clone TwinFieldDataListEntities for twin
        Map<UUID, List<TwinFieldDataListEntity>> twinFieldDataListEntityMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(twinFieldDataListEntityList)) { // converting from plain list to map, for easy access by key
            for (TwinFieldDataListEntity twinFieldDataListEntity : twinFieldDataListEntityList) {
                List<TwinFieldDataListEntity> list = twinFieldDataListEntityMap.computeIfAbsent(twinFieldDataListEntity.getTwinFieldId(), k -> new ArrayList<>());
                list.add(twinFieldDataListEntity);
            }
        }
        List<TwinFieldEntity> cloneFieldEntityList = new ArrayList<>();
        List<TwinFieldDataListEntity> cloneFieldDataListEntityList = new ArrayList<>();
        for (TwinFieldEntity twinFieldEntity : srcTwinFieldEntityList) {
            TwinFieldEntity duplicateTwinFieldEntity = cloneTwinField(twinFieldEntity);
            duplicateTwinFieldEntity
                    .setId(UUID.randomUUID()) // we have to generate it here, for creating TwinFieldDataListEntity
                    .setTwin(dstTwinEntity)
                    .setTwinId(dstTwinEntity.getId());
            cloneFieldEntityList.add(duplicateTwinFieldEntity);
            twinFieldDataListEntityList = twinFieldDataListEntityMap.get(twinFieldEntity.getId());
            if (twinFieldDataListEntityList != null)
                cloneFieldDataListEntityList.addAll(cloneTwinFieldDataList(twinFieldDataListEntityList, duplicateTwinFieldEntity));
        }
        entitySmartService.saveAllAndLog(cloneFieldEntityList, twinFieldRepository);
        entitySmartService.saveAllAndLog(cloneFieldDataListEntityList, twinFieldDataListRepository);
        //todo do we need to clone links?
        return cloneFieldEntityList;
    }

    public TwinEntity duplicateTwin(UUID srcTwinId, BusinessAccountEntity businessAccountEntity, UserEntity userEntity, UUID newTwinId) throws ServiceException {
        return duplicateTwin(
                findEntity(srcTwinId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.none),
                businessAccountEntity,
                userEntity,
                newTwinId);
    }

    public TwinEntity duplicateTwin(TwinEntity srcTwin, BusinessAccountEntity businessAccountEntity, UserEntity userEntity, UUID newTwinId) throws ServiceException {
        TwinEntity duplicateEntity = cloneTwin(srcTwin);
        fillOwner(duplicateEntity, businessAccountEntity, userEntity);
        duplicateEntity
                .setId(newTwinId)
                .setCreatedByUserId(userEntity.getId());
        duplicateEntity = saveTwin(duplicateEntity);
        cloneAndSaveTwinFieldList(srcTwin, duplicateEntity);
        twinflowService.runTwinStatusTransitionTriggers(duplicateEntity, null, duplicateEntity.getTwinStatus());
        return duplicateEntity;
    }

    @Data
    @Accessors(chain = true)
    public static class TwinCreateResult {
        private TwinEntity createdTwin;
        private List<TwinBusinessAccountAliasEntity> businessAccountAliasEntityList;
        private List<TwinDomainAliasEntity> domainAliasEntityList;
    }
}
