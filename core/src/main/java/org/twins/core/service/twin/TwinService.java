package org.twins.core.service.twin;

import jakarta.persistence.EntityManager;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.Kit;
import org.cambium.common.KitGrouped;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.i18n.service.I18nService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.history.HistoryType;
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
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.TwinChangesService;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.history.HistoryCollector;
import org.twins.core.service.history.HistoryCollectorMultiTwin;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinflow.TwinflowService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinService extends EntitySecureFindServiceImpl<TwinEntity> {
    final TwinRepository twinRepository;
    final TwinFieldRepository twinFieldRepository;
    final TwinFieldUserRepository twinFieldUserRepository;
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
    final TwinMarkerService twinMarkerService;
    @Lazy
    final AuthService authService;
    @Lazy
    final SystemEntityService systemEntityService;
    final TwinChangesService twinChangesService;
    @Lazy
    final HistoryService historyService;
    final I18nService i18nService;
    @Lazy
    final TwinTagService twinTagService;

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
            if (SystemEntityService.isTwinClassForUser(entity.getTwinClassId()))
                return false;  //todo check if entity.id is in domain businessAccount users scope. should be cached
            if (SystemEntityService.isTwinClassForBusinessAccount(entity.getTwinClassId()))
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

    public FieldValue getTwinFieldValue(TwinField twinField) throws ServiceException {
        if (twinField == null)
            return null;
        FieldTyper fieldTyper = featurerService.getFeaturer(twinField.getTwinClassField().getFieldTyperFeaturer(), FieldTyper.class);
        return fieldTyper.deserializeValue(twinField);
    }

    /**
     *
     * @param twinEntity - twin
     * @param twinClassFieldId - class field id
     * @return
     *  null - if twinClassFieldId does not belong to twins class
     *  FieldValue.isFilled = false in case when field is not filled for given twin in DB
     * @throws ServiceException
     */
    public FieldValue getTwinFieldValue(TwinEntity twinEntity, UUID twinClassFieldId) throws ServiceException {
        return getTwinFieldValue(getTwinFieldOrNull(twinEntity, twinClassFieldId));
    }

    public FieldValue getTwinFieldValue(TwinEntity twinEntity, TwinClassFieldEntity twinClassField) throws ServiceException {
        return getTwinFieldValue(wrapField(twinEntity, twinClassField));
    }


    public void loadTwinFields(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getTwinFieldBasicKit() != null && twinEntity.getTwinFieldUserKit() != null && twinEntity.getTwinFieldDatalistKit() != null)
            return;
        twinClassFieldService.loadTwinClassFields(twinEntity.getTwinClass());
        boolean hasBasicFields = false, hasUserFields = false, hasDatalistFields = false, hasLinksFields = false;
        for (TwinClassFieldEntity twinClassField : twinEntity.getTwinClass().getTwinClassFieldKit().getList()) {
            FieldTyper fieldTyper = featurerService.getFeaturer(twinClassField.getFieldTyperFeaturer(), FieldTyper.class);
            if (fieldTyper.getStorageType() == TwinFieldEntity.class) {
                hasBasicFields = true;
            } else if (fieldTyper.getStorageType() == TwinFieldUserEntity.class) {
                hasUserFields = true;
            } else if (fieldTyper.getStorageType() == TwinFieldDataListEntity.class) {
                hasDatalistFields = true;
            }
        }
        if (twinEntity.getTwinFieldBasicKit() == null && hasBasicFields)
            twinEntity.setTwinFieldBasicKit(
                    new Kit<>(twinFieldRepository.findByTwinId(twinEntity.getId()), TwinFieldEntity::getTwinClassFieldId));
        if (twinEntity.getTwinFieldUserKit() == null && hasUserFields)
            twinEntity.setTwinFieldUserKit(
                    new KitGrouped<>(twinFieldUserRepository.findByTwinId(twinEntity.getId()), TwinFieldUserEntity::getId, TwinFieldUserEntity::getTwinClassFieldId));
        if (twinEntity.getTwinFieldDatalistKit() == null && hasDatalistFields)
            twinEntity.setTwinFieldDatalistKit(
                    new KitGrouped<>(twinFieldDataListRepository.findByTwinId(twinEntity.getId()), TwinFieldDataListEntity::getId, TwinFieldDataListEntity::getTwinClassFieldId));

    }

    /**
     * Loading all twins fields with minimum db query count
     *
     * @param twinEntityList
     */
    public void loadTwinFields(Collection<TwinEntity> twinEntityList) {
        Map<UUID, TwinEntity> needLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList)
            if (twinEntity.getTwinFieldBasicKit() == null) {
                twinClassFieldService.loadTwinClassFields(twinEntity.getTwinClass());
                needLoad.put(twinEntity.getId(), twinEntity);
            }
        if (needLoad.isEmpty())
            return;
        List<TwinFieldEntity> fieldEntityList = twinFieldRepository.findByTwinIdIn(needLoad.keySet());
        if (CollectionUtils.isEmpty(fieldEntityList))
            return;
        Map<UUID, List<TwinFieldEntity>> fieldsMap = new HashMap<>(); // key - twinId
        Map<UUID, Map<UUID, TwinFieldEntity>> fieldsMap2 = new HashMap<>(); // first key - twinId, second key - twinClassFieldId
        for (TwinFieldEntity twinFieldEntity : fieldEntityList) { //grouping by twin
            fieldsMap.computeIfAbsent(twinFieldEntity.getTwinId(), k -> new ArrayList<>());
            fieldsMap2.computeIfAbsent(twinFieldEntity.getTwinId(), k -> new HashMap<>());
            fieldsMap.get(twinFieldEntity.getTwinId()).add(twinFieldEntity);
            fieldsMap2.get(twinFieldEntity.getTwinId()).put(twinFieldEntity.getTwinClassFieldId(), twinFieldEntity);
        }
        TwinEntity twinEntity;
        List<TwinFieldEntity> twinFieldList;
        Map<UUID, TwinFieldEntity> twinFieldMap;
        for (Map.Entry<UUID, TwinEntity> entry : needLoad.entrySet()) {
            twinEntity = entry.getValue();
            twinFieldList = fieldsMap.get(entry.getKey()); // can be null if entity has no fields
//            twinFieldMap = fieldsMap2.get(entry.getKey()); // can be null if entity has no fields
//            for (TwinClassFieldEntity twinClassField : twinEntity.getTwinClass().getTwinClassFieldKit().getList()) { // adding missing fields
//                if (twinFieldMap == null || !twinFieldMap.containsKey(twinClassField.getId())) {
//                    if (twinFieldList == null) twinFieldList = new ArrayList<>();
//                    twinFieldList.add(createTwinFieldEntity(twinEntity, twinClassField, ""));
//                }
//            }
            twinEntity.setTwinFieldBasicKit(new Kit<>(twinFieldList, TwinFieldEntity::getTwinClassFieldId));
        }
    }

    public TwinField wrapField(TwinEntity twinEntity, UUID twinClassFieldId) throws ServiceException {
        TwinClassFieldEntity twinClassField = twinClassFieldService.findEntitySafe(twinClassFieldId);
        if (twinClassField == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_UNKNOWN, "unknown twinClassFieldId[" + twinClassFieldId + "]");
        return wrapField(twinEntity, twinClassField);
    }


    public TwinField wrapField(UUID twinId, String fieldKey) throws ServiceException {
        TwinEntity twinEntity = entitySmartService.findById(twinId, twinRepository, EntitySmartService.FindMode.ifEmptyThrows);
        TwinClassFieldEntity twinClassField = twinClassFieldService.findByTwinClassIdAndKeyIncludeParent(twinEntity.getTwinClassId(), fieldKey);
        if (twinClassField == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_UNKNOWN, "unknown fieldKey[" + fieldKey + "] for twin["
                    + twinId + "] of class[" + twinEntity.getTwinClass().getKey() + " : " + twinEntity.getTwinClassId() + "]");
        return wrapField(twinEntity, twinClassField);
    }

    public TwinField wrapField(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        if (!twinClassService.isInstanceOf(twinEntity.getTwinClass(), twinClassFieldEntity.getTwinClassId()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_KEY_UNKNOWN, twinClassFieldEntity.logShort()
                    + "is nou suitable for " + twinEntity.logNormal() );
        return new TwinField(twinEntity, twinClassFieldEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinCreateResult createTwin(ApiUser apiUser, TwinCreate twinCreate) throws ServiceException {
        TwinEntity twinEntity = twinCreate.getTwinEntity();
        if (twinEntity.getTwinClass() == null)
            twinEntity.setTwinClass(twinClassService.findEntity(twinEntity.getTwinClassId(), EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows));
        twinEntity.setHeadTwinId(twinHeadService.checkHeadTwinAllowedForClass(twinEntity.getHeadTwinId(), twinEntity.getTwinClass()));
        if (twinEntity.getTwinStatusId() == null) {
            TwinflowEntity twinflowEntity = twinflowService.loadTwinflow(twinEntity);
            twinEntity.setTwinStatusId(twinflowEntity.getInitialTwinStatusId())
                    .setTwinStatus(twinflowEntity.getInitialTwinStatus());
        }
        fillOwner(twinEntity, apiUser.getBusinessAccount(), apiUser.getUser());
        twinEntity = saveTwin(twinEntity);
        saveTwinFields(twinEntity, twinCreate.getFields());
        if (CollectionUtils.isNotEmpty(twinCreate.getAttachmentEntityList())) {
            attachmentService.addAttachments(twinEntity, apiUser.getUser(), twinCreate.getAttachmentEntityList());
        }
        if (CollectionUtils.isNotEmpty(twinCreate.getLinksEntityList()))
            twinLinkService.addLinks(twinEntity, twinCreate.getLinksEntityList());
        if (CollectionUtils.isNotEmpty(twinCreate.getMarkersAdd()))
            twinMarkerService.addMarkers(twinEntity, twinCreate.getMarkersAdd());

        if (CollectionUtils.isNotEmpty(twinCreate.getNewTags()) || CollectionUtils.isNotEmpty(twinCreate.getExistingTags())) {
            twinTagService.createTags(twinEntity, twinCreate.getNewTags(), twinCreate.getExistingTags());
        }

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
        twinEntity = entitySmartService.save(twinEntity, twinRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        historyService.saveHistory(twinEntity, HistoryType.twinCreated, null);
        return twinEntity;
    }

    @Transactional
    public void saveTwinFields(TwinEntity twinEntity, Map<UUID, FieldValue> fields) throws ServiceException {
        if (fields == null)
            return;
        TwinChangesCollector twinChangesCollector = convertTwinFields(twinEntity, fields);
        twinChangesService.saveEntities(twinChangesCollector);
    }

    public TwinChangesCollector convertTwinFields(TwinEntity twinEntity, Map<UUID, FieldValue> fields) throws ServiceException {
        twinClassFieldService.loadTwinClassFields(twinEntity.getTwinClass());
        TwinField twinField;
        FieldValue fieldValue;
        TwinChangesCollector entitiesChangesCollector = new TwinChangesCollector(); //all fields will be saved at once, in one transaction
        for (TwinClassFieldEntity twinClassFieldEntity : twinEntity.getTwinClass().getTwinClassFieldKit().getList()) {
            fieldValue = fields.get(twinClassFieldEntity.getId());
            if (fieldValue == null)
                if (twinClassFieldEntity.isRequired())
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinClassFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " is required");
                else
                    continue;
            var fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturer(), FieldTyper.class);
            fieldTyper.serializeValue(twinEntity, fieldValue, entitiesChangesCollector);
        }
        return entitiesChangesCollector;
    }

    @Transactional
    public void updateTwin(TwinUpdate twinUpdate) throws ServiceException {
        updateTwin(twinUpdate.getTwinEntity(), twinUpdate.getDbTwinEntity(), twinUpdate.getFields());
        cudAttachments(twinUpdate.getDbTwinEntity(), twinUpdate.getAttachmentCUD());
        cudTwinLinks(twinUpdate.getDbTwinEntity(), twinUpdate.getTwinLinkCUD());
        twinMarkerService.addMarkers(twinUpdate.getDbTwinEntity(), twinUpdate.getMarkersAdd());
        twinMarkerService.deleteMarkers(twinUpdate.getDbTwinEntity(), twinUpdate.getMarkersDelete());

        twinTagService.updateTwinTags(twinUpdate.getDbTwinEntity(), twinUpdate.getTagsDelete(), twinUpdate.getNewTags(), twinUpdate.getExistingTags());
    }

    @Transactional
    public void updateTwin(TwinEntity updateTwinEntity, TwinEntity dbTwinEntity, Map<UUID, FieldValue> fields) throws ServiceException {
        ChangesHelper changesHelper = new ChangesHelper();
        HistoryCollector historyCollector = new HistoryCollector();
        if (changesHelper.isChanged("headTwinId", dbTwinEntity.getHeadTwinId(), updateTwinEntity.getHeadTwinId())) {
            historyCollector.add(historyService.headChanged(dbTwinEntity.getHeadTwin(), updateTwinEntity.getHeadTwin()));
            dbTwinEntity
                    .setHeadTwinId(twinHeadService.checkHeadTwinAllowedForClass(updateTwinEntity.getHeadTwinId(), dbTwinEntity.getTwinClass()))
                    .setHeadTwin(updateTwinEntity.getHeadTwin() != null ? updateTwinEntity.getHeadTwin() : null);
        }
        if (changesHelper.isChanged("name", dbTwinEntity.getName(), updateTwinEntity.getName())) {
            historyCollector.add(historyService.nameChanged(dbTwinEntity.getName(), updateTwinEntity.getName()));
            dbTwinEntity.setName(updateTwinEntity.getName());
        }
        if (changesHelper.isChanged("description", dbTwinEntity.getDescription(), updateTwinEntity.getDescription())) {
            historyCollector.add(historyService.descriptionChanged(dbTwinEntity.getDescription(), updateTwinEntity.getDescription()));
            dbTwinEntity.setDescription(updateTwinEntity.getDescription());
        }
        if (changesHelper.isChanged("assignerUser", dbTwinEntity.getAssignerUserId(), updateTwinEntity.getAssignerUserId())) {
            if (updateTwinEntity.getAssignerUserId().equals(TwinUpdate.NULLIFY_MARKER)) {
                historyCollector.add(historyService.assigneeChanged(dbTwinEntity.getAssignerUser(), null));
                dbTwinEntity
                        .setAssignerUserId(null)
                        .setAssignerUser(null);
            } else {
                historyCollector.add(historyService.assigneeChanged(dbTwinEntity.getAssignerUser(), updateTwinEntity.getAssignerUser()));
                dbTwinEntity
                        .setAssignerUserId(updateTwinEntity.getAssignerUserId())
                        .setAssignerUser(updateTwinEntity.getAssignerUser() != null ? updateTwinEntity.getAssignerUser() : null);
            }
        }
        if (changesHelper.isChanged("status", dbTwinEntity.getTwinStatusId(), updateTwinEntity.getTwinStatusId())) {
            historyCollector.add(historyService.statusChanged(dbTwinEntity.getTwinStatus(), updateTwinEntity.getTwinStatus()));
            dbTwinEntity
                    .setTwinStatusId(updateTwinEntity.getTwinStatusId())
                    .setTwinStatus(updateTwinEntity.getTwinStatus() != null ? updateTwinEntity.getTwinStatus() : null);
        }

        entitySmartService.saveAndLogChanges(dbTwinEntity, twinRepository, changesHelper);

        if (changesHelper.hasChanges())
            historyService.saveHistory(dbTwinEntity, historyCollector);
        if (MapUtils.isNotEmpty(fields))
            updateTwinFields(dbTwinEntity, fields.values().stream().toList());
    }

    public void cudAttachments(TwinEntity twinEntity, EntityCUD<TwinAttachmentEntity> attachmentCUD) throws ServiceException {
        if (attachmentCUD == null)
            return;
        if (CollectionUtils.isNotEmpty(attachmentCUD.getCreateList())) {
            attachmentService.addAttachments(twinEntity, twinEntity.getCreatedByUser(), attachmentCUD.getCreateList());
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
    public void changeStatus(Collection<TwinEntity> twinEntityList, TwinStatusEntity newStatus) throws ServiceException {
        ChangesHelper changesHelper = new ChangesHelper();
        HistoryCollectorMultiTwin historyCollector = new HistoryCollectorMultiTwin();
        for (TwinEntity twinEntity : twinEntityList) {
            if (changesHelper.isChanged(twinEntity.logShort() + ".status", twinEntity.getTwinStatusId(), newStatus.getId())) {
                historyCollector.forTwin(twinEntity).add(historyService.statusChanged(twinEntity.getTwinStatus(), newStatus));
                twinEntity
                        .setTwinStatusId(newStatus.getId())
                        .setTwinStatus(newStatus);
            }
        }
        entitySmartService.saveAllAndLogChanges(twinEntityList, twinRepository, changesHelper);
        historyService.saveHistory(historyCollector);
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
        TwinField twinField;
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        for (FieldValue fieldValue : values) {
            twinField = wrapField(twinEntity, fieldValue.getTwinClassField());
            var fieldTyper = featurerService.getFeaturer(twinField.getTwinClassField().getFieldTyperFeaturer(), FieldTyper.class);
            fieldTyper.serializeValue(twinEntity, fieldValue, twinChangesCollector);
        }
        twinChangesService.saveEntities(twinChangesCollector);
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

    public void updateField(TwinField twinField, FieldValue fieldValue) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(twinField.getTwinClassField().getFieldTyperFeaturer(), FieldTyper.class);
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        fieldTyper.serializeValue(twinField.getTwin(), fieldValue, twinChangesCollector);
        twinChangesService.saveEntities(twinChangesCollector);
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

    public CloneFieldsResult cloneTwinFieldListAndSave(TwinEntity srcTwin, TwinEntity dstTwinEntity) throws ServiceException {
        CloneFieldsResult cloneFieldsResult = cloneTwinFieldList(srcTwin, dstTwinEntity);
        if (CollectionUtils.isNotEmpty(cloneFieldsResult.getFieldEntityList()))
            entitySmartService.saveAllAndLog(cloneFieldsResult.getFieldEntityList(), twinFieldRepository);
        if (CollectionUtils.isNotEmpty(cloneFieldsResult.getFieldDataListEntityList()))
            entitySmartService.saveAllAndLog(cloneFieldsResult.getFieldDataListEntityList(), twinFieldDataListRepository);
        if (CollectionUtils.isNotEmpty(cloneFieldsResult.getFieldUserEntityList()))
            entitySmartService.saveAllAndLog(cloneFieldsResult.getFieldUserEntityList(), twinFieldUserRepository);
        //todo do we need to clone links and attachments?
        return cloneFieldsResult;
    }

    public CloneFieldsResult cloneTwinFieldList(TwinEntity srcTwin, TwinEntity dstTwinEntity) throws ServiceException {
        CloneFieldsResult cloneFieldsResult = new CloneFieldsResult();
        loadTwinFields(srcTwin);
        if (KitUtils.isNotEmpty(srcTwin.getTwinFieldBasicKit()))
            for (TwinFieldEntity twinFieldEntity : srcTwin.getTwinFieldBasicKit().getList()) {
                TwinFieldEntity duplicateTwinFieldBasicEntity = twinFieldEntity.cloneFor(dstTwinEntity);
                cloneFieldsResult.add(duplicateTwinFieldBasicEntity);
            }
        if (KitUtils.isNotEmpty(srcTwin.getTwinFieldUserKit()))
            for (TwinFieldUserEntity twinFieldUserEntity : srcTwin.getTwinFieldUserKit().getList()) {
                TwinFieldUserEntity duplicateTwinFieldUserEntity = twinFieldUserEntity.cloneFor(dstTwinEntity);
                cloneFieldsResult.add(duplicateTwinFieldUserEntity);
            }
        if (KitUtils.isNotEmpty(srcTwin.getTwinFieldDatalistKit()))
            for (TwinFieldDataListEntity twinFieldDatalistEntity : srcTwin.getTwinFieldDatalistKit().getList()) {
                TwinFieldDataListEntity duplicateTwinFieldUserEntity = twinFieldDatalistEntity.cloneFor(dstTwinEntity);
                cloneFieldsResult.add(duplicateTwinFieldUserEntity);
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

    public UserEntity getTwinAssignee(UUID twinId) {
        return twinRepository.getAssignee(twinId);
    }

    public void loadFieldsValues(TwinEntity src) throws ServiceException {
        if (src.getFieldValuesKit() != null)
            return;
        loadTwinFields(src);
        src.setFieldValuesKit(new Kit<>(FieldValue::getTwinClassFieldId));
        twinClassFieldService.loadTwinClassFields(src.getTwinClass());
        if (src.getTwinClass().getTwinClassFieldKit().isEmpty())
            return; // just empty kit
        FieldValue fieldValue;
        for (TwinClassFieldEntity twinClassFieldEntity : src.getTwinClass().getTwinClassFieldKit().getList()) {
            fieldValue = getTwinFieldValue(wrapField(src, twinClassFieldEntity));
            src.getFieldValuesKit().add(fieldValue);
        }
    }

    public TwinField getTwinFieldOrNull(TwinEntity twinEntity, UUID twinClassFieldId) throws ServiceException {
        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.getTwinClassFieldOrNull(twinEntity.getTwinClass(), twinClassFieldId);
        if (twinClassFieldEntity == null)
            return null;
        return wrapField(twinEntity, twinClassFieldEntity);
    }

    public static boolean isFilled(FieldValue fieldValue) {
        return fieldValue != null && fieldValue.isFilled();
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
        List<TwinFieldUserEntity> fieldUserEntityList;

        public CloneFieldsResult add(TwinFieldEntity cloneTwinFieldEntity) {
            fieldEntityList = CollectionUtils.safeAdd(fieldEntityList, cloneTwinFieldEntity);
            return this;
        }

        public CloneFieldsResult add(TwinFieldDataListEntity cloneTwinFieldDataListEntity) {
            fieldDataListEntityList = CollectionUtils.safeAdd(fieldDataListEntityList, cloneTwinFieldDataListEntity);
            return this;
        }

        public CloneFieldsResult add(TwinFieldUserEntity cloneTwinFieldUserEntity) {
            fieldUserEntityList = CollectionUtils.safeAdd(fieldUserEntityList, cloneTwinFieldUserEntity);
            return this;
        }
    }

    public static boolean isAssignee(TwinEntity twinEntity, ApiUser apiUser) throws ServiceException {
        return apiUser.getUserId().equals(twinEntity.getAssignerUserId());
    }

    public static boolean isCreator(TwinEntity twinEntity, ApiUser apiUser) throws ServiceException {
        return apiUser.getUserId().equals(twinEntity.getCreatedByUserId());
    }
}
