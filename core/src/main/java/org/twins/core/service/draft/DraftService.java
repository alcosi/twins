package org.twins.core.service.draft;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.LTreeUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.CUD;
import org.twins.core.dao.draft.*;
import org.twins.core.dao.history.HistoryType;
import org.twins.core.dao.twin.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.draft.DraftCollector;
import org.twins.core.domain.factory.FactoryBranchId;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryResultUncommited;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinDelete;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.factory.TwinFactoryService;
import org.twins.core.service.history.ChangesRecorder;
import org.twins.core.service.history.HistoryCollectorMultiTwin;
import org.twins.core.service.history.HistoryService;
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
    private final HistoryService historyService;

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
        try {
//            twinflowService.loadTwinflow(twinEntity);
            draftErase(draftCollector, twinEntity, twinEntity, DraftTwinEraseEntity.Reason.TARGET, false, "");
//            runEraseFactoryAndDraftResult(draftCollector, twinEntity);
            flush(draftCollector);
            draftCascadeErase(draftCollector);
            endDraft(draftCollector);
        } catch (ServiceException e) {
            draftCollector.getDraftEntity()
                    .setStatus(DraftEntity.Status.CONSTRUCTION_EXCEPTION)
                    .setStatusDetails(e.log());
            endDraft(draftCollector);
            throw e;
        }
        return draftCollector.getDraftEntity();
    }

    public void draftCascadeErase(DraftCollector draftCollector) throws ServiceException {
        /* erase scope is not fully loaded because:
        1. linked twins can also have children and links
        2. child twins can also have links
        scope will be loaded when all items will have eraseReady = true
        */
        List<DraftTwinEraseEntity> eraseNotReadyList = draftTwinEraseRepository.findByDraftIdAndStatus(draftCollector.getDraftId(), DraftTwinEraseEntity.Status.UNDETECTED);
        int cascadeDepth = 0;
        // we should use separate historyCollector (not from draftCollector), because of batch save delete items. HistoryCollector from draftCollector is flushing more often
        HistoryCollectorMultiTwin historyCollectorDeletes = new HistoryCollectorMultiTwin();
        while (CollectionUtils.isNotEmpty(eraseNotReadyList)) {
            cascadeDepth++;
            if (cascadeDepth >= 5)
                throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_CASCADE_ERASE_LIMIT);
            twinflowService.loadTwinflow(eraseNotReadyList.stream().map(DraftTwinEraseEntity::getTwin).toList()); //bulk detect
            for (DraftTwinEraseEntity eraseItem : eraseNotReadyList) {
//                entityManager.detach(eraseItem);
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
                runEraseFactoryAndDraftResult(draftCollector, eraseItem);

                eraseItem
//                        .setEraseReady(true)
                        .setEraseTwinStatusId(eraseItem.getTwin().getTwinflow().getEraseTwinStatusId())
                        .setEraseTwinStatus(eraseItem.getTwin().getTwinflow().getEraseTwinStatus());
                if (eraseItem.getStatus() == DraftTwinEraseEntity.Status.UNDETECTED) {
                    if (eraseItem.getEraseTwinStatusId() == null) {
                        eraseItem.setStatus(DraftTwinEraseEntity.Status.DETECTED_IRREVOCABLE_ERASE);
                        historyCollectorDeletes.forTwin(eraseItem.getTwin()).add(HistoryType.twinDeleted, null);
                        draftCollector.getDraftEntity().incrementTwinEraseIrrevocable();
                    } else {
                        eraseItem.setStatus(DraftTwinEraseEntity.Status.DETECTED_STATUS_CHANGE_ERASE);
                        historyCollectorDeletes.forTwin(eraseItem.getTwin()).add(historyService.statusChanged(eraseItem.getTwin().getTwinStatus(), eraseItem.getEraseTwinStatus()));
                        draftCollector.getDraftEntity().incrementTwinEraseByStatus();
                    }
                }
                draftTwinEraseRepository.save(eraseItem);
//                draftCollector.add(eraseItem);
                flush(draftCollector); //we will flush here, because factory also can generate some deletes
            }
            historyService.saveHistory(historyCollectorDeletes, draftCollector.getDraftId());
            historyCollectorDeletes.clear();
            eraseNotReadyList = draftTwinEraseRepository.findByDraftIdAndStatus(draftCollector.getDraftId(), DraftTwinEraseEntity.Status.UNDETECTED);
        }
    }

    public DraftEntity draftFactoryResult(FactoryResultUncommited factoryResultUncommited) throws ServiceException {
        DraftCollector draftCollector = beginDraft();
        try {
            draftFactoryResult(draftCollector, factoryResultUncommited, null);
            if (CollectionUtils.isNotEmpty(factoryResultUncommited.getDeletes())) {
                draftCascadeErase(draftCollector);
            }
            endDraft(draftCollector);
        } catch (ServiceException e) {
            draftCollector.getDraftEntity()
                    .setStatus(DraftEntity.Status.CONSTRUCTION_EXCEPTION)
                    .setStatusDetails(e.log());
            endDraft(draftCollector);
            throw e;
        }
        return draftCollector.getDraftEntity();
    }

    public DraftCollector runEraseFactoryAndDraftResult(DraftCollector draftCollector, DraftTwinEraseEntity eraseEntity) throws ServiceException {
        twinflowService.loadTwinflow(eraseEntity.getTwin());
        UUID eraseFactoryId = eraseEntity.getTwin().getTwinflow().getEraseTwinFactoryId();
        if (eraseFactoryId == null)
            return draftCollector;
        FactoryContext factoryContext = new FactoryContext(FactoryBranchId.root(eraseFactoryId))
                .addInputTwin(eraseEntity.getTwin());
        FactoryResultUncommited factoryResultUncommited = twinFactoryService.runFactory(eraseFactoryId, factoryContext);
        //if factory has some configured eraser, we should fish current twin from result, because it can be locked or skipped
        if (factoryResultUncommited.getSkippedDeletes().contains(eraseEntity.getTwinId())) {
            log.warn("{} was marked by eraser as 'skipped'. This provokes draft lock", eraseEntity.getTwin().logShort());
            eraseEntity
                    .setStatus(DraftTwinEraseEntity.Status.DETECTED_LOCK)
                    .setStatusDetails("skipped");
        }
        TwinDelete twinDelete = factoryResultUncommited.getDeletes().get(eraseEntity.getTwinId());
        if (twinDelete != null && twinDelete.isCauseGlobalLock()) {
            log.warn("{} was marked by eraser as 'locked'. This provokes draft lock", eraseEntity.getTwin().logShort());
            eraseEntity
                    .setStatus(DraftTwinEraseEntity.Status.DETECTED_LOCK)
                    .setStatusDetails(twinDelete.getEraseDetails());
        }
        draftFactoryResult(draftCollector, factoryResultUncommited, eraseEntity.getTwin());
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
            for (TwinDelete twinDelete : factoryResultUncommited.getDeletes()) {
                if (twinDelete.getTwinId().equals(reasonTwin.getId())) {
                    log.debug("{} is already drafted for erase", twinDelete.getTwinEntity());
                    continue;
                }
                draftErase(draftCollector, twinDelete.getTwinEntity(), reasonTwin, DraftTwinEraseEntity.Reason.FACTORY, twinDelete.isCauseGlobalLock(), twinDelete.getEraseDetails());
            }
        return draftCollector;
    }

    public DraftCollector draftErase(DraftCollector draftCollector, TwinEntity twinEntity, TwinEntity reasonTwin, DraftTwinEraseEntity.Reason reason, boolean causeGlobalLock, String statusDetails) throws ServiceException {
        if (causeGlobalLock) //adding extra lock
            draftCollector.getDraftEntity()
                    .setStatus(DraftEntity.Status.LOCKED)
                    .setStatusDetails("locked by: " + twinEntity.logNormal());
        return draftCollector.add(createTwinEraseDraft(draftCollector.getDraftEntity(), twinEntity, reasonTwin, reason, causeGlobalLock, statusDetails));
    }

    public DraftCollector draftTwinUpdate(DraftCollector draftCollector, TwinUpdate twinUpdate) throws ServiceException {
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        DraftTwinPersistEntity draftTwinPersistEntity = new DraftTwinPersistEntity().setCreateElseUpdate(false);
        ChangesRecorder<TwinEntity, DraftTwinPersistEntity> changesRecorder = new ChangesRecorder<>(
                twinUpdate.getDbTwinEntity(),
                twinUpdate.getTwinEntity(),
                draftTwinPersistEntity,
                twinChangesCollector.getHistoryCollector(twinUpdate.getDbTwinEntity()));
        twinService.updateTwin(twinUpdate, twinChangesCollector, changesRecorder);
        //todo add recorder to draftCollector
        draftTagsUpdate(draftCollector, twinChangesCollector);
        draftMarkersUpdate(draftCollector, twinChangesCollector);
        draftFieldSimpleUpdate(draftCollector, twinChangesCollector);
        draftFieldUserUpdate(draftCollector, twinChangesCollector);
        draftFieldDataListUpdate(draftCollector, twinChangesCollector);
        draftLinkUpdate(draftCollector, twinChangesCollector);
        draftAttachmentUpdate(draftCollector, twinChangesCollector);
        draftCollector.getHistoryCollector().add(twinChangesCollector.getHistoryCollector());
        return draftCollector;
    }

    public DraftCollector draftTwinCreate(DraftCollector draftCollector, TwinCreate twinCreate) throws ServiceException {
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        twinService.createTwin(twinCreate, twinChangesCollector);
        draftTwinCreate(draftCollector, twinChangesCollector);
        draftTagsUpdate(draftCollector, twinChangesCollector);
        draftMarkersUpdate(draftCollector, twinChangesCollector);
        draftFieldSimpleUpdate(draftCollector, twinChangesCollector);
        draftFieldUserUpdate(draftCollector, twinChangesCollector);
        draftFieldDataListUpdate(draftCollector, twinChangesCollector);
        draftLinkUpdate(draftCollector, twinChangesCollector);
        draftAttachmentUpdate(draftCollector, twinChangesCollector);
        draftCollector.getHistoryCollector().add(twinChangesCollector.getHistoryCollector());
        return draftCollector;
    }

    private void draftAttachmentUpdate(DraftCollector draftCollector, TwinChangesCollector twinChangesCollector) throws ServiceException {
        draftAttachmentSave(draftCollector, twinChangesCollector.getSaveEntities(TwinAttachmentEntity.class));
        draftAttachmentDelete(draftCollector, twinChangesCollector.getDeletes(TwinAttachmentEntity.class));
    }

    private void draftLinkUpdate(DraftCollector draftCollector, TwinChangesCollector twinChangesCollector) throws ServiceException {
        draftLinkSave(draftCollector, twinChangesCollector.getSaveEntities(TwinLinkEntity.class));
        draftLinkDelete(draftCollector, twinChangesCollector.getDeletes(TwinLinkEntity.class));
    }

    private void draftFieldDataListUpdate(DraftCollector draftCollector, TwinChangesCollector twinChangesCollector) throws ServiceException {
        draftFieldDataListSave(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldDataListEntity.class));
        draftFieldDataListDelete(draftCollector, twinChangesCollector.getDeletes(TwinFieldDataListEntity.class));
    }

    private void draftFieldUserUpdate(DraftCollector draftCollector, TwinChangesCollector twinChangesCollector) throws ServiceException {
        draftFieldUserSave(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldUserEntity.class));
        draftFieldUserDelete(draftCollector, twinChangesCollector.getDeletes(TwinFieldUserEntity.class));
    }

    private void draftFieldSimpleUpdate(DraftCollector draftCollector, TwinChangesCollector twinChangesCollector) throws ServiceException {
        draftFieldSimpleSave(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldSimpleEntity.class));
    }

    public void draftTagsUpdate(DraftCollector draftCollector, TwinChangesCollector twinChangesCollector) throws ServiceException {
        draftTagsCreate(draftCollector, twinChangesCollector.getSaveEntities(TwinTagEntity.class));
        draftTagsDelete(draftCollector, twinChangesCollector.getDeletes(TwinTagEntity.class));
    }

    public void draftMarkersUpdate(DraftCollector draftCollector, TwinChangesCollector twinChangesCollector) throws ServiceException {
        draftMarkersCreate(draftCollector, twinChangesCollector.getSaveEntities(TwinMarkerEntity.class));
        draftMarkersDelete(draftCollector, twinChangesCollector.getDeletes(TwinMarkerEntity.class));
    }

    private void draftTwinCreate(DraftCollector draftCollector, TwinChangesCollector twinChangesCollector) throws ServiceException {
        draftTwinCreate(draftCollector, twinChangesCollector.getSaveEntities(TwinEntity.class));
    }

    public void draftTwinCreate(DraftCollector draftCollector, Collection<TwinEntity> twinEntities) throws ServiceException {
        for (TwinEntity twinEntity : twinEntities)
            draftCollector.add(createTwinCreateDraft(draftCollector.getDraftEntity(), twinEntity));
    }

    public void draftFieldSimpleSave(DraftCollector draftCollector, Collection<TwinFieldSimpleEntity> fieldSimpleEntities) throws ServiceException {
        for (TwinFieldSimpleEntity twinFieldSimpleEntity : fieldSimpleEntities)
            draftCollector.add(createFieldDraft(draftCollector.getDraftEntity(), twinFieldSimpleEntity));
    }

    public void draftFieldUserSave(DraftCollector draftCollector, Collection<TwinFieldUserEntity> fieldUserEntities) throws ServiceException {
        for (TwinFieldUserEntity twinFieldUserEntity : fieldUserEntities)
            draftCollector.add(createFieldDraft(draftCollector.getDraftEntity(), twinFieldUserEntity));
    }

    public void draftFieldDataListSave(DraftCollector draftCollector, Collection<TwinFieldDataListEntity> fieldDataListEntities) throws ServiceException {
        for (TwinFieldDataListEntity twinFieldDataListEntity : fieldDataListEntities)
            draftCollector.add(createFieldDraft(draftCollector.getDraftEntity(), twinFieldDataListEntity));
    }

    public void draftLinkSave(DraftCollector draftCollector, Collection<TwinLinkEntity> twinLinkEntities) throws ServiceException {
        for (TwinLinkEntity twinLinkEntity : twinLinkEntities)
            draftCollector.add(createTwinLinkDraft(draftCollector.getDraftEntity(), twinLinkEntity));
    }

    public void draftAttachmentSave(DraftCollector draftCollector, Collection<TwinAttachmentEntity> twinAttachmentEntities) throws ServiceException {
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

    public void draftTagsCreate(DraftCollector draftCollector, Collection<TwinTagEntity> twinTagEntities) throws ServiceException {
        for (TwinTagEntity twinTagEntity : twinTagEntities)
            draftCollector.add(createTagDraft(draftCollector.getDraftEntity(), twinTagEntity, true));
    }

    public void draftTagsDelete(DraftCollector draftCollector, Collection<TwinTagEntity> twinTagEntities) throws ServiceException {
        for (TwinTagEntity twinTagEntity : twinTagEntities)
            draftCollector.add(createTagDraft(draftCollector.getDraftEntity(), twinTagEntity, false));
    }

    public void draftMarkersCreate(DraftCollector draftCollector, Collection<TwinMarkerEntity> twinMarkerEntities) throws ServiceException {
        for (TwinMarkerEntity twinMarkerEntity : twinMarkerEntities)
            draftCollector.add(createMarkerDraft(draftCollector.getDraftEntity(), twinMarkerEntity, true));
    }

    public void draftMarkersDelete(DraftCollector draftCollector, Collection<TwinMarkerEntity> twinMarkerEntities) throws ServiceException {
        for (TwinMarkerEntity twinMarkerEntity : twinMarkerEntities)
            draftCollector.add(createMarkerDraft(draftCollector.getDraftEntity(), twinMarkerEntity, false));
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
                .setTwinClassId(twinEntity.getTwinClassId())
                .setOwnerUserId(twinEntity.getOwnerUserId())
                .setOwnerBusinessAccountId(twinEntity.getOwnerBusinessAccountId())
                .setCreateElseUpdate(true);
    }

    public DraftTwinEraseEntity createTwinEraseDraft(DraftEntity draftEntity, TwinEntity twinEntity, TwinEntity reasonTwin, DraftTwinEraseEntity.Reason reason, boolean causeGlobalLock, String statusDetails) throws ServiceException {
        return new DraftTwinEraseEntity()
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinService.checkEntityReadAllow(twinEntity).getId())
                .setTwin(twinEntity)
                .setStatus(causeGlobalLock ? DraftTwinEraseEntity.Status.DETECTED_LOCK : DraftTwinEraseEntity.Status.UNDETECTED)
                .setStatusDetails(statusDetails)
                .setReasonTwinId(reasonTwin != null ? reasonTwin.getId() : null)
                .setReason(reason)
//                .setCauseGlobalLock(causeGlobalLock)
                .setEraseTwinStatusId(null); //we will fill it later
    }

    public DraftTwinTagEntity createTagDraft(DraftEntity draftEntity, TwinTagEntity twinTagEntity, boolean createElseDelete) throws ServiceException {
        return new DraftTwinTagEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinTagEntity.getTwinId())
                .setTagDataListOptionId(twinTagEntity.getTagDataListOptionId())
                .setCreateElseDelete(createElseDelete);
    }

    public DraftTwinMarkerEntity createMarkerDraft(DraftEntity draftEntity, TwinMarkerEntity twinMarkerEntity, boolean createElseDelete) throws ServiceException {
        return new DraftTwinMarkerEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinMarkerEntity.getTwinId())
                .setMarkerDataListOptionId(twinMarkerEntity.getMarkerDataListOptionId())
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
                .setTwinId(twinAttachmentEntity.getTwinId())
                .setCud(cud)
                .setExternalId(twinAttachmentEntity.getExternalId())
                .setTitle(twinAttachmentEntity.getTitle())
                .setDescription(twinAttachmentEntity.getDescription())
                .setStorageLink(twinAttachmentEntity.getStorageLink())
                .setViewPermissionId(twinAttachmentEntity.getViewPermissionId())
                .setTwinClassFieldId(twinAttachmentEntity.getTwinClassFieldId()) // not sure that we should allow this on update
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

        if (!draftCollector.getDraftEntitiesMap().isEmpty())
            for (Map.Entry<Class<?>, Set<Object>> classChanges : draftCollector.getDraftEntitiesMap().entrySet()) {
                log.warn("Unsupported entity class[{}] for saving", classChanges.getKey().getSimpleName());
            }
        historyService.saveHistory(draftCollector.getHistoryCollector(), draftCollector.getDraftId());
        draftCollector.clear();
    }

    private <T, K> void saveEntities(DraftCollector draftCollector, Class<T> entityClass, CrudRepository<T, K> repository) {
        Set<T> entities = (Set<T>) draftCollector.getDraftEntitiesMap().get(entityClass);
        if (entities != null) {
            entitySmartService.saveAllAndLog(entities, repository);
            draftCollector.getDraftEntitiesMap().remove(entityClass);
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
