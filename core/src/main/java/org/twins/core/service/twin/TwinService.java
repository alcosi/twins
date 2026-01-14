package org.twins.core.service.twin;

import com.github.f4b6a3.uuid.UuidCreator;
import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.exception.TwinBatchFieldValidationException;
import org.cambium.common.exception.TwinFieldValidationException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.kit.KitGroupedObj;
import org.cambium.common.util.*;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.draft.DraftTwinPersistEntity;
import org.twins.core.dao.twin.*;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRepository;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinDuplicate;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.factory.FactoryLauncher;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperList;
import org.twins.core.featurer.fieldtyper.storage.*;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.TwinChangesService;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.face.FaceTwinPointerService;
import org.twins.core.service.factory.TwinFactoryService;
import org.twins.core.service.history.ChangesRecorder;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinflow.TwinflowFactoryService;
import org.twins.core.service.twinflow.TwinflowService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.twins.core.featurer.fieldtyper.FieldTyperList.LIST_SPLITTER;

//Log calls that took more then 2 seconds
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinService extends EntitySecureFindServiceImpl<TwinEntity> {

    // resolve AOP problem: one-service self-invocation methods
    @Autowired
    private TwinService self;


    private final TwinRepository twinRepository;
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;
    private final TwinFieldSimpleNonIndexedRepository twinFieldSimpleNonIndexedRepository;
    private final TwinFieldUserRepository twinFieldUserRepository;
    private final TwinFieldDataListRepository twinFieldDataListRepository;
    private final TwinFieldI18nRepository twinFieldI18nRepository;
    private final TwinFieldBooleanRepository twinFieldBooleanRepository;
    private final TwinFieldTwinClassListRepository twinFieldTwinClassListRepository;
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
    @Lazy
    private final TwinFieldAttributeService twinFieldAttributeService;
    private final UserService userService;
    @Autowired
    private TwinflowFactoryService twinflowFactoryService;
    @Autowired
    private I18nService i18nService;

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

    public TwinEntity findHeadTwin(UUID twinId) {
        TwinEntity headTwin = twinRepository.findHeadTwin(twinId);
        if (headTwin == null)
            return null;
        if (isEntityReadDenied(headTwin))
            return null;
        return headTwin;
    }

    public FieldValue getTwinFieldValue(TwinField twinField) throws ServiceException {
        if (twinField == null)
            return null;
        FieldTyper fieldTyper = featurerService.getFeaturer(twinField.getTwinClassField().getFieldTyperFeaturerId(), FieldTyper.class);
        return fieldTyper.deserializeValue(twinField);
    }

    /**
     * @param twinEntity       - twin
     * @param twinClassFieldId - class field id
     * @return null - if twinClassFieldId does not belong to twins class
     * FieldValue.isFilled = false in case when field is not filled for given twin in DB
     * //TODO cache field values inside twin
     * @throws ServiceException
     */
    public FieldValue getTwinFieldValue(TwinEntity twinEntity, UUID twinClassFieldId) throws ServiceException {
        return getTwinFieldValue(getTwinFieldOrNull(twinEntity, twinClassFieldId));
    }

    public FieldValue getTwinFieldValue(TwinEntity twinEntity, TwinClassFieldEntity twinClassField) throws ServiceException {
        return getTwinFieldValue(wrapField(twinEntity, twinClassField));
    }


    public void loadTwinFields(TwinEntity twinEntity) throws ServiceException {
        loadTwinFields(Collections.singletonList(twinEntity));
    }

    public boolean areFieldsOfTwinClassFieldExists(TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        boolean result = false;
        FieldTyper fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturerId(), FieldTyper.class);
        TwinFieldStorage storage = fieldTyper.getStorage(twinClassFieldEntity);
        return storage.hasStrictValues(twinClassFieldEntity.getId());
    }

    /**
     * Loading all twins fields with minimum db query count
     *
     * @param twinEntityList
     */
    public void loadTwinFields(Collection<TwinEntity> twinEntityList) throws ServiceException {
        Map<TwinFieldStorage, Kit<TwinEntity, UUID>> needFieldLoad = new HashMap<>();

        KitGroupedObj<TwinEntity, UUID, UUID, TwinClassEntity> twinsGroupedByClass = new KitGroupedObj<>(
                twinEntityList, TwinEntity::getId, TwinEntity::getTwinClassId, TwinEntity::getTwinClass);
        twinClassFieldService.loadTwinClassFields(twinsGroupedByClass.getGroupingObjectMap().values());

        for (TwinClassEntity twinClassEntity : twinsGroupedByClass.getGroupingObjectMap().values()) {
            twinClassFieldService.loadFieldStorages(twinClassEntity);
            for (TwinEntity twinEntity : twinsGroupedByClass.getGrouped(twinClassEntity.getId())) {
                for (var fieldStorage : twinClassEntity.getFieldStorageSet()) {
                    if (!fieldStorage.isLoaded(twinEntity)) {
                        needFieldLoad.computeIfAbsent(fieldStorage, k -> new Kit<>(TwinEntity::getId))
                                .add(twinEntity);
//                        if (twinClassEntity.getFieldStorageSet().contains(fieldStorage)) {
//                            needFieldLoad.computeIfAbsent(fieldStorage, k -> new Kit<>(TwinEntity::getId))
//                                    .add(twinEntity);
//                        } else {
//                            fieldStorage.initEmpty(twinEntity);
//                        }
                    }
                }
            }
        }

        if (needFieldLoad.isEmpty())
            return;

        for (var needLoadEntry : needFieldLoad.entrySet()) {
            TwinFieldStorage twinFieldsStorage = needLoadEntry.getKey();
            if (KitUtils.isNotEmpty(needLoadEntry.getValue())) {
                twinFieldsStorage.load(needLoadEntry.getValue());
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
        TwinClassFieldEntity twinClassField = twinClassFieldService.findByTwinClassIdAndKeyIncludeParents(twinEntity.getTwinClass(), fieldKey);
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
                .setTwinAliasEntityList(twinAliasService.createAliasesForTwin(twinEntity, true));
    }

    //faster, but dont call it form method annotated by @transactional
    public TwinCreateResult createTwinAsync(TwinCreate twinCreate) throws ServiceException {
        List<TwinEntity> twins = self.createTwinsAsync(Collections.singletonList(twinCreate));
        TwinBatchCreateResult twinBatchCreateResult = generateTwinAliasesAndMakeCreationResult(twins);
        TwinCreateResult result = twinBatchCreateResult.getTwinCreateResultList().getFirst();
        return result;
    }

    public List<TwinEntity> createTwinsAsyncBatch(List<TwinCreate> twinCreates) throws ServiceException {
        List<TwinEntity> twins = self.createTwinsAsync(twinCreates);
        generateTwinAliasesAndMakeCreationResult(twins);
        return twins;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinEntity> createTwinsAsync(List<TwinCreate> twinCreateList) throws ServiceException {
        // todo try to use parallel stream for this
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        for (TwinCreate twinCreate : twinCreateList) {
            createTwin(twinCreate, twinChangesCollector);
        }
        twinChangesService.applyChanges(twinChangesCollector);
        List<TwinEntity> twins = new ArrayList<>();
        for (TwinCreate twinCreate : twinCreateList) {
            TwinEntity twinEntity = twinCreate.getTwinEntity();
            twins.add(twinEntity);
            twinflowService.runTwinStatusTransitionTriggers(twinEntity, null, twinEntity.getTwinStatus());
        }
        //todo mark all uncommited drafts as out-of-dated if they have current twin head deletion
        return twins;
    }

    public TwinBatchCreateResult generateTwinAliasesAndMakeCreationResult(List<TwinEntity> twins) throws ServiceException {
        TwinBatchCreateResult twinBatchCreateResult = new TwinBatchCreateResult();
        boolean returnResult = twins.size() == 1;
        Map<UUID, List<TwinAliasEntity>> aliasesForTwins = null;
        try {
            aliasesForTwins = twinAliasService.createAliasesForTwins(twins, returnResult).get();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(ErrorCodeTwins.ERROR_TWIN_ALIASES_CREATION, "failed to create aliases for twins: " + twins.stream().map(TwinEntity::logShort).collect(Collectors.joining(", ")));
        }
        for (TwinEntity twin : twins)
            twinBatchCreateResult.getTwinCreateResultList().add(
                    new TwinCreateResult()
                            .setCreatedTwin(twin)
                            .setTwinAliasEntityList(
                                    returnResult && null != aliasesForTwins ?
                                            aliasesForTwins.get(twin.getId()) :
                                            null
                            ));
        return twinBatchCreateResult;
    }

    public void createTwin(TwinCreate twinCreate, TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinEntity twinEntity = twinCreate.getTwinEntity();
        if (twinEntity.getTwinClass() == null)
            twinEntity.setTwinClass(twinClassService.findEntitySafe(twinEntity.getTwinClassId()));
        if (Boolean.TRUE.equals(twinEntity.getTwinClass().getAbstractt())) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_IS_ABSTRACT, "Cannot create twin of abstract twin class: " + twinEntity.getTwinClass().logShort());
        }
        setHeadSafe(twinEntity);
        twinEntity.setCreateElseUpdate(true);
        if (twinCreate.isCheckCreatePermission())
            checkCreatePermission(twinEntity, authService.getApiUser());
        createTwinEntity(twinCreate, twinChangesCollector);
        runFactoryOnCreate(twinCreate);
        validateFields(twinEntity, twinCreate.getFields());
        saveTwinFields(twinEntity, twinCreate.getFields(), twinChangesCollector);
        if (CollectionUtils.isNotEmpty(twinCreate.getAttachmentEntityList())) {
            attachmentService.checkAndSetAttachmentTwin(twinCreate.getAttachmentEntityList(), twinEntity);
            attachmentService.addAttachments(twinCreate.getAttachmentEntityList(), twinChangesCollector);
        }
        if (CollectionUtils.isNotEmpty(twinCreate.getLinksEntityList())) {
            twinLinkService.addLinks(twinEntity, twinCreate.getLinksEntityList(), twinChangesCollector);
        }
        if (CollectionUtils.isNotEmpty(twinCreate.getMarkersAdd())) {
            twinMarkerService.addMarkers(twinEntity, twinCreate.getMarkersAdd(), twinChangesCollector);
        }
        if (CollectionUtils.isNotEmpty(twinCreate.getTagsAddNew()) || CollectionUtils.isNotEmpty(twinCreate.getTagsAddExisted())) {
            twinTagService.createTags(twinEntity, twinCreate.getTagsAddNew(), twinCreate.getTagsAddExisted(), twinChangesCollector);
        }
        if (CollectionUtils.isNotEmpty(twinCreate.getTwinFieldAttributeEntityList())) {
            twinFieldAttributeService.addAttributes(twinEntity, twinCreate.getTwinFieldAttributeEntityList(), twinChangesCollector);
        }
        runFactoryAfterCreate(twinCreate, twinChangesCollector);
    }

    private void setHeadSafe(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getTwinClass().getHeadTwinClassId() != null && twinEntity.getHeadTwinId() == null) {
            throw new ServiceException(ErrorCodeTwins.HEAD_TWIN_NOT_SPECIFIED, "head twin is required for " + twinEntity.getTwinClass().logShort());
        } else if (twinEntity.getTwinClass().getHeadTwinClassId() == null) {
            return;
        }
        TwinEntity headTwin = twinHeadService.checkValidHeadForClass(twinEntity.getHeadTwinId(), twinEntity.getTwinClass());
        twinEntity
                .setHeadTwinId(headTwin.getId())
                .setPermissionSchemaSpaceId(getPermissionSchemaSpaceId(headTwin));
    }


    public static UUID getPermissionSchemaSpaceId(TwinEntity headTwin) {
        return Boolean.TRUE.equals(headTwin.getTwinClass().getPermissionSchemaSpace()) ?
                headTwin.getId() : headTwin.getPermissionSchemaSpaceId();
    }

    public void createTwinEntity(TwinCreate twinCreate, TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinEntity twinEntity = twinCreate.getTwinEntity();
        if (twinEntity.getId() == null)
            twinEntity.setId(UuidCreator.getTimeOrderedEpoch()); // this id is necessary for fields and links. Because entity is not stored currently
        twinEntity.setCreateElseUpdate(true);
        if (twinCreate.isSketchMode()) {
            setInitSketchStatus(twinEntity);
        } else if (twinEntity.getTwinStatusId() == null) {
            setInitStatus(twinEntity);
        }
        fillOwner(twinEntity);
        createTwin(twinEntity, twinChangesCollector);
    }

    private void setInitStatus(TwinEntity twinEntity) throws ServiceException {
        TwinflowEntity twinflowEntity = twinflowService.loadTwinflow(twinEntity);
        twinEntity
                .setTwinStatusId(twinflowEntity.getInitialTwinStatusId())
                .setTwinStatus(twinflowEntity.getInitialTwinStatus());
    }

    public void setInitSketchStatus(TwinEntity twinEntity) throws ServiceException {
        TwinflowEntity twinflowEntity = twinflowService.loadTwinflow(twinEntity);
        var sketchStatus = twinflowService.getInitSketchStatusSafe(twinflowEntity);
        twinEntity
                .setTwinStatus(sketchStatus)
                .setTwinStatusId(sketchStatus.getId());
    }

    public UUID detectDeletePermissionId(TwinEntity twinEntity) throws ServiceException {
        if (null == twinEntity.getTwinClass())
            twinEntity.setTwinClass(twinClassService.findEntitySafe(twinEntity.getTwinClassId()));
        return twinEntity.getTwinClass().getDeletePermissionId();
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

    public UUID detectCreatePermissionId(TwinEntity twinEntity) throws ServiceException {
        if (null == twinEntity.getTwinClass())
            twinEntity.setTwinClass(twinClassService.findEntitySafe(twinEntity.getTwinClassId()));
        return twinEntity.getTwinClass().getCreatePermissionId();
    }

    public void checkCreatePermission(TwinEntity twinEntity, ApiUser apiUser) throws ServiceException {
        if (permissionService.currentUserHasPermission(Permissions.DOMAIN_TWINS_CREATE_ANY))
            return;
        UUID createPermissionId = detectCreatePermissionId(twinEntity);
        if (null == createPermissionId)
            return;
        boolean hasPermission = permissionService.hasPermission(twinEntity, createPermissionId);
        if (!hasPermission)
            throw new ServiceException(ErrorCodeTwins.TWIN_CREATE_ACCESS_DENIED, apiUser.getUser().logShort() + " does not have permission to create " + twinEntity.logNormal());
    }

    public UUID detectUpdatePermissionId(TwinEntity twinEntity) throws ServiceException {
        if (null == twinEntity.getTwinClass())
            twinEntity.setTwinClass(twinClassService.findEntitySafe(twinEntity.getTwinClassId()));
        return twinEntity.getTwinClass().getEditPermissionId();
    }

    public void checkUpdatePermission(TwinEntity twinEntity, ApiUser apiUser) throws ServiceException {
        if (permissionService.currentUserHasPermission(Permissions.DOMAIN_TWINS_CREATE_ANY))
            return;
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
        if (Boolean.TRUE.equals(twinEntity.getTwinClass().getAssigneeRequired()) && userId == null) {
            throw new ServiceException(
                    ErrorCodeTwins.TWIN_ASSIGNEE_REQUIRED,
                    "Assignee is required for " + twinEntity.getTwinClass().logShort()
            );
        }

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
        if (twinEntity.getCreatedByUserId() == null)
            twinEntity.setCreatedByUserId(authService.getApiUser().getUserId());
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
        for (TwinClassFieldEntity twinClassFieldEntity : twinEntity.getTwinClass().getTwinClassFieldKit().getCollection()) {
            serializeFieldValue(twinEntity, fields, twinChangesCollector, twinClassFieldEntity);
        }
        for (var twinClassFieldId : fields.keySet()) {
            if (SystemEntityService.isSystemField(twinClassFieldId)) {
                TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.getBaseField(twinClassFieldId);
                serializeFieldValue(twinEntity, fields, twinChangesCollector, twinClassFieldEntity);
            }
        }
        return twinChangesCollector;
    }

    private void serializeFieldValue(TwinEntity twinEntity, Map<UUID, FieldValue> fields, TwinChangesCollector twinChangesCollector, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        Optional<FieldValue> fieldValue = getFieldValueSafe(twinEntity, fields, twinClassFieldEntity);
        if (fieldValue.isEmpty())
            return;
        var fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturerId(), FieldTyper.class);
        fieldTyper.serializeValue(twinEntity, fieldValue.get(), twinChangesCollector);
    }

    public void updateTwinFields(TwinEntity twinEntity, List<FieldValue> values) throws ServiceException {
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        updateTwinFields(twinEntity, values, twinChangesCollector);
        twinChangesService.applyChanges(twinChangesCollector);
    }

    public void updateTwinFields(TwinEntity twinEntity, List<FieldValue> values, TwinChangesCollector twinChangesCollector) throws ServiceException {
        TwinField twinField;
        for (FieldValue fieldValue : values) {
            twinField = wrapField(twinEntity, fieldValue.getTwinClassField());
            var fieldTyper = featurerService.getFeaturer(twinField.getTwinClassField().getFieldTyperFeaturerId(), FieldTyper.class);
            fieldTyper.serializeValue(twinEntity, fieldValue, twinChangesCollector);
        }
    }

    public void updateTwin(TwinUpdate twinUpdate) throws ServiceException {
        updateTwin(List.of(twinUpdate), false);
    }

    public List<TwinEntity> updateTwin(List<TwinUpdate> twinUpdates, boolean validateAll) throws ServiceException {
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        TwinBatchFieldValidationException batchFieldValidationException = null;
        for (TwinUpdate twinUpdate : twinUpdates) {
            if (!twinUpdate.isChanged()) continue;

            ChangesRecorder<TwinEntity, TwinEntity> changesRecorder = new ChangesRecorder<>(
                    twinUpdate.getDbTwinEntity(),
                    twinUpdate.getTwinEntity(),
                    twinUpdate.getDbTwinEntity(),
                    twinChangesCollector.getHistoryCollector(twinUpdate.getDbTwinEntity()));

            try {
                updateTwin(twinUpdate, twinChangesCollector, changesRecorder);
            } catch (TwinFieldValidationException validationException) {
                if (batchFieldValidationException == null)
                    batchFieldValidationException = new TwinBatchFieldValidationException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT);
                batchFieldValidationException.addInvalidFields(validationException.getTwinId(), validationException.getInvalidFields());
                if (!validateAll || twinUpdates.size() == 1) {
                    break;
                }
            }
        }
        if (batchFieldValidationException != null) {
            throw batchFieldValidationException;
        }
        twinChangesService.applyChanges(twinChangesCollector);

        return twinUpdates.stream().map(TwinUpdate::getDbTwinEntity).toList();
    }

    public void updateTwin(TwinUpdate twinUpdate, TwinChangesCollector twinChangesCollector, ChangesRecorder<TwinEntity, ?> twinChangesRecorder) throws ServiceException {
        if (!twinUpdate.isChanged())
            return;
        ApiUser apiUser = authService.getApiUser();
        if (twinUpdate.isCheckEditPermission())
            checkUpdatePermission(twinUpdate.getDbTwinEntity(), apiUser);
        if (twinUpdate.getTwinEntity().getTwinClassId() == null && twinUpdate.getDbTwinEntity() != null) {
            twinUpdate.getTwinEntity()
                    .setTwinClassId(twinUpdate.getDbTwinEntity().getTwinClassId())
                    .setTwinClass(twinUpdate.getDbTwinEntity().getTwinClass());
        }
        tryToFinalizeSketch(twinUpdate);
        runFactoryOnUpdate(twinUpdate);
        updateTwinBasics(twinChangesRecorder);
        if (twinChangesRecorder.hasChanges())
            twinChangesCollector.add(twinChangesRecorder.getRecorder());
        if (MapUtils.isNotEmpty(twinUpdate.getFields())) {
            validateFields(twinUpdate.getDbTwinEntity(), twinUpdate.getFields());
            updateTwinFields(twinChangesRecorder.getDbEntity(), twinUpdate.getFields().values().stream().toList(), twinChangesCollector);
        }
        attachmentService.cudAttachments(twinUpdate.getDbTwinEntity(), twinUpdate.getAttachmentCUD(), twinChangesCollector);
        twinLinkService.cudTwinLinks(twinUpdate.getDbTwinEntity(), twinUpdate.getTwinLinkCUD(), twinChangesCollector);
        twinMarkerService.addMarkers(twinUpdate.getDbTwinEntity(), twinUpdate.getMarkersAdd(), twinChangesCollector);
        twinMarkerService.deleteMarkers(twinUpdate.getDbTwinEntity(), twinUpdate.getMarkersDelete(), twinChangesCollector);
        twinTagService.updateTwinTags(twinUpdate.getDbTwinEntity(), twinUpdate.getTagsDelete(), twinUpdate.getTagsAddNew(), twinUpdate.getTagsAddExisted(), twinChangesCollector);
        twinFieldAttributeService.cudAttributes(twinUpdate.getDbTwinEntity(), twinUpdate.getTwinFieldAttributeCUD(), twinChangesCollector);
        runFactoryAfterUpdate(twinUpdate, twinChangesCollector);
    }

    private void tryToFinalizeSketch(TwinUpdate twinUpdate) throws ServiceException {
        if (!twinUpdate.getMode().equals(TwinUpdate.Mode.sketchUpdate)) {
            return;
        }
        UUID twinUpdateStatus = twinUpdate.getTwinEntity().getTwinStatusId();
        //perhaps we can finalize the sketch
        if (isAllRequiredFieldsFilled(twinUpdate)) {
            log.info("All required fields for sketch {} is filled", twinUpdate.getDbTwinEntity().logNormal());
            if (twinUpdateStatus == null || twinStatusService.isSketch(twinUpdateStatus)) //perhaps we need to check only if this is initSketchStatus
                setInitStatus(twinUpdate.getTwinEntity());
            twinUpdate.setMode(TwinUpdate.Mode.sketchFinalize);
        } else if (twinUpdate.getTwinEntity().getTwinStatusId() != null
                && !twinStatusService.isSketch(twinUpdateStatus)
                && !twinUpdate.getDbTwinEntity().getTwinStatusId().equals(twinUpdate.getTwinEntity().getTwinStatusId())) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, "{} can not change status to {}, because not all of required fields are filled", twinUpdate.getDbTwinEntity().logDetailed(), twinUpdate.getTwinEntity().getTwinStatusId());
        }
    }

    private boolean isAllRequiredFieldsFilled(TwinUpdate twinUpdate) throws ServiceException {
        loadFieldsValues(twinUpdate.getDbTwinEntity());
        for (var classField : twinUpdate.getDbTwinEntity().getTwinClass().getTwinClassFieldKit()) {
            if (Boolean.TRUE.equals(classField.getRequired())
                    && !(twinUpdate.getDbTwinEntity().getFieldValuesKit().containsKey(classField.getId()) && twinUpdate.getDbTwinEntity().getFieldValuesKit().get(classField.getId()).isFilled())
                    && (twinUpdate.getField(classField.getId()) == null || !twinUpdate.getField(classField.getId()).isFilled())
            ) {
                return false;
            }
        }
        return true;
    }

    public boolean isAllRequiredFieldsFilled(TwinEntity twinEntity) throws ServiceException {
        loadFieldsValues(twinEntity);
        for (var classField : twinEntity.getTwinClass().getTwinClassFieldKit())
            if (Boolean.TRUE.equals(classField.getRequired()) && !(twinEntity.getFieldValuesKit().containsKey(classField.getId()) && twinEntity.getFieldValuesKit().get(classField.getId()).isFilled()))
                return false;
        return true;
    }

    private void runFactoryOnCreate(TwinCreate twinCreate) throws ServiceException {
        if (twinCreate.getLauncher() != TwinOperation.Launcher.direct) {
            return;
        }
        FactoryLauncher factoryLauncher = twinCreate.isSketchMode() ? FactoryLauncher.onSketchCreate : FactoryLauncher.onTwinCreate;
        twinflowFactoryService.runFactoryOn(twinCreate, factoryLauncher);
    }

    private void runFactoryAfterCreate(TwinCreate twinCreate, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinCreate.getLauncher() != TwinOperation.Launcher.direct) {
            return;
        }
        FactoryLauncher factoryLauncher = twinCreate.isSketchMode() ? FactoryLauncher.afterSketchCreate : FactoryLauncher.afterTwinCreate;
        twinflowFactoryService.runFactoryAfter(twinCreate, twinChangesCollector, factoryLauncher);
    }

    private void runFactoryOnUpdate(TwinUpdate twinUpdate) throws ServiceException {
        if (twinUpdate.getLauncher() != TwinOperation.Launcher.direct) {
            return;
        }
        FactoryLauncher factoryLauncher = switch (twinUpdate.getMode()) {
            case twinUpdate -> FactoryLauncher.onTwinUpdate;
            case sketchUpdate -> FactoryLauncher.onSketchUpdate;
            case sketchFinalize -> FactoryLauncher.onSketchFinalize;
            default -> null;
        };
        if (factoryLauncher == null)
            return;
        twinflowFactoryService.runFactoryOn(twinUpdate, factoryLauncher);
        if (factoryLauncher.equals(FactoryLauncher.onSketchFinalize)
                && twinUpdate.getTwinEntity().isSketch()) {
            twinUpdate.setMode(TwinUpdate.Mode.sketchFinalizeRestricted);
        }
    }

    private void runFactoryAfterUpdate(TwinUpdate twinUpdate, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinUpdate.getLauncher() != TwinOperation.Launcher.direct) {
            return;
        }
        FactoryLauncher factoryLauncher = switch (twinUpdate.getMode()) {
            case twinUpdate -> FactoryLauncher.afterTwinUpdate;
            case sketchUpdate -> FactoryLauncher.afterSketchUpdate;
            case sketchFinalize -> FactoryLauncher.afterSketchFinalize;
            case sketchFinalizeRestricted -> FactoryLauncher.afterSketchFinalizeRestricted;
        };
        twinflowFactoryService.runFactoryAfter(twinUpdate, twinChangesCollector, factoryLauncher);
    }


    public void updateTwinBasics(ChangesRecorder<TwinEntity, ?> changesRecorder) throws ServiceException {
        updateTwinHead(changesRecorder);
        updateTwinName(changesRecorder);
        updateTwinDescription(changesRecorder);
        updateTwinExternalId(changesRecorder);
        updateTwinAssignee(changesRecorder);
        updateTwinStatus(changesRecorder);
    }

    public void updateTwinAssignee(ChangesRecorder<TwinEntity, ?> changesRecorder) throws ServiceException {
        if (changesRecorder.isChanged("assignerUser", changesRecorder.getDbEntity().getAssignerUserId(), changesRecorder.getUpdateEntity().getAssignerUserId())) {
            UserEntity newAssignee = null;
            if (!UuidUtils.isNullifyMarker(changesRecorder.getUpdateEntity().getAssignerUserId())) {
                newAssignee = changesRecorder.getUpdateEntity().getAssignerUser();
            }
            checkAssignee(changesRecorder.getDbEntity(), newAssignee != null ? newAssignee.getId() : null);
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
            TwinEntity headTwin = twinHeadService.checkValidHeadForClass(changesRecorder.getUpdateEntity().getHeadTwinId(), changesRecorder.getDbEntity().getTwinClass());
            if (changesRecorder.getRecorder() instanceof DraftTwinPersistEntity draftTwinPersistEntity)
                draftTwinPersistEntity
                        .setHeadTwinId(headTwin.getId()); //todo check permissionSchemaSpace is updated on db level
            if (changesRecorder.getRecorder() instanceof TwinEntity twinEntity)
                twinEntity
                        .setHeadTwinId(headTwin.getId())
                        .setPermissionSchemaSpaceId(getPermissionSchemaSpaceId(headTwin));
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

    public void updateTwinExternalId(ChangesRecorder<TwinEntity, ?> changesRecorder) {
        if (changesRecorder.isChanged(TwinEntity.Fields.externalId, changesRecorder.getDbEntity().getExternalId(), changesRecorder.getUpdateEntity().getExternalId())) {
            if (changesRecorder.isHistoryCollectorEnabled())
                changesRecorder.getHistoryCollector().add(historyService.externalIdChanged(changesRecorder.getDbEntity().getExternalId(), changesRecorder.getUpdateEntity().getExternalId()));
            if (changesRecorder.getRecorder() instanceof DraftTwinPersistEntity draftTwinPersistEntity)
                draftTwinPersistEntity
                        .setExternalId(changesRecorder.getUpdateEntity().getExternalId());
            if (changesRecorder.getRecorder() instanceof TwinEntity twinEntity)
                twinEntity
                        .setExternalId(changesRecorder.getUpdateEntity().getExternalId());
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
//            var fieldTyper = featurerService.getFeaturer(twinFieldEntity.getTwinClassField().getFieldTyperFeaturerId(), FieldTyper.class);
//            fieldTyper.serializeValue(twinFieldEntity, fieldValue, fieldChangesHelper);
//            if (fieldChangesHelper.hasChanges()) {
//                twinFieldEntityList.add(twinFieldEntity);
//                twinChangesHelper.addAll(fieldChangesHelper);
//            }
//        }
//        saveTwinFields(twinFieldEntityList, twinChangesHelper);
//    }

    public TwinEntity loadSpaceForTwin(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getSpaceTwin() != null)
            return twinEntity.getSpaceTwin();
        loadHeadForTwin(twinEntity);
        if (twinEntity.getHeadTwin() == null)
            return null;
        twinEntity.setSpaceTwin(findSpaceForTwin(twinEntity, twinEntity.getHeadTwin(), 10));
        return twinEntity.getSpaceTwin();
    }

    public TwinEntity loadHeadForTwin(TwinEntity twinEntity) throws ServiceException {
        loadHeadForTwin(Collections.singletonList(twinEntity));
        return twinEntity.getHeadTwin();
    }

    public void loadHeadForTwin(Collection<TwinEntity> srcCollection) throws ServiceException {
        KitGrouped<TwinEntity, UUID, UUID> needLoad = new KitGrouped<>(TwinEntity::getId, TwinEntity::getHeadTwinId);
        for (var twin : srcCollection) {
            if (twin.getHeadTwin() != null || twin.getHeadTwinId() == null)
                continue;
            needLoad.add(twin);
        }
        if (KitUtils.isEmpty(needLoad))
            return;
        Kit<TwinEntity, UUID> heads = findEntitiesSafe(needLoad.getGroupedKeySet());
        for (var twin : needLoad) {
            twin.setHeadTwin(heads.get(twin.getHeadTwinId()));
        }
    }

    public void loadSegments(TwinEntity twinEntity) {
        loadSegments(Collections.singletonList(twinEntity));
    }

    public void loadSegments(Collection<TwinEntity> srcCollection) {
        Kit<TwinEntity, UUID> needLoad = new Kit<>(TwinEntity::getId);
        for (var twin : srcCollection) {
            if (twin.getSegments() != null) {
                continue;
            } else if (Boolean.FALSE.equals(twin.getTwinClass().getHasSegment())) {
                twin.setSegments(Kit.EMPTY);
            }
            needLoad.add(twin);
        }
        if (KitUtils.isEmpty(needLoad))
            return;
        KitGrouped<TwinEntity, UUID, UUID> segments = new KitGrouped<>(
                twinRepository.findSegments(needLoad.getIdSet()), TwinEntity::getId, TwinEntity::getHeadTwinId);
        for (var twin : needLoad) {
            if (segments.containsGroupedKey(twin.getId())) {
                twin.setSegments(new Kit<>(segments.getGrouped(twin.getId()), TwinEntity::getId));
            } else {
                twin.setSegments(Kit.EMPTY);
            }
        }
    }

    protected TwinEntity findSpaceForTwin(TwinEntity twinEntity, TwinEntity headTwin, int recursionDepth) throws ServiceException {
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
        FieldTyper fieldTyper = featurerService.getFeaturer(twinField.getTwinClassField().getFieldTyperFeaturerId(), FieldTyper.class);
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
        if (KitUtils.isNotEmpty(srcTwin.getTwinFieldSimpleKit())) {
            for (TwinFieldSimpleEntity twinFieldEntity : srcTwin.getTwinFieldSimpleKit().getCollection()) {
                TwinFieldSimpleEntity duplicateTwinFieldBasicEntity = twinFieldEntity.cloneFor(dstTwinEntity);
                twinChangesCollector.add(duplicateTwinFieldBasicEntity);
            }
        }
        if (KitUtils.isNotEmpty(srcTwin.getTwinFieldSimpleNonIndexedKit())) {
            for (TwinFieldSimpleNonIndexedEntity twinFieldSimpleNonIndexedEntity : srcTwin.getTwinFieldSimpleNonIndexedKit().getCollection()) {
                TwinFieldSimpleNonIndexedEntity duplicateTwinFieldNonIndexedEntity = twinFieldSimpleNonIndexedEntity.cloneFor(dstTwinEntity);
                twinChangesCollector.add(duplicateTwinFieldNonIndexedEntity);
            }
        }
        if (KitUtils.isNotEmpty(srcTwin.getTwinFieldUserKit())) {
            for (TwinFieldUserEntity twinFieldUserEntity : srcTwin.getTwinFieldUserKit().getCollection()) {
                TwinFieldUserEntity duplicateTwinFieldUserEntity = twinFieldUserEntity.cloneFor(dstTwinEntity);
                twinChangesCollector.add(duplicateTwinFieldUserEntity);
            }
        }
        if (KitUtils.isNotEmpty(srcTwin.getTwinFieldDatalistKit())) {
            for (TwinFieldDataListEntity twinFieldDatalistEntity : srcTwin.getTwinFieldDatalistKit().getCollection()) {
                TwinFieldDataListEntity duplicateTwinFieldUserEntity = twinFieldDatalistEntity.cloneFor(dstTwinEntity);
                twinChangesCollector.add(duplicateTwinFieldUserEntity);
            }
        }
        if (KitUtils.isNotEmpty(srcTwin.getTwinFieldI18nKit())) {
            for (TwinFieldI18nEntity twinFieldEntity : srcTwin.getTwinFieldI18nKit().getCollection()) {
                TwinFieldI18nEntity duplicateTwinFieldI18nEntity = twinFieldEntity.cloneFor(dstTwinEntity);
                twinChangesCollector.add(duplicateTwinFieldI18nEntity);
            }
        }
        if (KitUtils.isNotEmpty(srcTwin.getTwinFieldBooleanKit())) {
            for (TwinFieldBooleanEntity twinFieldBooleanEntity : srcTwin.getTwinFieldBooleanKit().getCollection()) {
                TwinFieldBooleanEntity duplicateTwinFieldBooleanEntity = twinFieldBooleanEntity.cloneFor(dstTwinEntity);
                twinChangesCollector.add(duplicateTwinFieldBooleanEntity);
            }
        }
        if (KitUtils.isNotEmpty(srcTwin.getTwinFieldTwinClassKit())) {
            for (TwinFieldTwinClassEntity twinFieldTwinClassEntity : srcTwin.getTwinFieldTwinClassKit().getCollection()) {
                TwinFieldTwinClassEntity duplicateTwinFieldTwinClassEntity = twinFieldTwinClassEntity.cloneFor(dstTwinEntity);
                twinChangesCollector.add(duplicateTwinFieldTwinClassEntity);
            }
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

    public TwinFieldBooleanEntity createTwinFieldBooleanEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity, Boolean value) {
        return new TwinFieldBooleanEntity()
                .setTwinClassField(twinClassFieldEntity)
                .setTwinClassFieldId(twinClassFieldEntity.getId())
                .setTwin(twinEntity)
                .setTwinId(twinEntity.getId())
                .setValue(value);
    }

    public TwinFieldSimpleNonIndexedEntity createTwinFieldNonIndexedEntity(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity, String value) {
        return new TwinFieldSimpleNonIndexedEntity()
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
        if (newTwinId == null)
            newTwinId = UuidCreator.getTimeOrderedEpoch();
        TwinEntity duplicateEntity = fillDuplicate(srcTwin, newTwinId);
        duplicateEntity = createTwin(duplicateEntity);
        cloneTwinFieldListAndSave(srcTwin, duplicateEntity);
        twinflowService.runTwinStatusTransitionTriggers(duplicateEntity, null, duplicateEntity.getTwinStatus());
        return duplicateEntity;
    }

    public TwinDuplicate createDuplicateTwin(UUID srcTwinId, UUID newTwinId) throws ServiceException {
        return createDuplicateTwin(
                findEntity(srcTwinId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.none),
                newTwinId);
    }

    public TwinDuplicate createDuplicateTwin(TwinEntity srcTwin, UUID newTwinId) throws ServiceException {
        TwinDuplicate twinDuplicate = new TwinDuplicate();
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        TwinEntity duplicateEntity = fillDuplicate(srcTwin, newTwinId);
        createTwin(duplicateEntity, twinChangesCollector);
        cloneTwinFields(srcTwin, duplicateEntity, twinChangesCollector);
        twinDuplicate.setDuplicate(duplicateEntity);
        twinDuplicate.setChangesCollector(twinChangesCollector);
        return twinDuplicate;
    }

    @NotNull
    private TwinEntity fillDuplicate(TwinEntity srcTwin, UUID newTwinId) throws ServiceException {
        TwinEntity duplicateEntity = srcTwin.clone();
        fillOwner(duplicateEntity);
        duplicateEntity
                .setId(newTwinId)
                .setCreatedByUserId(authService.getApiUser().getUserId())
                .setCreatedAt(null); // no sense to use created at from original twin
        if (duplicateEntity.getAssignerUserId() == null && Boolean.TRUE.equals(duplicateEntity.getTwinClass().getAssigneeRequired())) {
            duplicateEntity
                    .setAssignerUserId(authService.getApiUser().getUserId())
                    .setAssignerUser(authService.getApiUser().getUser());
        }
        return duplicateEntity;
    }

    @Transactional
    public void saveDuplicateTwin(TwinDuplicate twinDuplicate) throws ServiceException {
        twinChangesService.applyChanges(twinDuplicate.getChangesCollector());
        twinflowService.runTwinStatusTransitionTriggers(twinDuplicate.getDuplicate(), null, twinDuplicate.getDuplicate().getTwinStatus());
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
        FieldTyper fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturerId(), FieldTyper.class);
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
        if (fieldTyper.getValueType() == FieldValueAttachment.class)
            fieldValue = new FieldValueAttachment(twinClassFieldEntity);
        if (fieldTyper.getValueType() == FieldValueI18n.class)
            fieldValue = new FieldValueI18n(twinClassFieldEntity);
        if (fieldTyper.getValueType() == FieldValueBoolean.class)
            fieldValue = new FieldValueBoolean(twinClassFieldEntity);
        if (fieldTyper.getValueType() == FieldValueTwinClassList.class)
            fieldValue = new FieldValueTwinClassList(twinClassFieldEntity);
        if (fieldValue == null)
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "unknown fieldValue[" + fieldTyper.getValueType() + "]");

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
            fieldValueDate.setDateStr(value);
        if (fieldValue instanceof FieldValueBoolean fieldValueBoolean)
            fieldValueBoolean.setValue(Boolean.parseBoolean(value));
        if (fieldValue instanceof FieldValueAttachment fieldValueAttachment) {
            // Parse the value as JSON to extract name and base64Content
            // For simplicity, we'll assume the value is in the format "name:base64Content"
            if (value != null && value.contains(":")) {
                String[] parts = value.split(":", 2);
                fieldValueAttachment.setName(parts[0]);
                fieldValueAttachment.setBase64Content(value);
            } else {
                fieldValueAttachment.setName("data");
                fieldValueAttachment.setBase64Content(value);
            }
        }
        if (fieldValue instanceof FieldValueTwinClassList fieldValueTwinClassList) {
            for (var id : value.split(LIST_SPLITTER)) {
                if (StringUtils.isEmpty(id)) {
                    continue;
                }

                UUID uuid;
                try {
                    uuid = UUID.fromString(id);
                } catch (Exception e) {
                    throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, fieldValueTwinClassList.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " incorrect class id[" + id + "]");
                }
                fieldValueTwinClassList.getTwinClassEntities().add(new TwinClassEntity().setId(uuid));
            }
        }
        if (fieldValue instanceof FieldValueSelect fieldValueSelect) {
            for (String dataListOption : value.split(LIST_SPLITTER)) {
                if (StringUtils.isEmpty(dataListOption)) continue;
                DataListOptionEntity dataListOptionEntity = new DataListOptionEntity();
                if (UuidUtils.isUUID(dataListOption)) {
                    dataListOptionEntity.setId(UUID.fromString(dataListOption));
                } else if (dataListOption.startsWith(FieldTyperList.EXTERNAL_ID_PREFIX)) {
                    dataListOptionEntity.setExternalId(StringUtils.substringAfter(dataListOption, FieldTyperList.EXTERNAL_ID_PREFIX));
                } else {
                    dataListOptionEntity.setOption(dataListOption);
                }
                fieldValueSelect.add(dataListOptionEntity);
            }
        }
        if (fieldValue instanceof FieldValueUser fieldValueUser) {
            for (String userId : value.split(LIST_SPLITTER)) {
                if (StringUtils.isEmpty(userId))
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
        if (fieldValue instanceof FieldValueUserSingle fieldValueUser) {
            UUID userId = UuidUtils.fromString(value);
            fieldValueUser.setUser(new UserEntity().setId(userId));
        }
        if (fieldValue instanceof FieldValueStatusSingle fieldValueStatus) {
            UUID statusId = UuidUtils.fromString(value);
            fieldValueStatus.setStatus(new TwinStatusEntity().setId(statusId));
        }
        if (fieldValue instanceof FieldValueLink fieldValueLink) {
            for (String dstTwinId : value.split(LIST_SPLITTER)) {
                if (StringUtils.isEmpty(dstTwinId))
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
        if (fieldValue instanceof FieldValueLinkSingle fieldValueLink) {
            UUID twinId = UuidUtils.fromString(value);
            fieldValueLink.setDstTwin(new TwinEntity().setId(twinId));
        }
        if (fieldValue instanceof FieldValueI18n fieldValueI18n) {
            Map<Locale, String> translations = JsonUtils.jsonToTranslationsMap(value);
            if (translations == null) {
                throw new ServiceException(
                        ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                        fieldValueI18n.getTwinClassField().logShort() + " can't deserialize i18n");
            }
            fieldValueI18n.setTranslations(translations);
        }
    }

    public FieldValue copyToField(FieldValue src, UUID dstTwinClassFieldId) throws ServiceException {
        TwinClassFieldEntity dstTwinClassField = twinClassFieldService.findEntitySafe(dstTwinClassFieldId);
        if (!isCopyable(src.getTwinClassField(), dstTwinClassField))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, src.getTwinClassField().logShort()
                    + " value can not be copied to " + dstTwinClassField.logShort());
        return src.clone(dstTwinClassField);
    }


    //TODO ft params equals(data list scope)
    public boolean isCopyable(TwinClassFieldEntity src, TwinClassFieldEntity dst) throws ServiceException {
        FieldTyper srcFieldTyper = featurerService.getFeaturer(src.getFieldTyperFeaturerId(), FieldTyper.class);
        FieldTyper dstFieldTyper = featurerService.getFeaturer(dst.getFieldTyperFeaturerId(), FieldTyper.class);
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

        if (twinClassEntity.getExtendsTwinClassId() == null) {
            return result;
        }

        twinClassService.loadExtendsTwinClass(twinClassEntity);
        TwinClassEntity extendsTwinClassEntity = twinClassEntity.getExtendsTwinClass();
        twinClassFieldService.loadTwinClassFields(extendsTwinClassEntity);

        if (extendsTwinClassEntity.getTwinClassFieldKit().isEmpty()) {
            return result;
        }
        if (skipFromTwinClass != null) {
            twinClassFieldService.loadTwinClassFields(skipFromTwinClass);
        }
        if (!showUsedOnly) {
            for (TwinClassFieldEntity inheritedTwinClassFieldEntity : extendsTwinClassEntity.getTwinClassFieldKit().getCollection()) {
                if (skipFromTwinClass != null && skipFromTwinClass.getTwinClassFieldKit().containsKey(inheritedTwinClassFieldEntity.getId()))
                    continue;
                result.add(inheritedTwinClassFieldEntity);
            }
        }

        Set<UUID> inheritedTwinClassFieldIds = new HashSet<>();
        Map<TwinFieldStorage, Set<UUID>> inheritedTwinClassFieldIdsByStorage = new HashMap<>();

        for (TwinClassFieldEntity inheritedTwinClassFieldEntity : extendsTwinClassEntity.getTwinClassFieldKit().getCollection()) {
            if (skipFromTwinClass != null && skipFromTwinClass.getTwinClassFieldKit().containsKey(inheritedTwinClassFieldEntity.getId()))
                continue;
            var fieldTyper = featurerService.getFeaturer(inheritedTwinClassFieldEntity.getFieldTyperFeaturerId(), FieldTyper.class);
            TwinFieldStorage twinFieldStorage = fieldTyper.getStorage(inheritedTwinClassFieldEntity);
            inheritedTwinClassFieldIdsByStorage
                    .computeIfAbsent(twinFieldStorage, k -> new HashSet<>())
                    .add(inheritedTwinClassFieldEntity.getId());
        }

        if (inheritedTwinClassFieldIdsByStorage.isEmpty()) {
            return result;
        }

        for (var entry : inheritedTwinClassFieldIdsByStorage.entrySet()) {
            inheritedTwinClassFieldIds.addAll(entry.getKey().findUsedFields(twinClassEntity.getId(), entry.getValue()));
        }
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
        twinFieldSimpleNonIndexedRepository.deleteByTwin_TwinClassIdAndTwinClassFieldIdIn(twinClassId, twinClassFieldIds);
        twinFieldUserRepository.deleteByTwin_TwinClassIdAndTwinClassFieldIdIn(twinClassId, twinClassFieldIds);
        twinFieldDataListRepository.deleteByTwin_TwinClassIdAndTwinClassFieldIdIn(twinClassId, twinClassFieldIds);
        twinFieldI18nRepository.deleteByTwin_TwinClassIdAndTwinClassFieldIdIn(twinClassId, twinClassFieldIds);
        twinFieldBooleanRepository.deleteByTwin_TwinClassIdAndTwinClassFieldIdIn(twinClassId, twinClassFieldIds);
        twinFieldTwinClassListRepository.deleteByTwin_TwinClassIdAndTwinClassFieldIdIn(twinClassId, twinClassFieldIds);

        log.info("Twin class fields [" + StringUtils.join(twinClassFieldIds, ",") + "] perhaps were deleted from all twins of class[" + twinClassId + "]");
    }

    public void convertFieldsForTwinsOfClass(TwinClassEntity twinClassEntity, TwinClassFieldEntity twinClassFieldForReplace, TwinClassFieldEntity twinClassFieldReplacement) throws ServiceException {
        if (!twinClassFieldService.isConvertable(twinClassFieldForReplace, twinClassFieldReplacement)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_INCORRECT_TYPE, twinClassFieldForReplace.logNormal() + " can not be converted to " + twinClassFieldReplacement.logNormal());
        }

        var fieldTyper = featurerService.getFeaturer(twinClassFieldForReplace.getFieldTyperFeaturerId(), FieldTyper.class);
        fieldTyper.getStorage(twinClassFieldForReplace).replaceTwinClassFieldForTwinsOfClass(twinClassEntity.getId(), twinClassFieldForReplace.getId(), twinClassFieldReplacement.getId());
        if (fieldTyper.getStorageType() == TwinFieldStorageSimple.class) {
            twinFieldSimpleRepository.replaceTwinClassFieldForTwinsOfClass(twinClassEntity.getId(), twinClassFieldForReplace.getId(), twinClassFieldReplacement.getId());
        } else if (fieldTyper.getStorageType() == TwinFieldStorageSimpleNonIndex.class) {
            twinFieldSimpleNonIndexedRepository.replaceTwinClassFieldForTwinsOfClass(twinClassEntity.getId(), twinClassFieldForReplace.getId(), twinClassFieldReplacement.getId());
        } else if (fieldTyper.getStorageType() == TwinFieldStorageUser.class) {
            twinFieldUserRepository.replaceTwinClassFieldForTwinsOfClass(twinClassEntity.getId(), twinClassFieldForReplace.getId(), twinClassFieldReplacement.getId());
        } else if (fieldTyper.getStorageType() == TwinFieldStorageDatalist.class) {
            twinFieldDataListRepository.replaceTwinClassFieldForTwinsOfClass(twinClassEntity.getId(), twinClassFieldForReplace.getId(), twinClassFieldReplacement.getId());
        } else if (fieldTyper.getStorageType() == TwinFieldStorageI18n.class) {
            twinFieldI18nRepository.replaceTwinClassFieldForTwinsOfClass(twinClassEntity.getId(), twinClassFieldForReplace.getId(), twinClassFieldReplacement.getId());
        } else if (fieldTyper.getStorageType() == TwinFieldStorageBoolean.class) {
            twinFieldBooleanRepository.replaceTwinClassFieldForTwinsOfClass(twinClassEntity.getId(), twinClassFieldForReplace.getId(), twinClassFieldReplacement.getId());
        } else if (fieldTyper.getStorageType() == TwinFieldStorageTwinClassList.class) {
            twinFieldTwinClassListRepository.replaceTwinClassFieldForTwinsOfClass(twinClassEntity.getId(), twinClassFieldForReplace.getId(), twinClassFieldReplacement.getId());
        }
    }

    public String getValueFromTwinClassField(TwinEntity twin, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        String res = null;

        if (twin == null || twinClassFieldEntity == null) {
            return res;
        }

        var fieldValue = getTwinFieldValue(twin, twinClassFieldEntity.getId());

        if (fieldValue instanceof FieldValueText text) {
            res = text.getValue();
        } else if (fieldValue instanceof FieldValueUser user) {
            res = user.getUsers().stream()
                    .map(UserEntity::getName)
                    .collect(Collectors.joining(", "));
        } else if (fieldValue instanceof FieldValueSelect select) {
            res = select.getOptions().stream()
                    .map(DataListOptionEntity::getOption)
                    .collect(Collectors.joining(", "));
        } else if (fieldValue instanceof FieldValueI18n i18n) {
            res = i18n.getTranslations().get(Locale.getDefault());
        } else if (fieldValue instanceof FieldValueBoolean bool) {
            res = bool.toString();
        } else if (fieldValue instanceof FieldValueTwinClassList classList) {
            res = classList.getTwinClassEntities().stream()
                    .map(entity -> i18nService.translateToLocale(entity.getNameI18NId()))
                    .collect(Collectors.joining(", "));
        }

        return res;
    }

    public TwinStatusEntity getStatusOrFreeze(TwinEntity src) throws ServiceException {
        if (src.getTwinClass().getTwinClassFreezeId() != null) {
            twinClassService.loadFreeze(src.getTwinClass());
            return src.getTwinClass().getTwinClassFreeze().getTwinStatus();
        }
        return src.getTwinStatus();
    }

    public boolean checkIsFreezeStatus(TwinEntity src) {
        return src.getTwinClass().getTwinClassFreezeId() != null;
    }

    @Data
    @Accessors(chain = true)
    public static class TwinCreateResult {
        private TwinEntity createdTwin;
        private List<TwinAliasEntity> twinAliasEntityList;
    }

    @Data
    public static class TwinBatchCreateResult {
        private List<TwinCreateResult> twinCreateResultList = new ArrayList<>();
    }

    @Data
    @Accessors(chain = true)
    public static class TwinUpdateResult {
        private TwinEntity updatedTwin;
    }

    @Data
    public static class CloneFieldsResult {
        List<TwinFieldSimpleEntity> fieldEntityList;
        List<TwinFieldSimpleNonIndexedEntity> fieldNonIndexedEntityList;
        List<TwinFieldDataListEntity> fieldDataListEntityList;
        List<TwinFieldUserEntity> fieldUserEntityList;
        List<TwinFieldI18nEntity> fieldI18nEntityList;
        List<TwinFieldBooleanEntity> fieldBooleanEntityList;
        List<TwinFieldTwinClassEntity> fieldTwinClassListEntityList;

        public CloneFieldsResult add(TwinFieldSimpleEntity cloneTwinFieldEntity) {
            fieldEntityList = CollectionUtils.safeAdd(fieldEntityList, cloneTwinFieldEntity);
            return this;
        }

        public CloneFieldsResult add(TwinFieldSimpleNonIndexedEntity cloneTwinFieldNonIndexedEntity) {
            fieldNonIndexedEntityList = CollectionUtils.safeAdd(fieldNonIndexedEntityList, cloneTwinFieldNonIndexedEntity);
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

        public CloneFieldsResult add(TwinFieldI18nEntity cloneTwinFieldI18nEntity) {
            fieldI18nEntityList = CollectionUtils.safeAdd(fieldI18nEntityList, cloneTwinFieldI18nEntity);
            return this;
        }

        public CloneFieldsResult add(TwinFieldBooleanEntity cloneTwinFieldBooleanEntity) {
            fieldBooleanEntityList = CollectionUtils.safeAdd(fieldBooleanEntityList, cloneTwinFieldBooleanEntity);
            return this;
        }

        public CloneFieldsResult add(TwinFieldTwinClassEntity twinFieldTwinClassEntity) {
            fieldTwinClassListEntityList = CollectionUtils.safeAdd(fieldTwinClassListEntityList, twinFieldTwinClassEntity);
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

    public void validateFields(TwinCreate twinCreate) throws ServiceException {
        if (twinCreate.isSketchMode()) {
            setInitSketchStatus(twinCreate.getTwinEntity());
        }
        validateFields(twinCreate.getTwinEntity(), twinCreate.getFields());
    }

    public void validateFields(TwinEntity twinEntity, Map<UUID, FieldValue> fields) throws ServiceException {
        if (twinEntity.getTwinClass() == null) {
            twinEntity.setTwinClass(twinClassService.findEntitySafe(twinEntity.getTwinClassId()));
        }
        twinClassFieldService.loadTwinClassFields(twinEntity.getTwinClass());
        Map<UUID, String> invalidFieldIds = new HashMap<>();
        for (TwinClassFieldEntity twinClassFieldEntity : twinEntity.getTwinClass().getTwinClassFieldKit().getCollection()) {
            ValidationResult validationResult = validateField(twinEntity, fields, twinClassFieldEntity);
            if (!validationResult.isValid()) {
                invalidFieldIds.put(twinClassFieldEntity.getId(), validationResult.getMessage());
            }
        }
        if (!invalidFieldIds.isEmpty()) {
            throw new TwinFieldValidationException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT, twinEntity.getId(), invalidFieldIds);
        }
    }

    private ValidationResult validateField(TwinEntity twinEntity, Map<UUID, FieldValue> fields, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        Optional<FieldValue> fieldValue = getFieldValueSafe(twinEntity, fields, twinClassFieldEntity);
        if (fieldValue.isEmpty())
            return new ValidationResult(true);
        FieldTyper fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturerId(), FieldTyper.class);
        return fieldTyper.validate(twinEntity, fieldValue.get());
    }

    private Optional<FieldValue> getFieldValueSafe(TwinEntity twinEntity, Map<UUID, FieldValue> fields, TwinClassFieldEntity twinClassFieldEntity) {
        FieldValue fieldValue = fields.get(twinClassFieldEntity.getId());
        if (fieldValue == null || !fieldValue.isFilled()) {
            return Optional.empty();
        }
        return Optional.of(fieldValue);
    }

    public void loadClass(TwinEntity src) throws ServiceException {
        loadClass(Collections.singletonList(src));
    }

    public void loadClass(Collection<TwinEntity> collection) throws ServiceException {
        KitGrouped<TwinEntity, UUID, UUID> needLoad = new KitGrouped<>(TwinEntity::getId, TwinEntity::getTwinClassId);
        for (var entry : collection) {
            if (entry.getTwinClass() == null)
                needLoad.add(entry);
        }
        if (KitUtils.isEmpty(needLoad))
            return;
        Kit<TwinClassEntity, UUID> items = twinClassService.findEntitiesSafe(needLoad.getGroupedKeySet());
        for (var twin : needLoad) {
            twin.setTwinClass(items.get(twin.getTwinClassId()));
        }
    }

}
