package org.twins.core.service.twin;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.TypedParameterTwins;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.draft.DraftTwinPersistEntity;
import org.twins.core.dao.history.HistoryType;
import org.twins.core.dao.twin.*;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperList;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.TwinChangesService;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.history.ChangesRecorder;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinflow.TwinflowService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinService extends EntitySecureFindServiceImpl<TwinEntity> {
    private final TwinRepository twinRepository;
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;
    private final TwinFieldUserRepository twinFieldUserRepository;
    private final TwinFieldDataListRepository twinFieldDataListRepository;
    private final TwinClassFieldService twinClassFieldService;
    private final EntitySmartService entitySmartService;
    private final TwinflowService twinflowService;
    private final TwinClassService twinClassService;
    @Lazy
    private final PermissionService permissionService;
    @Lazy
    private final TwinHeadService twinHeadService;
    private final TwinStatusService twinStatusService;
    private final FeaturerService featurerService;
    private final AttachmentService attachmentService;
    @Lazy
    private final TwinLinkService twinLinkService;
    @Lazy
    private final TwinMarkerService twinMarkerService;
    @Lazy
    private final AuthService authService;
    private final TwinChangesService twinChangesService;
    @Lazy
    private final HistoryService historyService;
    @Lazy
    private final TwinTagService twinTagService;
    @Lazy
    private final TwinAliasService twinAliasService;
    private final UserService userService;


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
    public Function<TwinEntity, UUID> entityGetIdFunction() {
        return TwinEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (entity.getTwinClass().getDomainId() != null //system twinClasses can be out of any domain
                && !entity.getTwinClass().getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed for " + apiUser.getDomain().easyLog(EasyLoggable.Level.NORMAL));
            return true;
        }
        if (permissionService.currentUserHasPermission(Permissions.DOMAIN_TWINS_VIEW_ALL))
            return false;
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
                if (!twinClassService.isInstanceOf(entity.getTwinClass(), entity.getTwinStatus().getTwinClassId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect twinStatusId[" + entity.getTwinStatusId() + "]");
        }
        return true;
    }

    public TwinEntity findTwinByAlias(String twinAlias) throws ServiceException {
        return twinAliasService.findAlias(twinAlias).getTwin();
    }

    public FieldValue getTwinFieldValue(TwinField twinField) throws ServiceException {
        if (twinField == null)
            return null;
        FieldTyper fieldTyper = featurerService.getFeaturer(twinField.getTwinClassField().getFieldTyperFeaturer(), FieldTyper.class);
        return fieldTyper.deserializeValue(twinField);
    }

    /**
     * @param twinEntity       - twin
     * @param twinClassFieldId - class field id
     * @return null - if twinClassFieldId does not belong to twins class
     * FieldValue.isFilled = false in case when field is not filled for given twin in DB
     * @throws ServiceException
     */
    public FieldValue getTwinFieldValue(TwinEntity twinEntity, UUID twinClassFieldId) throws ServiceException {
        return getTwinFieldValue(getTwinFieldOrNull(twinEntity, twinClassFieldId));
    }

    public FieldValue getTwinFieldValue(TwinEntity twinEntity, TwinClassFieldEntity twinClassField) throws ServiceException {
        return getTwinFieldValue(wrapField(twinEntity, twinClassField));
    }


    public void loadTwinFields(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getTwinFieldSimpleKit() != null && twinEntity.getTwinFieldUserKit() != null && twinEntity.getTwinFieldDatalistKit() != null)
            return;
        twinClassFieldService.loadTwinClassFields(twinEntity.getTwinClass());
        boolean hasBasicFields = false, hasUserFields = false, hasDatalistFields = false, hasLinksFields = false;
        for (TwinClassFieldEntity twinClassField : twinEntity.getTwinClass().getTwinClassFieldKit().getCollection()) {
            FieldTyper fieldTyper = featurerService.getFeaturer(twinClassField.getFieldTyperFeaturer(), FieldTyper.class);
            if (fieldTyper.getStorageType() == TwinFieldSimpleEntity.class) {
                hasBasicFields = true;
            } else if (fieldTyper.getStorageType() == TwinFieldUserEntity.class) {
                hasUserFields = true;
            } else if (fieldTyper.getStorageType() == TwinFieldDataListEntity.class) {
                hasDatalistFields = true;
            }
        }
        if (twinEntity.getTwinFieldSimpleKit() == null)
            twinEntity.setTwinFieldSimpleKit(
                    new Kit<>(hasBasicFields ? twinFieldSimpleRepository.findByTwinId(twinEntity.getId()) : null, TwinFieldSimpleEntity::getTwinClassFieldId));
        if (twinEntity.getTwinFieldUserKit() == null)
            twinEntity.setTwinFieldUserKit(
                    new KitGrouped<>(hasUserFields ? twinFieldUserRepository.findByTwinId(twinEntity.getId()) : null, TwinFieldUserEntity::getId, TwinFieldUserEntity::getTwinClassFieldId));
        if (twinEntity.getTwinFieldDatalistKit() == null)
            twinEntity.setTwinFieldDatalistKit(
                    new KitGrouped<>(hasDatalistFields ? twinFieldDataListRepository.findByTwinId(twinEntity.getId()) : null, TwinFieldDataListEntity::getId, TwinFieldDataListEntity::getTwinClassFieldId));

    }

    public boolean areFieldsOfTwinClassFieldExists(TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        boolean result = false;
        FieldTyper fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturer(), FieldTyper.class);
        if (fieldTyper.getStorageType() == TwinFieldSimpleEntity.class) {
            result = twinFieldSimpleRepository.existsByTwinClassFieldId(twinClassFieldEntity.getId());
        } else if (fieldTyper.getStorageType() == TwinFieldUserEntity.class) {
            result = twinFieldUserRepository.existsByTwinClassFieldId(twinClassFieldEntity.getId());
        } else if (fieldTyper.getStorageType() == TwinFieldDataListEntity.class) {
            result = twinFieldDataListRepository.existsByTwinClassFieldId(twinClassFieldEntity.getId());
        }
        return result;
    }

    /**
     * Loading all twins fields with minimum db query count
     *
     * @param twinEntityList
     */
    public void loadTwinFields(Collection<TwinEntity> twinEntityList) {
        Map<UUID, TwinEntity> needFieldSimpleLoad = new HashMap<>();
        Map<UUID, TwinEntity> needFieldUserLoad = new HashMap<>();
        Map<UUID, TwinEntity> needFieldDatalistLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList) {
            twinClassFieldService.loadTwinClassFields(twinEntity.getTwinClass());
            if (twinEntity.getTwinFieldSimpleKit() == null) {
                needFieldSimpleLoad.put(twinEntity.getId(), twinEntity);
            }
            if (twinEntity.getTwinFieldUserKit() == null) {
                needFieldUserLoad.put(twinEntity.getId(), twinEntity);
            }
            if (twinEntity.getTwinFieldDatalistKit() == null) {
                needFieldDatalistLoad.put(twinEntity.getId(), twinEntity);
            }
        }
        if (!needFieldSimpleLoad.isEmpty()) {
            KitGrouped<TwinFieldSimpleEntity, UUID, UUID> allTwinsSimpleFieldsKit = new KitGrouped<>(
                    twinFieldSimpleRepository.findByTwinIdIn(needFieldSimpleLoad.keySet()), TwinFieldSimpleEntity::getId, TwinFieldSimpleEntity::getTwinId);
            if (!KitUtils.isEmpty(allTwinsSimpleFieldsKit)) {
                TwinEntity twinEntity;
                for (Map.Entry<UUID, TwinEntity> entry : needFieldSimpleLoad.entrySet()) {
                    twinEntity = entry.getValue();
                    twinEntity.setTwinFieldSimpleKit(new Kit<>(allTwinsSimpleFieldsKit.getGrouped(twinEntity.getId()), TwinFieldSimpleEntity::getTwinClassFieldId));
                }
            }
        }
        if (!needFieldUserLoad.isEmpty()) {
            KitGrouped<TwinFieldUserEntity, UUID, UUID> allTwinsUserFieldsKit = new KitGrouped<>(
                    twinFieldUserRepository.findByTwinIdIn(needFieldUserLoad.keySet()), TwinFieldUserEntity::getId, TwinFieldUserEntity::getTwinId);
            if (!KitUtils.isEmpty(allTwinsUserFieldsKit)) {
                TwinEntity twinEntity;
                for (Map.Entry<UUID, TwinEntity> entry : needFieldUserLoad.entrySet()) {
                    twinEntity = entry.getValue();
                    twinEntity.setTwinFieldUserKit(new KitGrouped<>(allTwinsUserFieldsKit.getGrouped(twinEntity.getId()), TwinFieldUserEntity::getId, TwinFieldUserEntity::getTwinClassFieldId));
                }
            }
        }
        if (!needFieldDatalistLoad.isEmpty()) {
            KitGrouped<TwinFieldDataListEntity, UUID, UUID> allTwinsDatalistFieldsKit = new KitGrouped<>(
                    twinFieldDataListRepository.findByTwinIdIn(needFieldDatalistLoad.keySet()), TwinFieldDataListEntity::getId, TwinFieldDataListEntity::getTwinId);
            if (!KitUtils.isEmpty(allTwinsDatalistFieldsKit)) {
                TwinEntity twinEntity;
                for (Map.Entry<UUID, TwinEntity> entry : needFieldDatalistLoad.entrySet()) {
                    twinEntity = entry.getValue();
                    twinEntity.setTwinFieldDatalistKit(new KitGrouped<>(allTwinsDatalistFieldsKit.getGrouped(twinEntity.getId()), TwinFieldDataListEntity::getId, TwinFieldDataListEntity::getTwinClassFieldId));
                }
            }
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
                    + "is nou suitable for " + twinEntity.logNormal());
        return new TwinField(twinEntity, twinClassFieldEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinCreateResult createTwin(TwinCreate twinCreate) throws ServiceException {
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        createTwin(twinCreate, twinChangesCollector);
        twinChangesService.applyChanges(twinChangesCollector);
        TwinEntity twinEntity = twinCreate.getTwinEntity();
        twinflowService.runTwinStatusTransitionTriggers(twinEntity, null, twinEntity.getTwinStatus());
        //todo mark all uncommited drafts as out-of-dated if they have current twin head deletion
        return new TwinCreateResult()
                .setCreatedTwin(twinEntity)
                .setTwinAliasEntityList(twinAliasService.createAliases(twinEntity));
    }

    public void createTwin(TwinCreate twinCreate, TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinEntity twinEntity = twinCreate.getTwinEntity();
        ApiUser apiUser = authService.getApiUser();
        if(twinCreate.isCheckCreatePermission())
            checkCreatePermission(twinEntity, apiUser);
        createTwinEntity(twinEntity, twinChangesCollector);
        saveTwinFields(twinEntity, twinCreate.getFields(), twinChangesCollector);
        if (CollectionUtils.isNotEmpty(twinCreate.getAttachmentEntityList())) {
            attachmentService.checkAndSetAttachmentTwin(twinCreate.getAttachmentEntityList(), twinEntity);
            attachmentService.addAttachments(twinCreate.getAttachmentEntityList(), twinChangesCollector);
        }
        if (CollectionUtils.isNotEmpty(twinCreate.getLinksEntityList()))
            twinLinkService.addLinks(twinEntity, twinCreate.getLinksEntityList(), twinChangesCollector);
        if (CollectionUtils.isNotEmpty(twinCreate.getMarkersAdd()))
            twinMarkerService.addMarkers(twinEntity, twinCreate.getMarkersAdd(), twinChangesCollector);
        if (CollectionUtils.isNotEmpty(twinCreate.getTagsAddNew()) || CollectionUtils.isNotEmpty(twinCreate.getTagsAddExisted())) {
            twinTagService.createTags(twinEntity, twinCreate.getTagsAddNew(), twinCreate.getTagsAddExisted(), twinChangesCollector);
        }
    }

    public void createTwinEntity(TwinEntity twinEntity, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinEntity.getTwinClass() == null)
            twinEntity.setTwinClass(twinClassService.findEntitySafe(twinEntity.getTwinClassId()));
        if (twinEntity.getId() == null)
            twinEntity.setId(UUID.randomUUID()); // this id is necessary for fields and links. Because entity is not stored currently
        twinEntity.setHeadTwinId(twinHeadService.checkHeadTwinAllowedForClass(twinEntity.getHeadTwinId(), twinEntity.getTwinClass()));
        if (twinEntity.getTwinStatusId() == null) {
            TwinflowEntity twinflowEntity = twinflowService.loadTwinflow(twinEntity);
            twinEntity
                    .setTwinStatusId(twinflowEntity.getInitialTwinStatusId())
                    .setTwinStatus(twinflowEntity.getInitialTwinStatus());
        }
        fillOwner(twinEntity);
        createTwin(twinEntity, twinChangesCollector);
    }

    public UUID detectDeletePermissionId(TwinEntity twinEntity) {
        if(null != twinEntity.getTwinClass())
            return twinEntity.getTwinClass().getCreatePermissionId();
        return twinRepository.detectDeletePermissionId(TypedParameterTwins.uuidNullable(twinEntity.getTwinClassId()));
    }

    public void checkDeletePermission(TwinEntity twinEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID deletePermissionId = detectDeletePermissionId(twinEntity);
        if (null == deletePermissionId)
            return;
        boolean hasPermission = permissionService.hasPermission(twinEntity, deletePermissionId);
        if (!hasPermission)
            throw new ServiceException(ErrorCodeTwins.TWIN_DELETE_ACCESS_DENIED, apiUser.getUser().logShort() + " does not have permission to delete " + twinEntity.logNormal());
    }

    public UUID detectCreatePermissionId(TwinEntity twinEntity) {
        if(null != twinEntity.getTwinClass())
            return twinEntity.getTwinClass().getCreatePermissionId();
        return twinRepository.detectCreatePermissionId(TypedParameterTwins.uuidNullable(twinEntity.getTwinClassId()));
    }

    public void checkCreatePermission(TwinEntity twinEntity, ApiUser apiUser) throws ServiceException {
        UUID createPermissionId = detectCreatePermissionId(twinEntity);
        if (null == createPermissionId)
            return;
        boolean hasPermission = permissionService.hasPermission(twinEntity, createPermissionId);
        if (!hasPermission)
            throw new ServiceException(ErrorCodeTwins.TWIN_CREATE_ACCESS_DENIED, apiUser.getUser().logShort() + " does not have permission to create " + twinEntity.logNormal());
    }

    public UUID detectUpdatePermissionId(TwinEntity twinEntity) {
        if(null != twinEntity.getTwinClass())
            return twinEntity.getTwinClass().getCreatePermissionId();
        return twinRepository.detectUpdatePermissionId(TypedParameterTwins.uuidNullable(twinEntity.getTwinClassId()));
    }

    public void checkUpdatePermission(TwinEntity twinEntity, ApiUser apiUser) throws ServiceException {
        UUID updatePermissionId = detectUpdatePermissionId(twinEntity);
        if (null == updatePermissionId)
            return;
        boolean hasPermission = permissionService.hasPermission(twinEntity, updatePermissionId);
        if (!hasPermission)
            throw new ServiceException(ErrorCodeTwins.TWIN_UPDATE_ACCESS_DENIED, apiUser.getUser().logShort() + " does not have permission to edit " + twinEntity.logNormal());
    }

    public TwinEntity fillOwner(TwinEntity twinEntity) throws ServiceException {
        TwinClassEntity twinClassEntity = twinEntity.getTwinClass();
        ApiUser apiUser = authService.getApiUser();
        switch (twinClassEntity.getOwnerType()) {
            case DOMAIN:
                //twin will not be owned neither businessAccount, neither user
                break;
            case BUSINESS_ACCOUNT:
            case DOMAIN_BUSINESS_ACCOUNT:
                if (!apiUser.isBusinessAccountSpecified())
                    throw new ServiceException(ErrorCodeTwins.BUSINESS_ACCOUNT_UNKNOWN, twinClassEntity.logNormal() + " can not be created without businessAccount owner");
                twinEntity
                        .setOwnerBusinessAccountId(apiUser.getBusinessAccountId())
                        .setOwnerUserId(null);
                break;
            case USER:
            case DOMAIN_USER:
                if (!apiUser.isUserSpecified())
                    throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, twinClassEntity.logNormal() + " can not be created without user owner");
                twinEntity
                        .setOwnerUserId(apiUser.getUserId())
                        .setOwnerBusinessAccountId(null);
        }
        return twinEntity;
    }

    public void checkAssignee(TwinEntity twinEntity) throws ServiceException {
        checkAssignee(twinEntity, twinEntity.getAssignerUserId());
    }

    public void checkAssignee(TwinEntity twinEntity, UUID userId) throws ServiceException {
        if (null == userId)
            return;
        TwinClassEntity twinClassEntity = twinEntity.getTwinClass();
        switch (twinClassEntity.getOwnerType()) {
            case DOMAIN:
                if (!userService.checkUserRegisteredInDomain(userId, twinClassEntity.getDomainId()))
                    throw new ServiceException(ErrorCodeTwins.DOMAIN_USER_NOT_EXISTS, "Assignee for " + twinEntity.logNormal() + " user[" + userId + "] does not registered in domain[" + twinClassEntity.getDomainId() + "]");
                break;
            case BUSINESS_ACCOUNT:
                if (!userService.checkUserRegisteredInBusinessAccount(userId, twinEntity.getOwnerBusinessAccountId()))
                    throw new ServiceException(ErrorCodeTwins.BUSINESS_ACCOUNT_USER_NOT_EXISTS, "Assignee for " + twinEntity.logNormal() + " user[" + userId + "] does not registered in business account[" + twinEntity.getOwnerBusinessAccountId() + "]");
                break;
            case DOMAIN_BUSINESS_ACCOUNT:
                if (!userService.checkUserRegisteredInDomainAndBusinessAccount(userId, twinEntity.getOwnerBusinessAccountId(), twinClassEntity.getDomainId()))
                    throw new ServiceException(ErrorCodeTwins.DOMAIN_OR_BUSINESS_ACCOUNT_USER_NOT_EXISTS, "Assignee for " + twinEntity.logNormal() + " user[" + userId + "] does not registered in business account[" + twinEntity.getOwnerBusinessAccountId() + "] or in domain[" + twinClassEntity.getDomainId() + "]");
                break;
            case USER:
            case DOMAIN_USER:
                if (!userId.equals(twinEntity.getOwnerUserId()))
                    throw new ServiceException(ErrorCodeTwins.DOMAIN_OR_BUSINESS_ACCOUNT_USER_NOT_EXISTS, "Assignee for " + twinEntity.logNormal() + " - user[" + userId + "] - is not the owner of the twin");
        }
    }


    @Transactional
    public TwinEntity createTwin(TwinEntity twinEntity) throws ServiceException {
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        createTwin(twinEntity, twinChangesCollector);
        twinChangesService.applyChanges(twinChangesCollector);
        return twinEntity;
    }

    private void createTwin(TwinEntity twinEntity, TwinChangesCollector twinChangesCollector) throws ServiceException {
        checkAssignee(twinEntity);
        validateEntityAndThrow(twinEntity, EntitySmartService.EntityValidateMode.beforeSave);
        if (twinEntity.getCreatedAt() == null)
            twinEntity.setCreatedAt(Timestamp.from(Instant.now()));
        twinChangesCollector.add(twinEntity);
        if (twinChangesCollector.isHistoryCollectorEnabled())
            twinChangesCollector.getHistoryCollector(twinEntity).add(HistoryType.twinCreated, null);
    }


    public void saveTwinFields(TwinEntity twinEntity, Map<UUID, FieldValue> fields, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (fields == null)
            return;
        convertTwinFields(twinEntity, fields, twinChangesCollector);
    }

    public TwinChangesCollector convertTwinFields(TwinEntity twinEntity, Map<UUID, FieldValue> fields, TwinChangesCollector twinChangesCollector) throws ServiceException {
        twinClassFieldService.loadTwinClassFields(twinEntity.getTwinClass());
        FieldValue fieldValue;
        for (TwinClassFieldEntity twinClassFieldEntity : twinEntity.getTwinClass().getTwinClassFieldKit().getCollection()) {
            fieldValue = fields.get(twinClassFieldEntity.getId());
            if (fieldValue == null || !fieldValue.isFilled())
                if (twinClassFieldEntity.getRequired())
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinClassFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " is required");
                else
                    continue;
            var fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturer(), FieldTyper.class);
            fieldTyper.serializeValue(twinEntity, fieldValue, twinChangesCollector);
        }
        return twinChangesCollector;
    }

    @Transactional
    public void updateTwinFields(TwinEntity twinEntity, List<FieldValue> values) throws ServiceException {
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        updateTwinFields(twinEntity, values, twinChangesCollector);
        twinChangesService.applyChanges(twinChangesCollector);
    }

    public void updateTwinFields(TwinEntity twinEntity, List<FieldValue> values, TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinField twinField;
        for (FieldValue fieldValue : values) {
            twinField = wrapField(twinEntity, fieldValue.getTwinClassField());
            var fieldTyper = featurerService.getFeaturer(twinField.getTwinClassField().getFieldTyperFeaturer(), FieldTyper.class);
            fieldTyper.serializeValue(twinEntity, fieldValue, twinChangesCollector);
        }
    }

    public void updateTwin(TwinUpdate twinUpdate) throws ServiceException {
        if (!twinUpdate.isChanged())
            return;
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        ChangesRecorder<TwinEntity, TwinEntity> changesRecorder = new ChangesRecorder<>(
                twinUpdate.getDbTwinEntity(),
                twinUpdate.getTwinEntity(),
                twinUpdate.getDbTwinEntity(),
                twinChangesCollector.getHistoryCollector(twinUpdate.getDbTwinEntity()));
        updateTwin(twinUpdate, twinChangesCollector, changesRecorder);
        twinChangesService.applyChanges(twinChangesCollector);
    }

    public void updateTwin(TwinUpdate twinUpdate, TwinChangesCollector twinChangesCollector, ChangesRecorder<TwinEntity, ?> twinChangesRecorder) throws ServiceException {
        if (!twinUpdate.isChanged())
            return;
        ApiUser apiUser = authService.getApiUser();
        if(twinUpdate.isCheckEditPermission())
            checkUpdatePermission(twinUpdate.getDbTwinEntity(), apiUser);
        updateTwinBasics(twinChangesRecorder);
        if (twinChangesRecorder.hasChanges())
            twinChangesCollector.add(twinChangesRecorder.getRecorder());
        if (MapUtils.isNotEmpty(twinUpdate.getFields()))
            updateTwinFields(twinChangesRecorder.getDbEntity(), twinUpdate.getFields().values().stream().toList(), twinChangesCollector);
        attachmentService.cudAttachments(twinUpdate.getDbTwinEntity(), twinUpdate.getAttachmentCUD(), twinChangesCollector);
        twinLinkService.cudTwinLinks(twinUpdate.getDbTwinEntity(), twinUpdate.getTwinLinkCUD(), twinChangesCollector);
        twinMarkerService.addMarkers(twinUpdate.getDbTwinEntity(), twinUpdate.getMarkersAdd(), twinChangesCollector);
        twinMarkerService.deleteMarkers(twinUpdate.getDbTwinEntity(), twinUpdate.getMarkersDelete(), twinChangesCollector);
        twinTagService.updateTwinTags(twinUpdate.getDbTwinEntity(), twinUpdate.getTagsDelete(), twinUpdate.getTagsAddNew(), twinUpdate.getTagsAddExisted(), twinChangesCollector);
    }

    public void updateTwinBasics(ChangesRecorder<TwinEntity, ?> changesRecorder) throws ServiceException {
        updateTwinHead(changesRecorder);
        updateTwinName(changesRecorder);
        updateTwinDescription(changesRecorder);
        updateTwinAssignee(changesRecorder);
        updateTwinStatus(changesRecorder);
    }

    public void updateTwinAssignee(ChangesRecorder<TwinEntity, ?> changesRecorder) throws ServiceException {
        if (changesRecorder.isChanged("assignerUser", changesRecorder.getDbEntity().getAssignerUserId(), changesRecorder.getUpdateEntity().getAssignerUserId())) {
            UserEntity newAssignee = null;
            if (!UuidUtils.isNullifyMarker(changesRecorder.getUpdateEntity().getAssignerUserId())) {
                checkAssignee(changesRecorder.getDbEntity(), changesRecorder.getUpdateEntity().getAssignerUserId());
                newAssignee = changesRecorder.getUpdateEntity().getAssignerUser();
            }
            if (changesRecorder.isHistoryCollectorEnabled())
                changesRecorder.getHistoryCollector().add(historyService.assigneeChanged(changesRecorder.getDbEntity().getAssignerUser(), newAssignee));
            if (changesRecorder.getRecorder() instanceof DraftTwinPersistEntity draftTwinPersistEntity)
                draftTwinPersistEntity
                        .setAssignerUserId(changesRecorder.getUpdateEntity().getAssignerUserId()); // we should not nullify value here, because NULLIFY_MARKER will indicate it in draft table
            if (changesRecorder.getRecorder() instanceof TwinEntity twinEntity)
                twinEntity
                        .setAssignerUserId(UuidUtils.nullifyIfNecessary(changesRecorder.getUpdateEntity().getAssignerUserId()))
                        .setAssignerUser(newAssignee);
        }
    }

    public void updateTwinHead(ChangesRecorder<TwinEntity, ?> changesRecorder) throws ServiceException {
        if (changesRecorder.isChanged("headTwinId", changesRecorder.getDbEntity().getHeadTwinId(), changesRecorder.getUpdateEntity().getHeadTwinId())) {
            if (changesRecorder.isHistoryCollectorEnabled())
                changesRecorder.getHistoryCollector().add(historyService.headChanged(changesRecorder.getDbEntity().getHeadTwin(), changesRecorder.getUpdateEntity().getHeadTwin()));
            UUID headTwinId = twinHeadService.checkHeadTwinAllowedForClass(changesRecorder.getUpdateEntity().getHeadTwinId(), changesRecorder.getDbEntity().getTwinClass());
            if (changesRecorder.getRecorder() instanceof DraftTwinPersistEntity draftTwinPersistEntity)
                draftTwinPersistEntity
                        .setHeadTwinId(headTwinId);
            if (changesRecorder.getRecorder() instanceof TwinEntity twinEntity)
                twinEntity
                        .setHeadTwinId(headTwinId);
        }
    }

    public void updateTwinDescription(ChangesRecorder<TwinEntity, ?> changesRecorder) {
        if (changesRecorder.isChanged("description", changesRecorder.getDbEntity().getDescription(), changesRecorder.getUpdateEntity().getDescription())) {
            if (changesRecorder.isHistoryCollectorEnabled())
                changesRecorder.getHistoryCollector().add(historyService.descriptionChanged(changesRecorder.getDbEntity().getDescription(), changesRecorder.getUpdateEntity().getDescription()));
            if (changesRecorder.getRecorder() instanceof DraftTwinPersistEntity draftTwinPersistEntity)
                draftTwinPersistEntity
                        .setDescription(changesRecorder.getUpdateEntity().getDescription());
            if (changesRecorder.getRecorder() instanceof TwinEntity twinEntity)
                twinEntity
                        .setDescription(changesRecorder.getUpdateEntity().getDescription());
        }
    }

    public void updateTwinName(ChangesRecorder<TwinEntity, ?> changesRecorder) {
        if (changesRecorder.isChanged("name", changesRecorder.getDbEntity().getName(), changesRecorder.getUpdateEntity().getName())) {
            if (changesRecorder.isHistoryCollectorEnabled())
                changesRecorder.getHistoryCollector().add(historyService.nameChanged(changesRecorder.getDbEntity().getName(), changesRecorder.getUpdateEntity().getName()));
            if (changesRecorder.getRecorder() instanceof DraftTwinPersistEntity draftTwinPersistEntity)
                draftTwinPersistEntity
                        .setName(changesRecorder.getUpdateEntity().getName());
            if (changesRecorder.getRecorder() instanceof TwinEntity twinEntity)
                twinEntity
                        .setName(changesRecorder.getUpdateEntity().getName());
        }
    }

    public void updateTwinStatus(ChangesRecorder<TwinEntity, ?> changesRecorder) {
        if (changesRecorder.isChanged("status", changesRecorder.getDbEntity().getTwinStatusId(), changesRecorder.getUpdateEntity().getTwinStatusId())) {
            if (changesRecorder.isHistoryCollectorEnabled())
                changesRecorder.getHistoryCollector().add(historyService.statusChanged(changesRecorder.getDbEntity().getTwinStatus(), changesRecorder.getUpdateEntity().getTwinStatus()));
            if (changesRecorder.getRecorder() instanceof DraftTwinPersistEntity draftTwinPersistEntity)
                draftTwinPersistEntity
                        .setTwinStatusId(changesRecorder.getUpdateEntity().getTwinStatusId())
                        .setTwinStatus(changesRecorder.getUpdateEntity().getTwinStatus());
            if (changesRecorder.getRecorder() instanceof TwinEntity twinEntity)
                twinEntity
                        .setTwinStatusId(changesRecorder.getUpdateEntity().getTwinStatusId())
                        .setTwinStatus(changesRecorder.getUpdateEntity().getTwinStatus());
        }
    }

    @Transactional
    public void changeStatus(Collection<TwinEntity> twinEntityList, TwinStatusEntity newStatus) throws ServiceException {
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        for (TwinEntity twinEntity : twinEntityList) {
            if (twinChangesCollector.collectIfChanged(twinEntity, "status", twinEntity.getTwinStatusId(), newStatus.getId())) {
                twinChangesCollector.getHistoryCollector(twinEntity).add(historyService.statusChanged(twinEntity.getTwinStatus(), newStatus));
                twinEntity
                        .setTwinStatusId(newStatus.getId())
                        .setTwinStatus(newStatus);
            }
        }
        twinChangesService.applyChanges(twinChangesCollector);
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
        twinChangesService.applyChanges(twinChangesCollector);
    }

    public void cloneTwinFieldListAndSave(TwinEntity srcTwin, TwinEntity dstTwinEntity) throws ServiceException {
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        cloneTwinFields(srcTwin, dstTwinEntity, twinChangesCollector);
        twinChangesService.applyChanges(twinChangesCollector);
        //todo do we need to clone links and attachments?
    }

    public void cloneTwinFields(TwinEntity srcTwin, TwinEntity dstTwinEntity, TwinChangesCollector twinChangesCollector) throws ServiceException {
        loadTwinFields(srcTwin);
        if (KitUtils.isNotEmpty(srcTwin.getTwinFieldSimpleKit()))
            for (TwinFieldSimpleEntity twinFieldEntity : srcTwin.getTwinFieldSimpleKit().getCollection()) {
                TwinFieldSimpleEntity duplicateTwinFieldBasicEntity = twinFieldEntity.cloneFor(dstTwinEntity);
                twinChangesCollector.add(duplicateTwinFieldBasicEntity);
            }
        if (KitUtils.isNotEmpty(srcTwin.getTwinFieldUserKit()))
            for (TwinFieldUserEntity twinFieldUserEntity : srcTwin.getTwinFieldUserKit().getCollection()) {
                TwinFieldUserEntity duplicateTwinFieldUserEntity = twinFieldUserEntity.cloneFor(dstTwinEntity);
                twinChangesCollector.add(duplicateTwinFieldUserEntity);
            }
        if (KitUtils.isNotEmpty(srcTwin.getTwinFieldDatalistKit()))
            for (TwinFieldDataListEntity twinFieldDatalistEntity : srcTwin.getTwinFieldDatalistKit().getCollection()) {
                TwinFieldDataListEntity duplicateTwinFieldUserEntity = twinFieldDatalistEntity.cloneFor(dstTwinEntity);
                twinChangesCollector.add(duplicateTwinFieldUserEntity);
            }
    }

    public TwinFieldSimpleEntity createTwinFieldEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity, String value) {
        return new TwinFieldSimpleEntity()
                .setTwinClassField(twinClassFieldEntity)
                .setTwinClassFieldId(twinClassFieldEntity.getId())
                .setTwin(twinEntity)
                .setTwinId(twinEntity.getId())
                .setValue(value);
    }

    public TwinEntity duplicateTwin(UUID srcTwinId, UUID newTwinId) throws ServiceException {
        return duplicateTwin(
                findEntity(srcTwinId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.none),
                newTwinId);
    }

    public TwinEntity duplicateTwin(TwinEntity srcTwin, UUID newTwinId) throws ServiceException {
        TwinEntity duplicateEntity = srcTwin.clone();
        fillOwner(duplicateEntity);
        duplicateEntity
                .setId(newTwinId)
                .setCreatedByUserId(authService.getApiUser().getUserId());
        duplicateEntity = createTwin(duplicateEntity);
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
        for (TwinClassFieldEntity twinClassFieldEntity : src.getTwinClass().getTwinClassFieldKit().getCollection()) {
            fieldValue = getTwinFieldValue(wrapField(src, twinClassFieldEntity));
            src.getFieldValuesKit().add(fieldValue);
        }
    }

    public void loadFieldsValues(Collection<TwinEntity> twinEntityList) throws ServiceException {
        Map<UUID, TwinEntity> needLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList)
            if (twinEntity.getFieldValuesKit() == null)
                needLoad.put(twinEntity.getId(), twinEntity);
        if (needLoad.isEmpty())
            return;
        loadTwinFields(needLoad.values());
        for (Map.Entry<UUID, TwinEntity> entry : needLoad.entrySet())
            loadFieldsValues(entry.getValue());
    }

    public FieldValue createFieldValue(UUID twinClassFieldEntityId, String value) throws ServiceException {
        return createFieldValue(twinClassFieldService.findEntitySafe(twinClassFieldEntityId), value);
    }

    public FieldValue createFieldValue(TwinClassFieldEntity twinClassFieldEntity, String value) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturer(), FieldTyper.class);
        FieldValue fieldValue = null;
        if (fieldTyper.getValueType() == FieldValueText.class)
            fieldValue = new FieldValueText(twinClassFieldEntity);
        if (fieldTyper.getValueType() == FieldValueColorHEX.class)
            fieldValue = new FieldValueColorHEX(twinClassFieldEntity);
        if (fieldTyper.getValueType() == FieldValueDate.class)
            fieldValue = new FieldValueDate(twinClassFieldEntity);
        if (fieldTyper.getValueType() == FieldValueSelect.class)
            fieldValue = new FieldValueSelect(twinClassFieldEntity);
        if (fieldTyper.getValueType() == FieldValueUser.class)
            fieldValue = new FieldValueUser(twinClassFieldEntity);
        if (fieldTyper.getValueType() == FieldValueLink.class)
            fieldValue = new FieldValueLink(twinClassFieldEntity);
        if (fieldTyper.getValueType() == FieldValueInvisible.class)
            fieldValue = new FieldValueInvisible(twinClassFieldEntity);
        if (fieldValue == null)
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "unknown fieldTyper[" + fieldTyper.getValueType() + "]");

        if (value == null) // nullify
            fieldValue.nullify();
        else
            setFieldValue(fieldValue, value);
        return fieldValue;
    }

    public void setFieldValue(FieldValue fieldValue, String value) throws ServiceException {
        if (fieldValue instanceof FieldValueText fieldValueText)
            fieldValueText.setValue(value);
        if (fieldValue instanceof FieldValueColorHEX fieldValueColorHEX)
            fieldValueColorHEX.setHex(value);
        if (fieldValue instanceof FieldValueDate fieldValueDate)
            fieldValueDate.setDate(value);
        if (fieldValue instanceof FieldValueSelect fieldValueSelect) {
            for (String dataListOption : value.split(FieldTyperList.LIST_SPLITTER)) {
                if (org.cambium.common.util.StringUtils.isEmpty(dataListOption)) continue;
                DataListOptionEntity dataListOptionEntity = new DataListOptionEntity();
                if (UuidUtils.isUUID(dataListOption)) dataListOptionEntity.setId(UUID.fromString(dataListOption));
                else dataListOptionEntity.setOption(dataListOption);
                fieldValueSelect.add(dataListOptionEntity);
            }
        }
        if (fieldValue instanceof FieldValueUser fieldValueUser) {
            for (String userId : value.split(FieldTyperList.LIST_SPLITTER)) {
                if (org.cambium.common.util.StringUtils.isEmpty(userId))
                    continue;
                UUID userUUID;
                try {
                    userUUID = UUID.fromString(userId);
                } catch (Exception e) {
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, fieldValueUser.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " incorrect user UUID[" + userId + "]");
                }
                fieldValueUser.add(new UserEntity()
                        .setId(userUUID));
            }
        }
        if (fieldValue instanceof FieldValueLink fieldValueLink) {
            for (String dstTwinId : value.split(FieldTyperList.LIST_SPLITTER)) {
                if (org.cambium.common.util.StringUtils.isEmpty(dstTwinId))
                    continue;
                UUID dstTwinUUID;
                try {
                    dstTwinUUID = UUID.fromString(dstTwinId);
                } catch (Exception e) {
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, fieldValueLink.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " incorrect link UUID[" + dstTwinId + "]");
                }
                ((FieldValueLink) fieldValue).add(new TwinLinkEntity()
                        .setDstTwinId(dstTwinUUID));
            }
        }
    }


    public FieldValue copyToField(FieldValue src, UUID dstTwinClassFieldId) throws ServiceException {
        TwinClassFieldEntity dstTwinClassField = twinClassFieldService.findEntitySafe(dstTwinClassFieldId);
        if (!isCopyable(src.getTwinClassField(), dstTwinClassField))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, src.getTwinClassField().logShort()
                    + " value can not be copied to " + dstTwinClassField.logShort());
        return src.clone(dstTwinClassField);
    }

    public boolean isCopyable(TwinClassFieldEntity src, TwinClassFieldEntity dst) throws ServiceException {
        FieldTyper srcFieldTyper = featurerService.getFeaturer(src.getFieldTyperFeaturer(), FieldTyper.class);
        FieldTyper dstFieldTyper = featurerService.getFeaturer(dst.getFieldTyperFeaturer(), FieldTyper.class);
        return srcFieldTyper.getStorageType().equals(dstFieldTyper.getStorageType());
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

    public KitGrouped<TwinClassFieldEntity, UUID, UUID> findInheritedTwinClassFields(TwinClassEntity twinClassEntity, TwinClassEntity skipFromTwinClass, boolean showUsedOnly) throws ServiceException {
        KitGrouped<TwinClassFieldEntity, UUID, UUID> result = new KitGrouped<>(TwinClassFieldEntity::getId, TwinClassFieldEntity::getTwinClassId);
        if (twinClassEntity.getExtendsTwinClassId() == null)
            return result;
        twinClassService.loadExtendsTwinClass(twinClassEntity);
        TwinClassEntity extendsTwinClassEntity = twinClassEntity.getExtendsTwinClass();
        twinClassFieldService.loadTwinClassFields(extendsTwinClassEntity);
        if (extendsTwinClassEntity.getTwinClassFieldKit().isEmpty())
            return result;
        if (skipFromTwinClass != null)
            twinClassFieldService.loadTwinClassFields(skipFromTwinClass);
        if (!showUsedOnly) {
            for (TwinClassFieldEntity inheritedTwinClassFieldEntity : extendsTwinClassEntity.getTwinClassFieldKit().getCollection()) {
                if (skipFromTwinClass != null && skipFromTwinClass.getTwinClassFieldKit().containsKey(inheritedTwinClassFieldEntity.getId()))
                    continue;
                result.add(inheritedTwinClassFieldEntity);
            }
        }
        Set<UUID> inheritedTwinClassFieldIds = new HashSet<>();
        Set<UUID> inheritedSimpleTwinClassFieldIds = new HashSet<>();
        Set<UUID> inheritedUserTwinClassFieldIds = new HashSet<>();
        Set<UUID> inheritedDatalistTwinClassFieldIds = new HashSet<>();
        for (TwinClassFieldEntity inheritedTwinClassFieldEntity : extendsTwinClassEntity.getTwinClassFieldKit().getCollection()) {
            if (skipFromTwinClass != null && skipFromTwinClass.getTwinClassFieldKit().containsKey(inheritedTwinClassFieldEntity.getId()))
                continue;
            var fieldTyper = featurerService.getFeaturer(inheritedTwinClassFieldEntity.getFieldTyperFeaturer(), FieldTyper.class);
            if (fieldTyper.getStorageType() == TwinFieldSimpleEntity.class) {
                inheritedSimpleTwinClassFieldIds.add(inheritedTwinClassFieldEntity.getId());
            } else if (fieldTyper.getStorageType() == TwinFieldUserEntity.class) {
                inheritedUserTwinClassFieldIds.add(inheritedTwinClassFieldEntity.getId());
            } else if (fieldTyper.getStorageType() == TwinFieldDataListEntity.class) {
                inheritedDatalistTwinClassFieldIds.add(inheritedTwinClassFieldEntity.getId());
            }
        }
        if (!inheritedSimpleTwinClassFieldIds.isEmpty()) {
            inheritedTwinClassFieldIds.addAll(twinFieldSimpleRepository.findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(twinClassEntity.getId(), inheritedSimpleTwinClassFieldIds));
        }
        if (!inheritedUserTwinClassFieldIds.isEmpty()) {
            inheritedTwinClassFieldIds.addAll(twinFieldUserRepository.findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(twinClassEntity.getId(), inheritedSimpleTwinClassFieldIds));
        }
        if (!inheritedDatalistTwinClassFieldIds.isEmpty()) {
            inheritedTwinClassFieldIds.addAll(twinFieldDataListRepository.findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(twinClassEntity.getId(), inheritedSimpleTwinClassFieldIds));
        }
        if (CollectionUtils.isEmpty(inheritedTwinClassFieldIds))
            return result;
        for (TwinClassFieldEntity twinClassField : extendsTwinClassEntity.getTwinClassFieldKit().getCollection()) {
            if (inheritedTwinClassFieldIds.contains(twinClassField.getId())) {
                result.add(twinClassField);
            }
        }
        return result;
    }

    public void deleteTwinFieldsOfClass(Collection<TwinClassFieldEntity> twinClassFieldsForDeletion, UUID twinClassId) {
        if (CollectionUtils.isEmpty(twinClassFieldsForDeletion))
            return;
        // we should not care about FieldStorage, cause such deletion will do extra cleaning
        List<UUID> twinClassFieldIds = twinClassFieldsForDeletion.stream().map(TwinClassFieldEntity::getId).toList();
        twinFieldSimpleRepository.deleteByTwin_TwinClassIdAndTwinClassFieldIdIn(twinClassId, twinClassFieldIds);
        twinFieldUserRepository.deleteByTwin_TwinClassIdAndTwinClassFieldIdIn(twinClassId, twinClassFieldIds);
        twinFieldDataListRepository.deleteByTwin_TwinClassIdAndTwinClassFieldIdIn(twinClassId, twinClassFieldIds);
        log.info("Twin class fields [" + StringUtils.join(twinClassFieldIds, ",") + "] perhaps were deleted from all twins of class[" + twinClassId + "]");
    }

    public void convertFieldsForTwinsOfClass(TwinClassEntity twinClassEntity, TwinClassFieldEntity twinClassFieldForReplace, TwinClassFieldEntity twinClassFieldReplacement) throws ServiceException {
        if (!twinClassFieldService.isConvertable(twinClassFieldForReplace, twinClassFieldReplacement))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, twinClassFieldForReplace.logNormal() + " can not be converted to " + twinClassFieldReplacement.logNormal());
        var fieldTyper = featurerService.getFeaturer(twinClassFieldForReplace.getFieldTyperFeaturer(), FieldTyper.class);
        if (fieldTyper.getStorageType() == TwinFieldSimpleEntity.class) {
            twinFieldSimpleRepository.replaceTwinClassFieldForTwinsOfClass(twinClassEntity.getId(), twinClassFieldForReplace.getId(), twinClassFieldReplacement.getId());
        } else if (fieldTyper.getStorageType() == TwinFieldUserEntity.class) {
            twinFieldUserRepository.replaceTwinClassFieldForTwinsOfClass(twinClassEntity.getId(), twinClassFieldForReplace.getId(), twinClassFieldReplacement.getId());
        } else if (fieldTyper.getStorageType() == TwinFieldDataListEntity.class) {
            twinFieldDataListRepository.replaceTwinClassFieldForTwinsOfClass(twinClassEntity.getId(), twinClassFieldForReplace.getId(), twinClassFieldReplacement.getId());
        }
    }

    @Data
    @Accessors(chain = true)
    public static class TwinCreateResult {
        private TwinEntity createdTwin;
        private List<TwinAliasEntity> twinAliasEntityList;
    }

    @Data
    @Accessors(chain = true)
    public static class TwinUpdateResult {
        private TwinEntity updatedTwin;
    }

    @Data
    public static class CloneFieldsResult {
        List<TwinFieldSimpleEntity> fieldEntityList;
        List<TwinFieldDataListEntity> fieldDataListEntityList;
        List<TwinFieldUserEntity> fieldUserEntityList;

        public CloneFieldsResult add(TwinFieldSimpleEntity cloneTwinFieldEntity) {
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

    public void forceDeleteTwins(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();

        int deletedCount = twinRepository.deleteAllByBusinessAccountIdAndDomainId(businessAccountId, domainId);
        log.info(deletedCount + " number of twins were deleted");
    }

}
