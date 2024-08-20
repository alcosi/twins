package org.twins.core.service.draft;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.LTreeUtils;
import org.cambium.common.util.MapUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.CUD;
import org.twins.core.dao.draft.*;
import org.twins.core.dao.twin.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.draft.DraftCollector;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryResultUncommited;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinDelete;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.TwinChangesService;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.factory.TwinFactoryService;
import org.twins.core.service.twin.TwinEraserService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinflow.TwinflowService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class DraftService extends EntitySecureFindServiceImpl<DraftEntity> {
    private final DraftRepository draftRepository;
    private final DraftTwinTagRepository draftTwinTagRepository;
    private final DraftTwinMarkerRepository draftTwinMarkerRepository;
    private final DraftTwinEraseRepository draftTwinEraseRepository;
    private final DraftTwinAttachmentRepository draftTwinAttachmentRepository;
    private final DraftTwinLinkRepository draftTwinLinkRepository;
    private final DraftTwinFieldSimpleRepository draftTwinFieldSimpleRepository;
    private final DraftTwinFieldUserRepository draftTwinFieldUserRepository;
    private final DraftTwinFieldDataListRepository draftTwinFieldDataListRepository;
    private final DraftTwinPersistRepository draftTwinPersistRepository;
    private final EntitySmartService entitySmartService;
    @Lazy
    private final TwinflowService twinflowService;
    @Lazy
    private final AuthService authService;
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final TwinFactoryService twinFactoryService;
    @Lazy
    private final TwinEraserService twinEraserService;
    @Lazy
    private final TwinChangesService twinChangesService;
    @Lazy
    private final AttachmentService attachmentService;

    public DraftCollector beginDraft() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return new DraftCollector(
                new DraftEntity()
                        .setId(UUID.randomUUID())
                        .setCreatedAt(Timestamp.from(Instant.now()))
                        .setCreatedByUser(authService.getApiUser().getUser())
                        .setCreatedByUserId(authService.getApiUser().getUserId())
                        .setStatus(DraftEntity.Status.UNDER_CONSTRUCTION)
                        .setDomainId(apiUser.getDomainId())
                        .setBusinessAccountId(apiUser.getBusinessAccountId())
        );
    }

    public DraftEntity draftErase(TwinEntity twinEntity) throws ServiceException {
        DraftCollector draftCollector = beginDraft();
        twinflowService.loadTwinflow(twinEntity);
        draftErase(draftCollector, twinEntity, twinEntity, DraftTwinEraseEntity.Reason.TARGET, false);
        runFactoryAndDraftResult(draftCollector, twinEntity);
        flush(draftCollector);
        draftCascadeErase(draftCollector);
        endDraft(draftCollector);
        return draftCollector.getDraftEntity();
    }

    public void draftCascadeErase(DraftCollector draftCollector) throws ServiceException {
        /* erase scope is not fully loaded because:
        1. linked twins can also have children and links
        2. child twins can also have links
        scope will be loaded when all items will have eraseReady = true
        */
        List<DraftTwinEraseEntity> eraseNotReadyList = draftTwinEraseRepository.findByDraftIdAndEraseReadyFalse(draftCollector.getDraftId());
        int cascadeDepth = 0;
        while (CollectionUtils.isNotEmpty(eraseNotReadyList)) {
            cascadeDepth++;
            if (cascadeDepth >= 5)
                throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_CASCADE_ERASE_LIMIT);
            twinflowService.loadTwinflow(eraseNotReadyList.stream().map(DraftTwinEraseEntity::getTwin).toList()); //bulk detect
            for (DraftTwinEraseEntity eraseItem : eraseNotReadyList) {
                switch (eraseItem.getReason()) { // switch is more clear here
                    case TARGET:
                    case FACTORY:
                    case LINK:
                        // after running erase factory we can have updated child twins with new heads (not current twin)
                        // so we should exclude them from deletion, this will be done in db query
                        draftTwinEraseRepository.addChildTwins(draftCollector.getDraftId(), eraseItem.getTwinId(), LTreeUtils.matchInTheMiddle(eraseItem.getTwinId()));
                        // after running erase factory we can have some link updates (dst twin was changed from current twin)
                        // so we should exclude them from deletion
                        draftTwinEraseRepository.addLinked(draftCollector.getDraftId(), eraseItem.getTwinId());
                        break;
                    case CHILD:
                        // we do not need to add child of child, because it's already done, so we will add only links
                        draftTwinEraseRepository.addLinked(draftCollector.getDraftId(), eraseItem.getTwinId());
                        break;
                    default:
                        throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "something went wrong");
                }
                runFactoryAndDraftResult(draftCollector, eraseItem.getTwin());
                flush(draftCollector);
                eraseItem
                        .setEraseReady(true)
                        .setEraseTwinStatusId(eraseItem.getTwin().getTwinflow().getEraseTwinStatusId());
                if (eraseItem.getEraseTwinStatusId() == null)
                    draftCollector.getDraftEntity().incrementTwinEraseIrrevocable();
                else
                    draftCollector.getDraftEntity().incrementTwinEraseByStatus();
            }
            draftTwinEraseRepository.saveAll(eraseNotReadyList);
            eraseNotReadyList = draftTwinEraseRepository.findByDraftIdAndEraseReadyFalse(draftCollector.getDraftId());
        }
    }

    public DraftEntity draftFactoryResult(FactoryResultUncommited factoryResultUncommited) throws ServiceException {
        DraftCollector draftCollector = beginDraft();
        draftFactoryResult(draftCollector, factoryResultUncommited, null);
        if (CollectionUtils.isNotEmpty(factoryResultUncommited.getDeletes())) {
            draftCascadeErase(draftCollector);
        }
        endDraft(draftCollector);
        return draftCollector.getDraftEntity();
    }

    public DraftCollector runFactoryAndDraftResult(DraftCollector draftCollector, TwinEntity twinEntity) throws ServiceException {
        UUID eraseFactoryId = twinEntity.getTwinflow().getEraseTwinFactoryId();
        if (eraseFactoryId == null)
            return draftCollector;
        FactoryContext factoryContext = new FactoryContext()
                .addInputTwin(twinEntity);
        runFactoryAndDraftResult(draftCollector, eraseFactoryId, factoryContext, twinEntity);
        return draftCollector;
    }

    public DraftCollector runFactoryAndDraftResult(DraftCollector draftCollector, UUID factoryId, FactoryContext factoryContext, TwinEntity reasonTwin) throws ServiceException {
        if (factoryId == null)
            return draftCollector;
        FactoryResultUncommited factoryResultUncommited = twinFactoryService.runFactory(factoryId, factoryContext);
        draftFactoryResult(draftCollector, factoryResultUncommited, reasonTwin);
        return draftCollector;
    }

    public DraftCollector draftFactoryResult(DraftCollector draftCollector, Collection<FactoryResultUncommited> factoryResultsUncommited) throws ServiceException {
        for (FactoryResultUncommited factoryResult : factoryResultsUncommited) {
            draftFactoryResult(draftCollector, factoryResult, null);
        }
        return draftCollector;
    }

    public DraftCollector draftFactoryResult(DraftCollector draftCollector, FactoryResultUncommited factoryResultUncommited, TwinEntity reasonTwin) throws ServiceException {
        if (!factoryResultUncommited.isCommittable()) // we will anyway create draft to show locked twin
            draftCollector.getDraftEntity().setStatus(DraftEntity.Status.LOCKED);
        if (CollectionUtils.isNotEmpty(factoryResultUncommited.getCreates()))
            for (TwinCreate twinCreate : factoryResultUncommited.getCreates())
                draftTwinCreate(draftCollector, twinCreate);
        if (CollectionUtils.isNotEmpty(factoryResultUncommited.getUpdates()))
            for (TwinUpdate twinUpdate : factoryResultUncommited.getUpdates())
                draftTwinUpdate(draftCollector, twinUpdate);
        if (CollectionUtils.isNotEmpty(factoryResultUncommited.getDeletes()))
            for (TwinDelete twinDelete : factoryResultUncommited.getDeletes())
                draftErase(draftCollector, twinDelete.getTwinEntity(), reasonTwin, DraftTwinEraseEntity.Reason.FACTORY, twinDelete.isCauseGlobalLock());
        return draftCollector;
    }

    public DraftCollector draftErase(DraftCollector draftCollector, TwinEntity twinEntity, TwinEntity reasonTwin, DraftTwinEraseEntity.Reason reason, boolean causeGlobalLock) throws ServiceException {
        if (causeGlobalLock) //adding extra lock
            draftCollector.getDraftEntity().setStatus(DraftEntity.Status.LOCKED);
        return draftCollector.add(createTwinEraseDraft(draftCollector.getDraftEntity(), twinEntity, reasonTwin, reason, causeGlobalLock));
    }

    public DraftCollector draftTwinUpdate(DraftCollector draftCollector, TwinEntity twinEntity) throws ServiceException {
        draftCollector.add(createTwinUpdateDraft(draftCollector.getDraftEntity(), twinEntity));
        return draftCollector;
    }

    public DraftCollector draftTwinUpdate(DraftCollector draftCollector, TwinUpdate twinUpdate) throws ServiceException {
        draftTwinUpdate(draftCollector, twinUpdate.getTwinEntity());
        draftTagsUpdate(draftCollector, twinUpdate.getDbTwinEntity().getId(), twinUpdate.getTagsAddExisted(), twinUpdate.getTagsDelete());
        draftMarkersUpdate(draftCollector, twinUpdate.getDbTwinEntity().getId(), twinUpdate.getMarkersAdd(), twinUpdate.getMarkersDelete());
        if (MapUtils.isNotEmpty(twinUpdate.getFields())) {
            TwinChangesCollector twinChangesCollector = new TwinChangesCollector(false); //we will collect history on commit
            twinService.convertTwinFields(twinUpdate.getDbTwinEntity(), twinUpdate.getFields(), twinChangesCollector);
            draftFieldSimpleUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldSimpleEntity.class));
            draftFieldUserUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldUserEntity.class));
            draftFieldDataListUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldDataListEntity.class));
            draftLinkUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinLinkEntity.class));

            draftFieldUserDelete(draftCollector, twinChangesCollector.getDeletes(TwinFieldUserEntity.class));
            draftFieldDataListDelete(draftCollector, twinChangesCollector.getDeletes(TwinFieldDataListEntity.class));
            draftLinkDelete(draftCollector, twinChangesCollector.getDeletes(TwinLinkEntity.class));
        }
        if (twinUpdate.getTwinLinkCUD() != null) {
            draftLinkUpdate(draftCollector, twinUpdate.getTwinLinkCUD().getUpdateList());
            draftLinkUpdate(draftCollector, twinUpdate.getTwinLinkCUD().getCreateList());
            draftLinkDelete(draftCollector, twinUpdate.getTwinLinkCUD().getDeleteList());
        }
        if (twinUpdate.getAttachmentCUD() != null) {
            draftAttachmentUpdate(draftCollector, twinUpdate.getAttachmentCUD().getUpdateList());
            draftAttachmentUpdate(draftCollector, twinUpdate.getAttachmentCUD().getCreateList());
            draftAttachmentDelete(draftCollector, twinUpdate.getAttachmentCUD().getDeleteList());
        }
        return draftCollector;
    }

    public DraftCollector draftTwinCreate(DraftCollector draftCollector, TwinCreate twinCreate) throws ServiceException {
        draftCollector.add(createTwinCreateDraft(draftCollector.getDraftEntity(), twinCreate.getTwinEntity()));
        draftTagsUpdate(draftCollector, twinCreate.getTwinEntity().getId(), twinCreate.getTagsAddExisted(), null);
        draftMarkersUpdate(draftCollector, twinCreate.getTwinEntity().getId(), twinCreate.getMarkersAdd(), null);
        if (MapUtils.isNotEmpty(twinCreate.getFields())) {
            TwinChangesCollector twinChangesCollector = new TwinChangesCollector(false);
            twinService.convertTwinFields(twinCreate.getTwinEntity(), twinCreate.getFields(), twinChangesCollector);
            draftFieldSimpleUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldSimpleEntity.class));
            draftFieldUserUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldUserEntity.class));
            draftFieldDataListUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldDataListEntity.class));
            draftLinkUpdate(draftCollector, twinChangesCollector.getSaveEntities(TwinLinkEntity.class));
        }
        if (twinCreate.getLinksEntityList() != null) {
            draftLinkUpdate(draftCollector, twinCreate.getLinksEntityList());
        }
        if (twinCreate.getAttachmentEntityList() != null) {
            draftAttachmentUpdate(draftCollector, twinCreate.getAttachmentEntityList());
        }
        return draftCollector;
    }

    public void draftFieldSimpleUpdate(DraftCollector draftCollector, Collection<TwinFieldSimpleEntity> fieldSimpleEntities) throws ServiceException {
        for (TwinFieldSimpleEntity twinFieldSimpleEntity : fieldSimpleEntities)
            draftCollector.add(createFieldDraft(draftCollector.getDraftEntity(), twinFieldSimpleEntity));
    }

    public void draftFieldUserUpdate(DraftCollector draftCollector, Collection<TwinFieldUserEntity> fieldUserEntities) throws ServiceException {
        for (TwinFieldUserEntity twinFieldUserEntity : fieldUserEntities)
            draftCollector.add(createFieldDraft(draftCollector.getDraftEntity(), twinFieldUserEntity));
    }

    public void draftFieldDataListUpdate(DraftCollector draftCollector, Collection<TwinFieldDataListEntity> fieldDataListEntities) throws ServiceException {
        for (TwinFieldDataListEntity twinFieldDataListEntity : fieldDataListEntities)
            draftCollector.add(createFieldDraft(draftCollector.getDraftEntity(), twinFieldDataListEntity));
    }

    public void draftLinkUpdate(DraftCollector draftCollector, Collection<TwinLinkEntity> twinLinkEntities) throws ServiceException {
        for (TwinLinkEntity twinLinkEntity : twinLinkEntities)
            draftCollector.add(createTwinLinkDraft(draftCollector.getDraftEntity(), twinLinkEntity));
    }

    public void draftAttachmentUpdate(DraftCollector draftCollector, Collection<TwinAttachmentEntity> twinAttachmentEntities) throws ServiceException {
        for (TwinAttachmentEntity twinLinkEntity : twinAttachmentEntities)
            draftCollector.add(createTwinAttachmentDraft(draftCollector.getDraftEntity(), twinLinkEntity));
    }

    public void draftFieldUserDelete(DraftCollector draftCollector, Collection<TwinFieldUserEntity> fieldUserDeleteList) throws ServiceException {
        for (TwinFieldUserEntity twinFieldUser : fieldUserDeleteList)
            draftCollector.add(createFieldUserDeleteDraft(draftCollector.getDraftEntity(), twinFieldUser));
    }

    public void draftFieldDataListDelete(DraftCollector draftCollector, Collection<TwinFieldDataListEntity> fieldDataListDeleteList) throws ServiceException {
        for (TwinFieldDataListEntity twinFieldDataList : fieldDataListDeleteList)
            draftCollector.add(createFieldDataListDeleteDraft(draftCollector.getDraftEntity(), twinFieldDataList));
    }

    public void draftLinkDelete(DraftCollector draftCollector, Collection<TwinLinkEntity> twinLinkDeleteList) throws ServiceException {
        for (TwinLinkEntity twinLink : twinLinkDeleteList)
            draftCollector.add(createLinkDeleteDraft(draftCollector.getDraftEntity(), twinLink));
    }

    public void draftAttachmentDelete(DraftCollector draftCollector, Collection<TwinAttachmentEntity> twinAttachmentDeleteList) throws ServiceException {
        for (TwinAttachmentEntity twinAttachment : twinAttachmentDeleteList)
            draftCollector.add(createAttachmentDeleteDraft(draftCollector.getDraftEntity(), twinAttachment));
    }

    public void draftTagsUpdate(DraftCollector draftCollector, UUID twinId, Set<UUID> tagsAddExisted, Set<UUID> tagsDelete) throws ServiceException {
        if (CollectionUtils.isNotEmpty(tagsAddExisted)) {
            for (UUID tagId : tagsAddExisted) {
                draftCollector.add(
                        createTagDraft(draftCollector.getDraftEntity(), twinId, tagId, true));
            }
        }
        if (CollectionUtils.isNotEmpty(tagsDelete)) {
            for (UUID tagId : tagsDelete) {
                draftCollector.add(
                        createTagDraft(draftCollector.getDraftEntity(), twinId, tagId, false));
            }
        }
    }

    public void draftMarkersUpdate(DraftCollector draftCollector, UUID twinId, Set<UUID> markersAdd, Set<UUID> markersDelete) throws ServiceException {
        if (CollectionUtils.isNotEmpty(markersAdd))
            for (UUID markerId : markersAdd)
                draftCollector.add(
                        createMarkerDraft(draftCollector.getDraftEntity(), twinId, markerId, true));

        if (CollectionUtils.isNotEmpty(markersDelete))
            for (UUID markerId : markersDelete)
                draftCollector.add(
                        createMarkerDraft(draftCollector.getDraftEntity(), twinId, markerId, false));
    }

    public DraftTwinPersistEntity createTwinCreateDraft(DraftEntity draftEntity, TwinEntity twinEntity) throws ServiceException {
        return new DraftTwinPersistEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinEntity.getId())
                .setDescription(twinEntity.getDescription())
                .setName(twinEntity.getName())
                .setAssignerUserId(twinEntity.getAssignerUserId())
                .setCreatedByUserId(twinEntity.getCreatedByUserId())
                .setHeadTwinId(twinEntity.getHeadTwinId())
                .setExternalId(twinEntity.getExternalId())
                .setViewPermissionId(twinEntity.getViewPermissionId())
                .setTwinStatusId(twinEntity.getTwinStatusId())
