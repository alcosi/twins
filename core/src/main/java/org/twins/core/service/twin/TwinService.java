package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
import org.twins.core.domain.*;
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

    public static Map<UUID, List<TwinEntity>> toClassMap(List<TwinEntity> twinEntityList) {
        Map<UUID, List<TwinEntity>> ret = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList) {
            List<TwinEntity> twinsGroupedByClass = ret.computeIfAbsent(twinEntity.getTwinClassId(), k -> new ArrayList<>());
            twinsGroupedByClass.add(twinEntity);
        }
        return ret;
    }

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
                && entity.getOwnerBusinessAccountId() != null //for twin_templates owner will not be filled
                && !entity.getOwnerBusinessAccountId().equals(apiUser.getBusinessAccount().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed for " + apiUser.getBusinessAccount().easyLog(EasyLoggable.Level.NORMAL));
            return true;
        }
        if (entity.getTwinClass().getOwnerType().isUserLevel()
                && entity.getOwnerUserId() != null //for twin_templates owner will not be filled
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

    public FieldValue getTwinFieldValue(TwinFieldEntity twinFieldEntity) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(twinFieldEntity.getTwinClassField().getFieldTyperFeaturer(), FieldTyper.class);
        return fieldTyper.deserializeValue(twinFieldEntity);
    }

    public List<TwinFieldEntity> loadTwinFields(TwinEntity twinEntity) {
        if (twinEntity.getTwinFieldList() != null)
            return twinEntity.getTwinFieldList();
        List<TwinFieldEntity> fields = findTwinFields(twinEntity.getId());
        twinEntity.setTwinFieldList(fields);
        return fields;
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

    public TwinFieldEntity findTwinField(UUID twinId, UUID twinClassFieldId) throws ServiceException {
        return twinFieldRepository.findByTwinIdAndTwinClassFieldId(twinId, twinClassFieldId);
    }

    public TwinFieldEntity findTwinFieldIncludeMissing(UUID twinId, UUID twinClassFieldId) throws ServiceException {
        TwinFieldEntity twinFieldEntity = twinFieldRepository.findByTwinIdAndTwinClassFieldId(twinId, twinClassFieldId);
        if (twinFieldEntity != null)
            return twinFieldEntity;
        TwinEntity twinEntity = findEntitySafe(twinId);
        twinFieldEntity = new TwinFieldEntity()
                .setTwinClassField(twinClassFieldService.findEntitySafe(twinClassFieldId))
                .setTwinClassFieldId(twinClassFieldId)
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
        List<TwinFieldEntity> twinFieldEntityList = twinFieldRepository.findByTwinId(twinEntity.getId());
        Map<UUID, TwinFieldEntity> twinFieldEntityMap = twinFieldEntityList.stream().collect(Collectors.toMap(TwinFieldEntity::getTwinClassFieldId, Function.identity()));
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
    public TwinCreateResult createTwin(ApiUser apiUser, TwinCreate twinCreate) throws ServiceException {
        return createTwin(apiUser, twinCreate.getTwinEntity(), twinCreate.getFields(), twinCreate.getAttachmentEntityList(), twinCreate.getLinksEntityList());
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinCreateResult createTwin(ApiUser apiUser, TwinEntity twinEntity, Map<UUID, FieldValue> fields, List<TwinAttachmentEntity> attachmentEntityList, List<TwinLinkEntity> linksEntityList) throws ServiceException {
        TwinflowEntity twinflowEntity = twinflowService.getTwinflow(twinEntity.getTwinClassId());
        if (twinEntity.getTwinClass() == null)
            twinEntity.setTwinClass(twinClassService.findEntity(twinEntity.getTwinClassId(), EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows));
        twinEntity
                .setHeadTwinId(twinHeadService.checkHeadTwinAllowedForClass(twinEntity.getHeadTwinId(), twinEntity.getTwinClass()))
                .setTwinStatusId(twinflowEntity.getInitialTwinStatusId())
                .setTwinStatus(twinflowEntity.getInitialTwinStatus());
        fillOwner(twinEntity, apiUser.getBusinessAccount(), apiUser.getUser());
        twinEntity = saveTwin(twinEntity);
        saveTwinFields(twinEntity, fields);
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
                        .setOwnerUserId(null);
                break;
            case USER:
            case DOMAIN_USER:
                if (userEntity == null)
                    throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, twinClassEntity.easyLog(EasyLoggable.Level.NORMAL) + " can not be created without user owner");
                twinEntity
                        .setOwnerUserId(userEntity.getId())
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
    public void saveTwinFields(TwinEntity twinEntity, Map<UUID, FieldValue> fields) throws ServiceException {
        if (fields == null)
            return;
        EntitiesChangesCollector entitiesChangesCollector = convertTwinFields(twinEntity, fields);
        entityChangesService.saveEntities(entitiesChangesCollector);
    }

    public EntitiesChangesCollector convertTwinFields(TwinEntity twinEntity, Map<UUID, FieldValue> fields) throws ServiceException {
        List<TwinClassFieldEntity> twinClassFieldEntityList = twinClassFieldService.findTwinClassFieldsIncludeParent(twinEntity.getTwinClass());
        TwinFieldEntity twinFieldEntity;
        FieldValue fieldValue;
        EntitiesChangesCollector entitiesChangesCollector = new EntitiesChangesCollector(); //all fields will be saved at once, in one transaction
        for (TwinClassFieldEntity twinClassFieldEntity : twinClassFieldEntityList) {
            fieldValue = fields.get(twinClassFieldEntity.getId());
            if (fieldValue == null)
                if (twinClassFieldEntity.isRequired())
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinClassFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " is required");
                else
                    continue;
            twinFieldEntity = createTwinFieldEntity(twinEntity, twinClassFieldEntity, null);
            var fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturer(), FieldTyper.class);
            fieldTyper.serializeValue(twinFieldEntity, fieldValue, entitiesChangesCollector);
        }
        return entitiesChangesCollector;
    }

    @Transactional
    public TwinUpdateResult updateTwin(TwinUpdate twinUpdate) throws ServiceException {
        return updateTwin(twinUpdate.getTwinEntity(), twinUpdate.getDbTwinEntity(), twinUpdate.getFields(), twinUpdate.getAttachmentCUD(), twinUpdate.getTwinLinkCUD());
    }

    @Transactional
    public TwinUpdateResult updateTwin(TwinEntity updateTwinEntity, TwinEntity dbTwinEntity, Map<UUID, FieldValue> fields, EntityCUD<TwinAttachmentEntity> attachmentCUD, EntityCUD<TwinLinkEntity> twinLinkCUD) throws ServiceException {
        TwinUpdateResult twinUpdateResult = updateTwin(updateTwinEntity, dbTwinEntity, fields);
        cudAttachments(dbTwinEntity, attachmentCUD);
        cudTwinLinks(dbTwinEntity, twinLinkCUD);
        return twinUpdateResult;
    }

    @Transactional
    public TwinUpdateResult updateTwin(TwinEntity updateTwinEntity, TwinEntity dbTwinEntity, Map<UUID, FieldValue> fields) throws ServiceException {
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
        if (changesHelper.isChanged("status", dbTwinEntity.getTwinStatusId(), updateTwinEntity.getTwinStatusId())) {
            dbTwinEntity.setTwinStatusId(updateTwinEntity.getTwinStatusId());
        }
        TwinEntity updatedTwin = entitySmartService.saveAndLogChanges(dbTwinEntity, twinRepository, changesHelper);
        if (MapUtils.isNotEmpty(fields))
            updateTwinFields(dbTwinEntity, fields.values().stream().toList());
        return new TwinUpdateResult().setUpdatedTwin(updatedTwin);
    }

    public void cudAttachments(TwinEntity twinEntity, EntityCUD<TwinAttachmentEntity> attachmentCUD) throws ServiceException {
        if (attachmentCUD == null)
            return;
        if (CollectionUtils.isNotEmpty(attachmentCUD.getCreateList())) {
            attachmentService.addAttachments(twinEntity.getId(), twinEntity.getCreatedByUser(), attachmentCUD.getCreateList());
        }
        if (CollectionUtils.isNotEmpty(attachmentCUD.getUpdateList())) {
            attachmentService.updateAttachments(attachmentCUD.getUpdateList());
        }
        if (CollectionUtils.isNotEmpty(attachmentCUD.getDeleteUUIDList())) {
            attachmentService.deleteAttachments(twinEntity.getId(), attachmentCUD.getDeleteUUIDList());
        }
    }

    public void cudTwinLinks(TwinEntity twinEntity, EntityCUD<TwinLinkEntity> twinLinkCUD) throws ServiceException {
        if (twinLinkCUD == null)
            return;
        if (CollectionUtils.isNotEmpty(twinLinkCUD.getCreateList())) {
            twinLinkService.addLinks(twinEntity, twinLinkCUD.getCreateList());
        }
        if (CollectionUtils.isNotEmpty(twinLinkCUD.getUpdateList())) {
            twinLinkService.updateTwinLinks(twinEntity, twinLinkCUD.getUpdateList());
        }
        if (CollectionUtils.isNotEmpty(twinLinkCUD.getDeleteUUIDList())) {
            twinLinkService.deleteTwinLinks(twinEntity.getId(), twinLinkCUD.getDeleteUUIDList());
        }
    }

    @Transactional
    public void changeStatus(TwinEntity twinEntity, TwinStatusEntity newStatus) {
        ChangesHelper changesHelper = new ChangesHelper();
        if (changesHelper.isChanged("status", twinEntity.getTwinStatusId(), newStatus.getId())) {
            twinEntity
                    .setTwinStatusId(newStatus.getId())
                    .setTwinStatus(newStatus);
            entitySmartService.saveAndLogChanges(twinEntity, twinRepository, changesHelper);
        }
    }

    @Transactional
    public void changeStatus(Collection<TwinEntity> twinEntityList, TwinStatusEntity newStatus) {
        ChangesHelper changesHelper = new ChangesHelper();
        for (TwinEntity twinEntity : twinEntityList) {
            if (changesHelper.isChanged(twinEntity + ".status", twinEntity.getTwinStatusId(), newStatus.getId())) {
                twinEntity
                        .setTwinStatusId(newStatus.getId())
                        .setTwinStatus(newStatus);
            }
        }
        entitySmartService.saveAllAndLogChanges(twinEntityList, twinRepository, changesHelper);
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
        TwinEntity spaceTwin = loadSpaceForTwin(twinEntity);
        if (spaceTwin != null) {
            twinBusinessAccountAliasRepository.createAliasBySpace(twinEntity.getId(), spaceTwin.getId());
        }
        return twinBusinessAccountAliasRepository.findAllByTwinId(twinEntity.getId());
    }

    public List<TwinDomainAliasEntity> createTwinDomainAliases(TwinEntity twinEntity) {
        twinDomainAliasRepository.createAliasByClass(twinEntity.getId());
        TwinEntity spaceTwin = loadSpaceForTwin(twinEntity);
        if (spaceTwin != null) {
            twinDomainAliasRepository.createAliasBySpace(twinEntity.getId(), spaceTwin.getId());
        }
        return twinDomainAliasRepository.findAllByTwinId(twinEntity.getId());
    }

    public TwinEntity loadSpaceForTwin(TwinEntity twinEntity) {
        if (twinEntity.getSpaceTwin() != null)
            return twinEntity.getSpaceTwin();
        loadHeadForTwin(twinEntity);
        if (twinEntity.getHeadTwin() == null)
            return null;
        twinEntity.setSpaceTwin(findSpaceForTwin(twinEntity, twinEntity.getHeadTwin(), 10));
        return twinEntity.getSpaceTwin();
    }

    public TwinEntity loadHeadForTwin(TwinEntity twinEntity) {
        if (twinEntity.getHeadTwin() != null)
            return twinEntity.getHeadTwin();
        if (twinEntity.getHeadTwinId() == null)
            return null;
        TwinEntity headTwin = twinRepository.findById(twinEntity.getHeadTwinId()).get(); //fix
        twinEntity.setHeadTwin(headTwin);
        return headTwin;
    }

    protected TwinEntity findSpaceForTwin(TwinEntity twinEntity, TwinEntity headTwin, int recursionDepth) {
        if (headTwin == null)
            return null;
        else if (headTwin.getTwinClass().isSpace())
            return headTwin;
        else if (recursionDepth == 0) {
            log.warn("Can not detect space for " + twinEntity.logShort());
            return null;
        } else {
            loadHeadForTwin(headTwin);
            return findSpaceForTwin(twinEntity, headTwin.getHeadTwin(), recursionDepth - 1);
        }
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
                .setSpaceTwin(twinEntity.getSpaceTwin())
                .setAssignerUser(twinEntity.getAssignerUser())
                .setAssignerUserId(twinEntity.getAssignerUserId())
                .setCreatedByUser(twinEntity.getCreatedByUser())
                .setCreatedByUserId(twinEntity.getCreatedByUserId())
                .setOwnerBusinessAccountId(twinEntity.getOwnerBusinessAccountId())
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

    public CloneFieldsResult cloneTwinFieldListAndSave(TwinEntity srcTwin, TwinEntity dstTwinEntity) {
        CloneFieldsResult cloneFieldsResult = cloneTwinFieldList(srcTwin, dstTwinEntity);
        if (CollectionUtils.isNotEmpty(cloneFieldsResult.getFieldEntityList()))
            entitySmartService.saveAllAndLog(cloneFieldsResult.getFieldEntityList(), twinFieldRepository);
        if (CollectionUtils.isNotEmpty(cloneFieldsResult.getFieldDataListEntityList()))
            entitySmartService.saveAllAndLog(cloneFieldsResult.getFieldDataListEntityList(), twinFieldDataListRepository);
        //todo do we need to clone links?
        return cloneFieldsResult;
    }

    public CloneFieldsResult cloneTwinFieldList(TwinEntity srcTwin, TwinEntity dstTwinEntity) {
        CloneFieldsResult cloneFieldsResult = new CloneFieldsResult();
        List<TwinFieldEntity> srcTwinFieldEntityList = findTwinFields(srcTwin.getId());
        if (CollectionUtils.isEmpty(srcTwinFieldEntityList))
            return cloneFieldsResult;
        List<TwinFieldDataListEntity> twinFieldDataListEntityList = twinFieldDataListRepository.findByTwinField_TwinId(srcTwin.getId()); // we also need to clone TwinFieldDataListEntities for twin
        Map<UUID, List<TwinFieldDataListEntity>> twinFieldDataListEntityMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(twinFieldDataListEntityList)) { // converting from plain list to map, for easy access by key
            for (TwinFieldDataListEntity twinFieldDataListEntity : twinFieldDataListEntityList) {
                List<TwinFieldDataListEntity> list = twinFieldDataListEntityMap.computeIfAbsent(twinFieldDataListEntity.getTwinFieldId(), k -> new ArrayList<>());
                list.add(twinFieldDataListEntity);
            }
        }
        for (TwinFieldEntity twinFieldEntity : srcTwinFieldEntityList) {
            TwinFieldEntity duplicateTwinFieldEntity = cloneTwinField(twinFieldEntity);
            duplicateTwinFieldEntity
                    .setId(UUID.randomUUID()) // we have to generate it here, for creating TwinFieldDataListEntity
                    .setTwin(dstTwinEntity)
                    .setTwinId(dstTwinEntity.getId());
            cloneFieldsResult.add(duplicateTwinFieldEntity);
            twinFieldDataListEntityList = twinFieldDataListEntityMap.get(twinFieldEntity.getId());
            if (twinFieldDataListEntityList != null)
                cloneFieldsResult.add(cloneTwinFieldDataList(twinFieldDataListEntityList, duplicateTwinFieldEntity));
        }
        return cloneFieldsResult;
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
        cloneTwinFieldListAndSave(srcTwin, duplicateEntity);
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

    @Data
    @Accessors(chain = true)
    public static class TwinUpdateResult {
        private TwinEntity updatedTwin;
    }

    @Data
    public static class CloneFieldsResult {
        List<TwinFieldEntity> fieldEntityList;
        List<TwinFieldDataListEntity> fieldDataListEntityList;

        public CloneFieldsResult add(TwinFieldEntity cloneTwinFieldEntity) {
            if (fieldEntityList == null)
                fieldEntityList = new ArrayList<>();
            fieldEntityList.add(cloneTwinFieldEntity);
            return this;
        }

        public CloneFieldsResult add(TwinFieldDataListEntity cloneTwinFieldDataListEntity) {
            if (fieldDataListEntityList == null)
                fieldDataListEntityList = new ArrayList<>();
            fieldDataListEntityList.add(cloneTwinFieldDataListEntity);
            return this;
        }

        public CloneFieldsResult add(List<TwinFieldDataListEntity> cloneTwinFieldDataListEntity) {
            if (fieldDataListEntityList == null)
                fieldDataListEntityList = new ArrayList<>();
            fieldDataListEntityList.addAll(cloneTwinFieldDataListEntity);
            return this;
        }
    }
}