//                .setTwinClassId()
//                .setOwnerUserId()
//                .setOwnerBusinessAccountId()
                .setCreateElseUpdate(true);
    }

    public DraftTwinPersistEntity createTwinUpdateDraft(DraftEntity draftEntity, TwinEntity twinEntity) throws ServiceException {
        return new DraftTwinPersistEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinService.checkEntityReadAllow(twinEntity).getId())
                .setDescription(twinEntity.getDescription())
                .setName(twinEntity.getName())
                .setAssignerUserId(twinEntity.getAssignerUserId())
                .setCreatedByUserId(twinEntity.getCreatedByUserId())
                .setHeadTwinId(twinEntity.getHeadTwinId())
                .setExternalId(twinEntity.getExternalId())
                .setViewPermissionId(twinEntity.getViewPermissionId())
                .setTwinStatusId(twinEntity.getTwinStatusId())
//                .setTwinClassId()
//                .setOwnerUserId()
//                .setOwnerBusinessAccountId()
                .setCreateElseUpdate(false);
    }

    public DraftTwinEraseEntity createTwinEraseDraft(DraftEntity draftEntity, TwinEntity twinEntity, TwinEntity reasonTwin, DraftTwinEraseEntity.Reason reason, boolean causeGlobalLock) throws ServiceException {
        return new DraftTwinEraseEntity()
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinService.checkEntityReadAllow(twinEntity).getId())
                .setTwin(twinEntity)
                .setEraseReady(false)
                .setReasonTwinId(reasonTwin != null ? reasonTwin.getId() : null)
                .setReason(reason)
                .setCauseGlobalLock(causeGlobalLock)
                .setEraseTwinStatusId(null); //we will fill it later
    }

    public DraftTwinTagEntity createTagDraft(DraftEntity draftEntity, UUID twinId, UUID tagId, boolean createElseDelete) throws ServiceException {
        return new DraftTwinTagEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinId)
                .setTagDataListOptionId(tagId)
                .setCreateElseDelete(createElseDelete);
    }

    public DraftTwinMarkerEntity createMarkerDraft(DraftEntity draftEntity, UUID twinId, UUID markerId, boolean createElseDelete) throws ServiceException {
        return new DraftTwinMarkerEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinId)
                .setMarkerDataListOptionId(markerId)
                .setCreateElseDelete(createElseDelete);
    }

    public DraftTwinFieldSimpleEntity createFieldDraft(DraftEntity draftEntity, TwinFieldSimpleEntity twinFieldSimpleEntity) throws ServiceException {
        CUD cud = twinFieldSimpleEntity.getId() == null ? CUD.CREATE : CUD.UPDATE;

        DraftTwinFieldSimpleEntity draftTwinFieldSimpleEntity = new DraftTwinFieldSimpleEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinFieldSimpleEntity.getTwinId()) // can we guarantee that it's not null?
                .setCud(cud);
        switch (cud) {
            case CREATE:
                if (twinFieldSimpleEntity.getTwinClassFieldId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field is required for new field creation");
                draftTwinFieldSimpleEntity
                        .setTwinClassFieldId(twinFieldSimpleEntity.getTwinClassFieldId())
                        .setValue(twinFieldSimpleEntity.getValue());
                break;
            case UPDATE:
                if (twinFieldSimpleEntity.getId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field id required for field update");
                draftTwinFieldSimpleEntity
                        .setTwinFieldSimpleId(twinFieldSimpleEntity.getId())
                        .setValue(twinFieldSimpleEntity.getValue());
                break;
        }
        return draftTwinFieldSimpleEntity;
    }

    public DraftTwinFieldSimpleEntity createFieldDraft(DraftEntity draftEntity, TwinEntity twinEntity, FieldValue fieldValue) throws ServiceException {
        CUD cud = fieldValue.getId() == null ? CUD.CREATE : CUD.UPDATE;

        DraftTwinFieldSimpleEntity draftTwinFieldSimpleEntity = new DraftTwinFieldSimpleEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinEntity.getId())
                .setTwinClassFieldId(fieldValue.getTwinClassFieldId())
                .setCud(cud);
        switch (cud) {
            case CREATE:
                if (fieldValue.getTwinClassFieldId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field is required for new field creation");
                draftTwinFieldSimpleEntity
                        .setTwinClassFieldId(fieldValue.getTwinClassFieldId())
                        .setValue(fieldValue.getValue());
                break;
            case UPDATE:
                if (fieldValue.getId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field id required for field update");
                draftTwinFieldSimpleEntity
                        .setTwinFieldSimpleId(fieldValue.getId())
                        .setValue(fieldValue.getValue());
                break;
        }
        return draftTwinFieldSimpleEntity;
    }

    public DraftTwinFieldUserEntity createFieldDraft(DraftEntity draftEntity, TwinFieldUserEntity twinFieldUserEntity) throws ServiceException {
        CUD cud = twinFieldUserEntity.getId() == null ? CUD.CREATE : CUD.UPDATE;

        DraftTwinFieldUserEntity draftTwinFieldUserEntity = new DraftTwinFieldUserEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinFieldUserEntity.getTwinId())
                .setCud(cud);
        switch (cud) {
            case CREATE:
                if (twinFieldUserEntity.getTwinClassFieldId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field is required for new field creation");
                draftTwinFieldUserEntity
                        .setTwinClassFieldId(twinFieldUserEntity.getTwinClassFieldId())
                        .setUserId(twinFieldUserEntity.getUserId());
                break;
            case UPDATE:
                if (twinFieldUserEntity.getId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_class_field.id required for field update");
                draftTwinFieldUserEntity
                        .setTwinFieldUserId(twinFieldUserEntity.getId())
                        .setUserId(twinFieldUserEntity.getUserId());
                break;
        }
        return draftTwinFieldUserEntity;
    }


    public DraftTwinLinkEntity createTwinLinkDraft(DraftEntity draftEntity, TwinLinkEntity twinLinkEntity) throws ServiceException {
        CUD cud = twinLinkEntity.getId() == null ? CUD.CREATE : CUD.UPDATE;

        DraftTwinLinkEntity draftTwinLinkEntity = new DraftTwinLinkEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setCud(cud);
        switch (cud) {
            case CREATE:
                if (twinLinkEntity.getLinkId() == null || twinLinkEntity.getSrcTwinId() == null || twinLinkEntity.getDstTwinId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "incorrect link");
                draftTwinLinkEntity
                        .setLinkId(twinLinkEntity.getLinkId())
                        .setSrcTwinId(twinLinkEntity.getSrcTwinId())
                        .setDstTwinId(twinLinkEntity.getDstTwinId());
                break;
            case UPDATE:
                if (twinLinkEntity.getId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_link.id required for link update");
                draftTwinLinkEntity
                        .setTwinLinkId(twinLinkEntity.getId())
                        .setLinkId(twinLinkEntity.getLinkId())
                        .setSrcTwinId(twinLinkEntity.getSrcTwinId())
                        .setDstTwinId(twinLinkEntity.getDstTwinId());
                break;
        }
        return draftTwinLinkEntity;
    }

    public DraftTwinAttachmentEntity createTwinAttachmentDraft(DraftEntity draftEntity, TwinAttachmentEntity twinAttachmentEntity) throws ServiceException {
        CUD cud = twinAttachmentEntity.getId() == null ? CUD.CREATE : CUD.UPDATE;

        DraftTwinAttachmentEntity draftTwinAttachmentEntity = new DraftTwinAttachmentEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinAttachmentEntity.getTwinId()) // not sure that we need it
                .setCud(cud)
                .setExternalId(twinAttachmentEntity.getExternalId())
                .setTitle(twinAttachmentEntity.getTitle())
                .setDescription(twinAttachmentEntity.getDescription())
                .setStorageLink(twinAttachmentEntity.getStorageLink())
                .setViewPermissionId(twinAttachmentEntity.getViewPermissionId())
                .setTwinClassFieldId(twinAttachmentEntity.getTwinClassFieldId())
                .setTwinCommentId(twinAttachmentEntity.getTwinCommentId())
                .setTwinflowTransitionId(twinAttachmentEntity.getTwinflowTransitionId());
        if (cud == CUD.UPDATE) {
            if (twinAttachmentEntity.getId() == null)
                throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_attachment.id required for link update");
            draftTwinAttachmentEntity.setTwinAttachmentId(twinAttachmentEntity.getId());
        }
        return draftTwinAttachmentEntity;
    }

    public DraftTwinFieldUserEntity createFieldUserDeleteDraft(DraftEntity draftEntity, TwinFieldUserEntity twinFieldUser) throws ServiceException {
        if (twinFieldUser == null || twinFieldUser.getId() == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_field_user.id required for field deletion");
        return new DraftTwinFieldUserEntity()
                .setCud(CUD.DELETE)
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinFieldUserId(twinFieldUser.getUserId())
                .setUserId(twinFieldUser.getUserId())
                .setTwinId(twinFieldUser.getTwinId())
                .setTwinClassFieldId(twinFieldUser.getTwinClassFieldId());
    }

    public DraftTwinFieldDataListEntity createFieldDataListDeleteDraft(DraftEntity draftEntity, TwinFieldDataListEntity twinFieldDataList) throws ServiceException {
        if (twinFieldDataList == null || twinFieldDataList.getId() == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_field_data_list.id required for field deletion");
        return new DraftTwinFieldDataListEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setCud(CUD.DELETE)
                .setTwinFieldDataListId(twinFieldDataList.getId())
                .setTwinId(twinFieldDataList.getTwinId())
                .setTwinClassFieldId(twinFieldDataList.getTwinClassFieldId());
    }

    public DraftTwinLinkEntity createLinkDeleteDraft(DraftEntity draftEntity, TwinLinkEntity twinLink) throws ServiceException {
        if (twinLink == null || twinLink.getId() == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_link.id required for link  deletion");
        return new DraftTwinLinkEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setCud(CUD.DELETE)
                .setTwinLinkId(twinLink.getId())
                .setSrcTwinId(twinLink.getSrcTwinId())
                .setDstTwinId(twinLink.getDstTwinId());
    }

    public DraftTwinAttachmentEntity createAttachmentDeleteDraft(DraftEntity draftEntity, TwinAttachmentEntity attachmentEntity) throws ServiceException {
        if (attachmentEntity == null || attachmentEntity.getId() == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_attachment.id required for deletion");
        return new DraftTwinAttachmentEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setCud(CUD.DELETE)
                .setTwinAttachmentId(attachmentEntity.getId())
                .setTwinId(attachmentEntity.getTwinId());
    }

    public DraftTwinFieldDataListEntity createFieldDraft(DraftEntity draftEntity, TwinFieldDataListEntity twinFieldDataListEntity) throws ServiceException {
        CUD cud = twinFieldDataListEntity.getId() == null ? CUD.CREATE : CUD.UPDATE;

        DraftTwinFieldDataListEntity draftTwinFieldDataListEntity = new DraftTwinFieldDataListEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinFieldDataListEntity.getTwinId())
                .setCud(cud);
        switch (cud) {
            case CREATE:
                if (twinFieldDataListEntity.getTwinClassFieldId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field is required for new field creation");
                draftTwinFieldDataListEntity
                        .setTwinClassFieldId(twinFieldDataListEntity.getTwinClassFieldId())
                        .setDataListOptionId(twinFieldDataListEntity.getDataListOptionId());
                break;
            case UPDATE:
                if (twinFieldDataListEntity.getId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field id required for field update");
                draftTwinFieldDataListEntity
                        .setTwinFieldDataListId(twinFieldDataListEntity.getId())
                        .setDataListOptionId(twinFieldDataListEntity.getDataListOptionId());
                break;
        }
        return draftTwinFieldDataListEntity;
    }

    public void flush(DraftCollector draftCollector) throws ServiceException {
        if (!draftCollector.isOnceFlushed()) { // if we still do not store draftEntity to DB, let's do it
            entitySmartService.save(draftCollector.getDraftEntity(), draftRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
            draftCollector.setOnceFlushed(true);
        }
        if (!draftCollector.hasChanges())
            return;
        if (!draftCollector.isWritable())
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_NOT_WRITABLE, "current draft is already not writable");
        saveEntities(draftCollector, DraftTwinPersistEntity.class, draftTwinPersistRepository);
        saveEntities(draftCollector, DraftTwinEraseEntity.class, draftTwinEraseRepository);
        saveEntities(draftCollector, DraftTwinAttachmentEntity.class, draftTwinAttachmentRepository);
        saveEntities(draftCollector, DraftTwinLinkEntity.class, draftTwinLinkRepository);
        saveEntities(draftCollector, DraftTwinTagEntity.class, draftTwinTagRepository);
        saveEntities(draftCollector, DraftTwinMarkerEntity.class, draftTwinMarkerRepository);
        saveEntities(draftCollector, DraftTwinFieldSimpleEntity.class, draftTwinFieldSimpleRepository);
        saveEntities(draftCollector, DraftTwinFieldUserEntity.class, draftTwinFieldUserRepository);
        saveEntities(draftCollector, DraftTwinFieldDataListEntity.class, draftTwinFieldDataListRepository);
        if (!draftCollector.getSaveEntityMap().isEmpty())
            for (Map.Entry<Class<?>, Set<Object>> classChanges : draftCollector.getSaveEntityMap().entrySet()) {
                log.warn("Unsupported entity class[{}] for saving", classChanges.getKey().getSimpleName());
            }
    }

    private <T, K> void saveEntities(DraftCollector draftCollector, Class<T> entityClass, CrudRepository<T, K> repository) {
        Set<Object> entities = draftCollector.getSaveEntityMap().get(entityClass);
        if (entities != null) {
            entitySmartService.saveAllAndLogChanges((Map) entities, repository);
            draftCollector.getSaveEntityMap().remove(entityClass);
        }
    }

    public void endDraft(DraftCollector draftCollector) throws ServiceException {
        flush(draftCollector);
        if (draftCollector.getDraftEntity().getStatus() == DraftEntity.Status.UNDER_CONSTRUCTION) // only such status can be changed to UNCOMMITED
            draftCollector.getDraftEntity().setStatus(DraftEntity.Status.UNCOMMITED);
        normalizeDraft(draftCollector);
        //todo update counters
        checkConflicts(draftCollector);
        entitySmartService.save(draftCollector.getDraftEntity(), draftRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
    }

    // let's try to clean some possible garbage (updates ot twins that will be fully deleted)
    private void normalizeDraft(DraftCollector draftCollector) {
        if (draftCollector.getDraftEntity().getTwinEraseIrrevocableCount() == 0)
            return; //nothing need to be normalized

        if (draftCollector.getDraftEntity().getTwinPersistCreateCount() > 0)
            // we can delete all persisted draft twins, if they must be deleted in future
            draftTwinPersistRepository.normalizeDraft(draftCollector.getDraftId());
        if (draftCollector.getDraftEntity().getTwinTagCount() > 0)
            // we can delete all persisted draft twins tags, if twins must be deleted in future
            draftTwinTagRepository.normalizeDraft(draftCollector.getDraftId());
        if (draftCollector.getDraftEntity().getTwinMarkerCount() > 0)
            // we can delete all persisted draft twins markers, if twins must be deleted in future
            draftTwinMarkerRepository.normalizeDraftByTwinDeletion(draftCollector.getDraftId());
        if (draftCollector.getDraftEntity().getTwinAttachmentCount() > 0)
            // we can delete all persisted draft twins attachments, if twins must be deleted in future
            draftTwinAttachmentRepository.normalizeDraft(draftCollector.getDraftId());
        if (draftCollector.getDraftEntity().getTwinFieldSimpleCount() > 0)
            // we can delete all persisted draft twins fields simple, if twins must be deleted in future
            draftTwinFieldSimpleRepository.normalizeDraft(draftCollector.getDraftId());
        if (draftCollector.getDraftEntity().getTwinFieldUserCount() > 0)
            // we can delete all persisted draft twins fields user, if twins must be deleted in future
            draftTwinFieldUserRepository.normalizeDraft(draftCollector.getDraftId());
        if (draftCollector.getDraftEntity().getTwinFieldDataListCount() > 0)
            // we can delete all persisted draft twins fields datalist, if twins must be deleted in future
            draftTwinFieldDataListRepository.normalizeDraft(draftCollector.getDraftId());
        if (draftCollector.getDraftEntity().getTwinLinkDeleteCount() > 0)
            //we can clean only links, which must be deleted, all other should be checked during commit
            draftTwinLinkRepository.normalizeDraft(draftCollector.getDraftId());

        if (draftCollector.getDraftEntity().getTwinMarkerCreateCount() > 0 && draftCollector.getDraftEntity().getTwinMarkerDeleteCount() > 0)
            draftTwinMarkerRepository.normalizeDraftByTwinDeletion(draftCollector.getDraftId());
    }

    private void checkConflicts(DraftCollector draftCollector) {
        if (draftCollector.getDraftEntity().getTwinEraseIrrevocableCount() == 0)
            return; //hope we will have no conflicts in such case
        //todo
    }

    @Override
    public CrudRepository<DraftEntity, UUID> entityRepository() {
        return draftRepository;
    }

    @Override
    public Function<DraftEntity, UUID> entityGetIdFunction() {
        return DraftEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DraftEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!apiUser.getUserId().equals(entity.getCreatedByUserId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed for " + apiUser.getUser().easyLog(EasyLoggable.Level.NORMAL));
            return true;
        }
        if (!apiUser.getDomainId().equals(entity.getDomainId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not accessible in " + apiUser.getDomain().easyLog(EasyLoggable.Level.NORMAL));
            return true;
        }
        if (apiUser.isBusinessAccountSpecified() && !apiUser.getBusinessAccountId().equals(entity.getBusinessAccountId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not accessible in " + apiUser.getBusinessAccount().easyLog(EasyLoggable.Level.NORMAL));
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(DraftEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
