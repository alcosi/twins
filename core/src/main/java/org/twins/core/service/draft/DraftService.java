package org.twins.core.service.draft;

import com.github.f4b6a3.uuid.UuidCreator;
import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.CUD;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.attachment.TwinAttachmentModificationEntity;
import org.twins.core.dao.draft.*;
import org.twins.core.dao.eraseflow.EraseflowEntity;
import org.twins.core.dao.eraseflow.EraseflowLinkCascadeEntity;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.*;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.DetachedTwinChangesCollector;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.draft.DraftCollector;
import org.twins.core.domain.factory.EraseAction;
import org.twins.core.domain.factory.FactoryBranchId;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryResultUncommited;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinDelete;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.draft.DraftStatus;
import org.twins.core.enums.draft.DraftTwinEraseReason;
import org.twins.core.enums.draft.DraftTwinEraseStatus;
import org.twins.core.enums.factory.FactoryEraserAction;
import org.twins.core.enums.factory.FactoryLauncher;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.eraseflow.EraseflowService;
import org.twins.core.service.factory.TwinFactoryService;
import org.twins.core.service.history.ChangesRecorder;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.twin.TwinService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;


@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Lazy
@RequiredArgsConstructor
public class DraftService extends EntitySecureFindServiceImpl<DraftEntity> {
    private final DraftRepository draftRepository;
    private final DraftHistoryRepository draftHistoryRepository;
    private final DraftTwinTagRepository draftTwinTagRepository;
    private final DraftTwinMarkerRepository draftTwinMarkerRepository;
    private final DraftTwinEraseRepository draftTwinEraseRepository;
    private final DraftTwinAttachmentRepository draftTwinAttachmentRepository;
    private final DraftTwinLinkRepository draftTwinLinkRepository;
    private final DraftTwinFieldSimpleRepository draftTwinFieldSimpleRepository;
    private final DraftTwinFieldSimpleNonIndexedRepository draftTwinFieldSimpleNonIndexedRepository;
    private final DraftTwinFieldBooleanRepository draftTwinFieldBooleanRepository;
    private final DraftTwinFieldUserRepository draftTwinFieldUserRepository;
    private final DraftTwinFieldTwinClassRepository draftTwinFieldTwinClassRepository;
    private final DraftTwinFieldDataListRepository draftTwinFieldDataListRepository;
    private final DraftTwinPersistRepository draftTwinPersistRepository;
    private final EntitySmartService entitySmartService;
    private final EntityManager entityManager;
    @Lazy
    private final EraseflowService eraseflowService;
    @Lazy
    private final AuthService authService;
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final TwinFactoryService twinFactoryService;
    @Lazy
    private final HistoryService historyService;
    @Lazy
    private final DraftNormalizeService draftNormalizeService;
    @Lazy
    private final DraftCheckConflictsService draftCheckConflictsService;

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

    public DraftCollector beginDraft() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return new DraftCollector(
                new DraftEntity()
                        .setId(UuidCreator.getTimeOrderedEpoch())
                        .setCreatedAt(Timestamp.from(Instant.now()))
                        .setCreatedByUser(authService.getApiUser().getUser())
                        .setCreatedByUserId(authService.getApiUser().getUserId())
                        .setStatus(DraftStatus.UNDER_CONSTRUCTION)
                        .setDomainId(apiUser.getDomainId())
                        .setBusinessAccountId(apiUser.getBusinessAccountId())
        );
    }

    public DraftEntity draftErase(TwinEntity... twinEntityList) throws ServiceException {
        DraftCollector draftCollector = beginDraft();
        try {
            for (TwinEntity twinEntity : twinEntityList) {
                draftErase(draftCollector, twinEntity, twinEntity, DraftTwinEraseReason.TARGET, new EraseAction(FactoryEraserAction.ERASE_CANDIDATE, ""));
            }
            flush(draftCollector);
            createEraseScope(draftCollector);
            endDraft(draftCollector);
        } catch (ServiceException e) {
            draftCollector.getDraftEntity()
                    .setStatus(DraftStatus.CONSTRUCTION_EXCEPTION)
                    .setStatusDetails(e.log());
            endDraft(draftCollector);
            throw e;
        }
        return draftCollector.getDraftEntity();
    }

    public void createEraseScopeInQueue(DraftCollector draftCollector) throws ServiceException {

    }

    public void createEraseScope(DraftCollector draftCollector) throws ServiceException {
        switch (draftCollector.getDraftEntity().getStatus()) {
            case UNDER_CONSTRUCTION:
                draftCollector.getDraftEntity().setStatus(DraftStatus.ERASE_SCOPE_COLLECT_PLANNED);
                return; // we will run it in other thread
            case ERASE_SCOPE_COLLECT_IN_PROGRESS:
                break; // we will go next
            default:
                return;
        }
        /* erase scope is not fully loaded because:
        1. linked twins can also have children and links
        2. child twins can also have links
        scope will be loaded when all items will be in final states
        */
        List<DraftTwinEraseEntity> eraseNotReadyList = draftTwinEraseRepository.findByDraftIdAndStatusIn(
                draftCollector.getDraftId(),
                DraftTwinEraseStatus.UNDETECTED,
                DraftTwinEraseStatus.IRREVOCABLE_ERASE_DETECTED);
        int cascadeDepth = 0;
        while (CollectionUtils.isNotEmpty(eraseNotReadyList)) {
            cascadeDepth++;
            if (cascadeDepth >= 5)
                throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_CASCADE_ERASE_LIMIT);
            eraseflowService.loadEraseflow(eraseNotReadyList.stream().map(DraftTwinEraseEntity::getTwin).toList()); //bulk detect
            for (DraftTwinEraseEntity eraseItem : eraseNotReadyList) {
//                entityManager.detach(eraseItem);
                switch (eraseItem.getStatus()) {
                    case UNDETECTED:
                        processUndetectedDeletion(draftCollector, eraseItem);
                        if (eraseItem.getStatus() == DraftTwinEraseStatus.UNDETECTED) // extra check, because such statuses are incorrect on current stage
                            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "incorrect status");
                        break;
                    case IRREVOCABLE_ERASE_DETECTED:
                        processCascadeDeletion(draftCollector, eraseItem);
                        if (eraseItem.getStatus() == DraftTwinEraseStatus.UNDETECTED
                                || eraseItem.getStatus() == DraftTwinEraseStatus.IRREVOCABLE_ERASE_DETECTED
                                || eraseItem.getStatus() == DraftTwinEraseStatus.SKIP_DETECTED
                                || eraseItem.getStatus() == DraftTwinEraseStatus.STATUS_CHANGE_ERASE_DETECTED) // extra check, because such statuses are incorrect on current stage
                            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "incorrect status");
                        break;
                    default:
                        log.error("Unreachable status. If you see this - something went wrong, please contact the developer.");
                        continue;
                }
                draftTwinEraseRepository.save(eraseItem);
//                draftCollector.add(eraseItem);
                flush(draftCollector); //we will flush here, because factory also can generate some deletes
            }
            eraseNotReadyList = draftTwinEraseRepository.findByDraftIdAndStatusIn(draftCollector.getDraftId(), DraftTwinEraseStatus.UNDETECTED);
        }
    }

    private void processUndetectedDeletion(DraftCollector draftCollector, DraftTwinEraseEntity eraseEntity) throws ServiceException {
        if (eraseEntity.getStatus() != DraftTwinEraseStatus.UNDETECTED)
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "Incorrect method call. Target deletion can not be started from status " + eraseEntity.getStatus());
        eraseflowService.loadEraseflow(eraseEntity.getTwin());
        UUID eraseFactoryId = detectCascadeDeletionFactory(eraseEntity);
        if (eraseFactoryId == null) { // no factory configured, so let's do irrevocable erase
            eraseEntity.setStatus(DraftTwinEraseStatus.IRREVOCABLE_ERASE_DETECTED);
            processCascadeDeletion(draftCollector, eraseEntity);
            return;
        }

        FactoryLauncher factoryLauncher = switch (eraseEntity.getReason()) {
            case CHILD, LINK -> FactoryLauncher.cascadeDeletion;
            case FACTORY, TARGET -> FactoryLauncher.targetDeletion;
            default ->
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "unknown erase reason " + eraseEntity.getReason());
        };

        FactoryContext factoryContext = new FactoryContext(factoryLauncher, FactoryBranchId.root(eraseFactoryId))
                .addInputTwin(eraseEntity.getTwin());
        FactoryResultUncommited factoryResultUncommited = twinFactoryService.runFactoryAndCollectResult(eraseFactoryId, factoryContext);
        TwinDelete twinDelete;
        switch (factoryLauncher) {
            case targetDeletion:
                // factory input twin (for target deletion) can be in any state:
                // 1. it can be not changed (but some other linked twins are changed)
                // 2. it can be changed (DELETE_BY_STATUS or with some fields change)
                // 3. it can be marked by eraser as RESTRICTED
                // 4. it can be marked by eraser as ERASE_IRREVOCABLE
                // 5. it CAN NOT be marked by eraser as ERASE_CANDIDATE or NOT_SPECIFIED
                twinDelete = factoryResultUncommited.getDeletes().get(eraseEntity.getTwinId());
                if (twinDelete != null) { //p.3, p.4, p.5
                    if (twinDelete.getEraseAction().isCauseGlobalLock()) { //p.3
                        lock(draftCollector, eraseEntity, twinDelete);
                    } else if (twinDelete.getEraseAction().getAction() == FactoryEraserAction.ERASE_CANDIDATE
                            || twinDelete.getEraseAction().getAction() == FactoryEraserAction.NOT_SPECIFIED) { //p.5
                        throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "Unreachable action for current factory launcher");
                    } else if (twinDelete.getEraseAction().getAction() == FactoryEraserAction.ERASE_IRREVOCABLE) { //p.4
                        log.warn("{} was marked by eraser as 'ERASE_IRREVOCABLE'", eraseEntity.getTwin().logShort());
                        eraseEntity
                                .setStatus(DraftTwinEraseStatus.IRREVOCABLE_ERASE_DETECTED)
                                .setStatusDetails(twinDelete.getEraseAction().getDetails());
                    }
                } else { //p.1, p.2
                    TwinUpdate twinUpdate = factoryResultUncommited.getUpdates().get(eraseEntity.getTwinId());
                    if (twinUpdate != null
                            && twinUpdate.getTwinEntity().getTwinStatusId() != null
                            && !twinUpdate.getTwinEntity().getTwinStatusId().equals(twinUpdate.getDbTwinEntity().getTwinStatusId())) { // p.2
                        eraseEntity
                                .setStatus(DraftTwinEraseStatus.STATUS_CHANGE_ERASE_DETECTED)
                                .setStatusDetails("Twin status was changed by factory[" + eraseFactoryId + "]");
                        //todo check that such twin be stored in draft_persist table
                    } else { // p.1
                        eraseEntity
                                .setStatus(DraftTwinEraseStatus.SKIP_DETECTED)
                                .setStatusDetails("Twin erase was skipped factory[" + eraseFactoryId + "]");
                    }
                }
                draftFactoryResult(draftCollector, factoryResultUncommited, eraseEntity.getTwin());
                break;
            case cascadeDeletion:
                // factory input twin (for cascade deletion) can be in 3 states:
                // 1. it can be marked by eraser as RESTRICTED, so we had to lock current draft
                // 2. it can be marked by eraser as DETECTED_IRREVOCABLE_ERASE
                // 3. it can be extracted from cascade deletion (CASCADE_DELETION_EXTRACTION)
                // 4. it can be missed by eraser, so we will treat that as DETECTED_IRREVOCABLE_ERASE
                // 5. it CAN NOT be marked by eraser as ERASE_CANDIDATE or NOT_SPECIFIED
                twinDelete = factoryResultUncommited.getDeletes().get(eraseEntity.getTwinId());
                if (twinDelete != null) { //p.1, p.2, p.5
                    if (twinDelete.getEraseAction().isCauseGlobalLock()) { //p.1
                        lock(draftCollector, eraseEntity, twinDelete);
                    } else if (twinDelete.getEraseAction().getAction() == FactoryEraserAction.ERASE_CANDIDATE
                            || twinDelete.getEraseAction().getAction() == FactoryEraserAction.NOT_SPECIFIED) { //p.5
                        throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "Unreachable action for current factory launcher");
                    } else { //p.2
                        draftFactoryResult(draftCollector, factoryResultUncommited, eraseEntity.getTwin());
                        processCascadeDeletion(draftCollector, eraseEntity);
                    }
                } else if (detectCascadeBreak(eraseEntity, factoryResultUncommited)) { // p.3
                    // currently we can only pause cascade, because we can not be absolutely sure,
                    // that new related twin won't be deleted by some future factory in current draft.
                    // We need to recheck this at the end of draft creation
                    eraseEntity.setStatus(DraftTwinEraseStatus.CASCADE_DELETION_PAUSE);
                    draftFactoryResult(draftCollector, factoryResultUncommited, eraseEntity.getTwin());
                } else { // p.4
                    eraseEntity.setStatus(DraftTwinEraseStatus.IRREVOCABLE_ERASE_DETECTED);
                    draftFactoryResult(draftCollector, factoryResultUncommited, eraseEntity.getTwin());
                    processCascadeDeletion(draftCollector, eraseEntity);
                }
        }
    }

    private static void lock(DraftCollector draftCollector, DraftTwinEraseEntity eraseEntity, TwinDelete twinDelete) {
        log.warn("{} was marked by eraser as 'locked'. This provokes draft lock", eraseEntity.getTwin().logShort());
        eraseEntity
                .setStatus(DraftTwinEraseStatus.LOCK_DETECTED)
                .setStatusDetails(twinDelete.getEraseAction().getDetails());
        lockDraft(draftCollector, eraseEntity.getTwin());
    }

    private static void lockDraft(DraftCollector draftCollector, TwinEntity twinEntity) {
        draftCollector.getDraftEntity()
                .setStatus(DraftStatus.LOCKED)
                .setStatusDetails("locked by: " + twinEntity.logNormal());
    }

    private boolean detectCascadeBreak(DraftTwinEraseEntity eraseEntity, FactoryResultUncommited factoryResultUncommited) {
        TwinUpdate twinUpdate = factoryResultUncommited.getUpdates().get(eraseEntity.getTwinId());
        if (twinUpdate == null)
            return false;
        switch (eraseEntity.getReason()) {
            case CHILD:
                if (twinUpdate.getDbTwinEntity().getHeadTwinId().equals(twinUpdate.getTwinEntity().getHeadTwinId()))  // twin head was not changed
                    return false;
                return checkNewRelatedTwin(eraseEntity, twinUpdate.getTwinEntity().getHeadTwinId());
            case LINK:
                if (twinUpdate.getTwinLinkCUD() == null
                        || CollectionUtils.isEmpty(twinUpdate.getTwinLinkCUD().getUpdateList()))
                    return false;
                for (TwinLinkEntity twinLinkEntity : twinUpdate.getTwinLinkCUD().getUpdateList()) {
                    // hope that nobody will change linkId in factory... otherwise we will have problems here
                    // also this logic currently is not suitable for Many2Many links
                    if (!twinLinkEntity.getLinkId().equals(eraseEntity.getReasonLinkId()))
                        continue;
                    if (!twinLinkEntity.getSrcTwinId().equals(eraseEntity.getTwinId())) {
                        // srcTwin was changed by factory, so we can pause break cascade
                        log.info("Cascade deletion for {} was paused, because link srcTwin was changed from [{}] to [{}]", eraseEntity.logNormal(), twinUpdate.getDbTwinEntity().getHeadTwinId(), twinUpdate.getTwinEntity().getHeadTwinId());
                        return true;
                    } else if (!twinLinkEntity.getDstTwinId().equals(eraseEntity.getReasonTwinId())) {
                        // dstTwin was changed by factory, so we perhaps can break cascade
                        return checkNewRelatedTwin(eraseEntity, twinLinkEntity.getDstTwinId());
                    } else
                        return false;
                }
                return false;
            default:
                return false;
        }
    }

    private boolean checkNewRelatedTwin(DraftTwinEraseEntity eraseEntity, UUID newRelatedTwin) {
        //let's check that new twin is safe (it's should not be deleted)
        DraftTwinEraseEntity newHeadEraseEntity = draftTwinEraseRepository.findByDraftIdAndTwinId(eraseEntity.getDraftId(), newRelatedTwin);
        if (newHeadEraseEntity != null && newHeadEraseEntity.getStatus() == DraftTwinEraseStatus.IRREVOCABLE_ERASE_HANDLED) {
            log.info("{} was not extracted from deletion, because related twin [{}] should be also deleted", eraseEntity.logDetailed(), newRelatedTwin);
            return false;

        } else {
            log.info("Cascade deletion for {} was paused, because twin was relinked to [{}].", eraseEntity.logDetailed(), newRelatedTwin);
            eraseEntity.setCascadeBreakTwinId(newRelatedTwin);
            return true;
        }
    }

    private void processCascadeDeletion(DraftCollector draftCollector, DraftTwinEraseEntity eraseEntity) throws ServiceException {
        if (eraseEntity.getStatus() != DraftTwinEraseStatus.IRREVOCABLE_ERASE_DETECTED)
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "Incorrect method call. Cascade deletion can not be started from status " + eraseEntity.getStatus());
        // after running erase factory we can have updated child twins with new heads (not current twin)
        // so we should exclude them from deletion, this will be done in db query
        draftTwinEraseRepository.addDirectChildTwins(draftCollector.getDraftId(), eraseEntity.getTwinId());
        // after running erase factory we can have some link updates (dst twin was changed from current twin)
        // so we should exclude them from deletion
        draftTwinEraseRepository.addLinked(draftCollector.getDraftId(), eraseEntity.getTwinId());
        // no factory should be run for current item, because has  DETECTED_IRREVOCABLE_ERASE status
        eraseEntity.setStatus(DraftTwinEraseStatus.IRREVOCABLE_ERASE_HANDLED);
        // draftCollector.getHistoryCollector().forTwin(eraseEntity.getTwin()).add(HistoryType.twinDeleted, null); //todo link delete history to BA main twin, because of FK
    }

    public UUID detectCascadeDeletionFactory(DraftTwinEraseEntity eraseEntity) throws ServiceException {
        eraseflowService.loadEraseflow(eraseEntity.getTwin());
        EraseflowEntity eraseflow = eraseEntity.getTwin().getTwinflow().getEraseflow();
        if (eraseflow == null)
            return null;
        switch (eraseEntity.getReason()) {
            case CHILD:
                return eraseflow.getCascadeDeletionByHeadFactoryId();
            case LINK:
                if (eraseEntity.getReasonLinkId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, eraseEntity.logShort() + ": empty reason_link_id");
                eraseflowService.loadEraseflowLinkCascade(eraseflow);
                EraseflowLinkCascadeEntity eraseflowLinkCascadeEntity = eraseflow.getCascadeLinkKit().get(eraseEntity.getReasonLinkId());
                return eraseflowLinkCascadeEntity != null ? eraseflowLinkCascadeEntity.getCascadeDeletionFactoryId() : eraseflow.getCascadeDeletionByLinkDefaultFactoryId();
            case FACTORY:
            case TARGET:
                return eraseflow.getTargetDeletionFactoryId();
            default:
                throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "unknown erase reason " + eraseEntity.getReason());
        }
    }

    public DraftEntity draftFactoryResult(FactoryResultUncommited factoryResultUncommited) throws ServiceException {
        DraftCollector draftCollector = beginDraft();
        try {
            draftFactoryResult(draftCollector, List.of(factoryResultUncommited));
            endDraft(draftCollector);
        } catch (ServiceException e) {
            draftCollector.getDraftEntity()
                    .setStatus(DraftStatus.CONSTRUCTION_EXCEPTION)
                    .setStatusDetails(e.log());
            endDraft(draftCollector);
            throw e;
        }
        return draftCollector.getDraftEntity();
    }


    public DraftCollector draftFactoryResult(DraftCollector draftCollector, Collection<FactoryResultUncommited> factoryResultsUncommited) throws ServiceException {
        for (FactoryResultUncommited factoryResult : factoryResultsUncommited) {
            draftFactoryResult(draftCollector, factoryResult, null);
            flush(draftCollector);
            if (CollectionUtils.isNotEmpty(factoryResult.getDeletes())) {
                createEraseScope(draftCollector);
            }
        }
        return draftCollector;
    }

    private DraftCollector draftFactoryResult(DraftCollector draftCollector, FactoryResultUncommited factoryResultUncommited, TwinEntity reasonTwin) throws ServiceException {
        if (!factoryResultUncommited.isCommittable()) // we will anyway create draft to show locked twin
            draftCollector.getDraftEntity().setStatus(DraftStatus.LOCKED);
        if (CollectionUtils.isNotEmpty(factoryResultUncommited.getCreates()))
            for (TwinCreate twinCreate : factoryResultUncommited.getCreates())
                draftTwinCreate(draftCollector, twinCreate);
        if (CollectionUtils.isNotEmpty(factoryResultUncommited.getUpdates()))
            for (TwinUpdate twinUpdate : factoryResultUncommited.getUpdates())
                draftTwinUpdate(draftCollector, twinUpdate);
        if (CollectionUtils.isNotEmpty(factoryResultUncommited.getDeletes()))
            for (TwinDelete twinDelete : factoryResultUncommited.getDeletes()) {
                if (reasonTwin != null && twinDelete.getTwinId().equals(reasonTwin.getId())) {
                    // 1. if some twin was selected for erase (in UNDETECTED state)
                    // 2. it will be added to draft_twin_erase table
                    // 3. then we run TARGET_DELETION_FACTORY for it
                    // 4. this factory has configured eraser which erase current twin
                    // 5. we should not add one more draft_twin_erase row, because it was already added on p.2
                    log.debug("{} is already drafted for erase", twinDelete.getTwinEntity());
                    continue;
                }
                draftErase(draftCollector, twinDelete.getTwinEntity(), reasonTwin, DraftTwinEraseReason.FACTORY, twinDelete.getEraseAction());
            }
        return draftCollector;
    }

    public DraftCollector draftErase(DraftCollector draftCollector, TwinEntity twinEntity, TwinEntity reasonTwin, DraftTwinEraseReason reason, EraseAction eraseAction) throws ServiceException {
        if (eraseAction.isCauseGlobalLock()) //adding extra lock
            lockDraft(draftCollector, twinEntity);
        else if (eraseAction.getAction() == FactoryEraserAction.NOT_SPECIFIED)
            return draftCollector;
        return draftCollector.add(createTwinEraseDraft(draftCollector.getDraftEntity(), twinEntity, reasonTwin, reason, eraseAction));
    }

    public DraftCollector draftTwinUpdate(DraftCollector draftCollector, TwinUpdate twinUpdate) throws ServiceException {
        DetachedTwinChangesCollector twinChangesCollector = new DetachedTwinChangesCollector(entityManager);
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
        draftFieldSimpleNonIndexedUpdate(draftCollector, twinChangesCollector);
        draftFieldBooleanUpdate(draftCollector, twinChangesCollector);
        draftFieldUserUpdate(draftCollector, twinChangesCollector);
        draftFieldTwinClassUpdate(draftCollector, twinChangesCollector);
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
        draftFieldSimpleNonIndexedUpdate(draftCollector, twinChangesCollector);
        draftFieldBooleanUpdate(draftCollector, twinChangesCollector);
        draftFieldUserUpdate(draftCollector, twinChangesCollector);
        draftFieldTwinClassUpdate(draftCollector, twinChangesCollector);
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

    private void draftFieldTwinClassUpdate(DraftCollector draftCollector, TwinChangesCollector twinChangesCollector) throws ServiceException {
        draftFieldTwinClassSave(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldTwinClassEntity.class));
        draftFieldTwinClassDelete(draftCollector, twinChangesCollector.getDeletes(TwinFieldTwinClassEntity.class));
    }

    private void draftFieldSimpleUpdate(DraftCollector draftCollector, TwinChangesCollector twinChangesCollector) throws ServiceException {
        draftFieldSimpleSave(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldSimpleEntity.class));
    }

    private void draftFieldSimpleNonIndexedUpdate(DraftCollector draftCollector, TwinChangesCollector twinChangesCollector) throws ServiceException {
        draftFieldSimpleNonIndexedSave(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldSimpleNonIndexedEntity.class));
    }

    private void draftFieldBooleanUpdate(DraftCollector draftCollector, TwinChangesCollector twinChangesCollector) throws ServiceException {
        draftFieldBooleanSave(draftCollector, twinChangesCollector.getSaveEntities(TwinFieldBooleanEntity.class));
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

    public void draftFieldSimpleNonIndexedSave(DraftCollector draftCollector, Collection<TwinFieldSimpleNonIndexedEntity> fieldSimpleNonIndexedEntities) throws ServiceException {
        for (TwinFieldSimpleNonIndexedEntity twinFieldSimpleNonIndexedEntity : fieldSimpleNonIndexedEntities)
            draftCollector.add(createFieldDraft(draftCollector.getDraftEntity(), twinFieldSimpleNonIndexedEntity));
    }

    public void draftFieldBooleanSave(DraftCollector draftCollector, Collection<TwinFieldBooleanEntity> fieldBooleanEntities) throws ServiceException {
        for (TwinFieldBooleanEntity twinFieldBooleanEntity : fieldBooleanEntities)
            draftCollector.add(createFieldDraft(draftCollector.getDraftEntity(), twinFieldBooleanEntity));
    }

    public void draftFieldUserSave(DraftCollector draftCollector, Collection<TwinFieldUserEntity> fieldUserEntities) throws ServiceException {
        for (TwinFieldUserEntity twinFieldUserEntity : fieldUserEntities)
            draftCollector.add(createFieldDraft(draftCollector.getDraftEntity(), twinFieldUserEntity));
    }

    public void draftFieldTwinClassSave(DraftCollector draftCollector, Collection<TwinFieldTwinClassEntity> fieldUserEntities) throws ServiceException {
        for (TwinFieldTwinClassEntity twinFieldUserEntity : fieldUserEntities)
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

    public void draftFieldTwinClassDelete(DraftCollector draftCollector, Collection<TwinFieldTwinClassEntity> fieldUserDeleteList) throws ServiceException {
        for (TwinFieldTwinClassEntity twinFieldUser : fieldUserDeleteList)
            draftCollector.add(createFieldTwinClassDeleteDraft(draftCollector.getDraftEntity(), twinFieldUser));
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

    public DraftTwinEraseEntity createTwinEraseDraft(DraftEntity draftEntity, TwinEntity twinEntity, TwinEntity reasonTwin, DraftTwinEraseReason reason, EraseAction eraseAction) throws ServiceException {
        DraftTwinEraseStatus status = null;
        switch (eraseAction.getAction()) {
            case ERASE_CANDIDATE -> status = DraftTwinEraseStatus.UNDETECTED;
            case ERASE_IRREVOCABLE -> status = DraftTwinEraseStatus.IRREVOCABLE_ERASE_DETECTED;
            case RESTRICT -> status = DraftTwinEraseStatus.LOCK_DETECTED;
            case NOT_SPECIFIED -> log.warn("This is unreachable log message! If you see this something is going wrong");
        }
        return new DraftTwinEraseEntity()
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinService.checkEntityReadAllow(twinEntity).getId())
                .setTwin(twinEntity)
                .setStatus(status)
                .setStatusDetails(eraseAction.getDetails())
                .setReasonTwinId(reasonTwin != null ? reasonTwin.getId() : null)
                .setReason(reason);
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
                        .setTwinClassFieldId(twinFieldSimpleEntity.getTwinClassFieldId())
                        .setTwinFieldSimpleId(twinFieldSimpleEntity.getId())
                        .setValue(twinFieldSimpleEntity.getValue());
                break;
        }
        return draftTwinFieldSimpleEntity;
    }

    public DraftTwinFieldSimpleNonIndexedEntity createFieldDraft(DraftEntity draftEntity, TwinFieldSimpleNonIndexedEntity twinFieldSimpleNonIndexedEntity) throws ServiceException {
        CUD cud = twinFieldSimpleNonIndexedEntity.getId() == null ? CUD.CREATE : CUD.UPDATE;

        DraftTwinFieldSimpleNonIndexedEntity draftTwinFieldSimpleNonIndexedEntity = new DraftTwinFieldSimpleNonIndexedEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinFieldSimpleNonIndexedEntity.getTwinId())
                .setCud(cud);

        switch (cud) {
            case CREATE -> {
                if (twinFieldSimpleNonIndexedEntity.getTwinClassFieldId() == null) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field is required for new field creation");
                }

                draftTwinFieldSimpleNonIndexedEntity
                        .setTwinClassFieldId(twinFieldSimpleNonIndexedEntity.getTwinClassFieldId())
                        .setValue(twinFieldSimpleNonIndexedEntity.getValue());
            }
            case UPDATE -> {
                if (twinFieldSimpleNonIndexedEntity.getId() == null) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field id required for field update");
                }

                draftTwinFieldSimpleNonIndexedEntity
                        .setTwinClassFieldId(twinFieldSimpleNonIndexedEntity.getTwinClassFieldId())
                        .setTwinFieldSimpleNonIndexedId(twinFieldSimpleNonIndexedEntity.getId())
                        .setValue(twinFieldSimpleNonIndexedEntity.getValue());
            }
        }

        return draftTwinFieldSimpleNonIndexedEntity;
    }

    public DraftTwinFieldBooleanEntity createFieldDraft(DraftEntity draftEntity, TwinFieldBooleanEntity twinFieldBooleanEntity) throws ServiceException {
        CUD cud = twinFieldBooleanEntity.getId() == null ? CUD.CREATE : CUD.UPDATE;

        DraftTwinFieldBooleanEntity draftTwinFieldBooleanEntity = new DraftTwinFieldBooleanEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinFieldBooleanEntity.getTwinId())
                .setCud(cud);

        switch (cud) {
            case CREATE -> {
                if (twinFieldBooleanEntity.getTwinClassFieldId() == null) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field is required for new field creation");
                }

                draftTwinFieldBooleanEntity
                        .setTwinClassFieldId(twinFieldBooleanEntity.getTwinClassFieldId())
                        .setValue(twinFieldBooleanEntity.getValue());
            }
            case UPDATE -> {
                if (twinFieldBooleanEntity.getId() == null) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field id required for field update");
                }

                draftTwinFieldBooleanEntity
                        .setTwinClassFieldId(twinFieldBooleanEntity.getTwinClassFieldId())
                        .setTwinFieldBooleanId(twinFieldBooleanEntity.getId())
                        .setValue(twinFieldBooleanEntity.getValue());
            }
        }

        return draftTwinFieldBooleanEntity;
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

    public DraftTwinFieldTwinClassEntity createFieldDraft(DraftEntity draftEntity, TwinFieldTwinClassEntity twinFieldTwinClassEntity) throws ServiceException {
        CUD cud = twinFieldTwinClassEntity.getId() == null ? CUD.CREATE : CUD.UPDATE;

        DraftTwinFieldTwinClassEntity draftTwinFieldTwinClassEntity = new DraftTwinFieldTwinClassEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinId(twinFieldTwinClassEntity.getTwinId())
                .setCud(cud);

        switch (cud) {
            case CREATE:
                if (twinFieldTwinClassEntity.getTwinClassFieldId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin class field is required for new field creation");
                draftTwinFieldTwinClassEntity
                        .setTwinClassFieldId(twinFieldTwinClassEntity.getTwinClassFieldId())
                        .setTwinClassId(twinFieldTwinClassEntity.getTwinClassId());
                break;
            case UPDATE:
                if (twinFieldTwinClassEntity.getId() == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_class_field.id required for field update");
                draftTwinFieldTwinClassEntity
                        .setTwinFieldTwinClassId(twinFieldTwinClassEntity.getId())
                        .setTwinClassId(twinFieldTwinClassEntity.getTwinClassId());
                break;
        }

        return draftTwinFieldTwinClassEntity;
    }

    public DraftTwinLinkEntity createTwinLinkDraft(DraftEntity draftEntity, TwinLinkEntity twinLinkEntity) throws ServiceException {
        CUD cud = twinLinkEntity.getId() == null ? CUD.CREATE : CUD.UPDATE;

        DraftTwinLinkEntity draftTwinLinkEntity = new DraftTwinLinkEntity()
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setCreatedByUserId(authService.getApiUser().getUserId())
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
                .setStorageFileKey(twinAttachmentEntity.getStorageFileKey())
                .setModifications(convertAttachmentModificationsToString(twinAttachmentEntity.getModifications()))
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

    private String convertAttachmentModificationsToString(Collection<TwinAttachmentModificationEntity> modifications) {
        StringBuilder sb = new StringBuilder();
        for (TwinAttachmentModificationEntity modification : modifications)
            sb.append(modification.easyLog(EasyLoggable.Level.DETAILED));
        return sb.toString();
    }

    public DraftTwinFieldUserEntity createFieldUserDeleteDraft(DraftEntity draftEntity, TwinFieldUserEntity twinFieldUser) throws ServiceException {
        if (twinFieldUser == null || twinFieldUser.getId() == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_field_user.id required for field deletion");
        return new DraftTwinFieldUserEntity()
                .setCud(CUD.DELETE)
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinFieldUserId(twinFieldUser.getId())
                .setUserId(twinFieldUser.getUserId())
                .setTwinId(twinFieldUser.getTwinId())
                .setTwinClassFieldId(twinFieldUser.getTwinClassFieldId());
    }

    public DraftTwinFieldTwinClassEntity createFieldTwinClassDeleteDraft(DraftEntity draftEntity, TwinFieldTwinClassEntity twinFieldTwinClassEntity) throws ServiceException {
        if (twinFieldTwinClassEntity == null || twinFieldTwinClassEntity.getId() == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "twin_field_twin_class.id required for field deletion");
        return new DraftTwinFieldTwinClassEntity()
                .setCud(CUD.DELETE)
                .setDraft(draftEntity)
                .setDraftId(draftEntity.getId())
                .setTimeInMillis(System.currentTimeMillis())
                .setTwinFieldTwinClassId(twinFieldTwinClassEntity.getId())
                .setTwinClassId(twinFieldTwinClassEntity.getTwinClassId())
                .setTwinId(twinFieldTwinClassEntity.getTwinId())
                .setTwinClassFieldId(twinFieldTwinClassEntity.getTwinClassFieldId());
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
        if (!draftCollector.isWritable())
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_NOT_WRITABLE, "current draft is already not writable");
        if (draftCollector.hasChanges()) {
            saveEntities(draftCollector, DraftTwinPersistEntity.class, draftTwinPersistRepository);
            saveEntities(draftCollector, DraftTwinEraseEntity.class, draftTwinEraseRepository);
            saveEntities(draftCollector, DraftTwinAttachmentEntity.class, draftTwinAttachmentRepository);
            saveEntities(draftCollector, DraftTwinLinkEntity.class, draftTwinLinkRepository);
            saveEntities(draftCollector, DraftTwinTagEntity.class, draftTwinTagRepository);
            saveEntities(draftCollector, DraftTwinMarkerEntity.class, draftTwinMarkerRepository);
            saveEntities(draftCollector, DraftTwinFieldSimpleEntity.class, draftTwinFieldSimpleRepository);
            saveEntities(draftCollector, DraftTwinFieldSimpleNonIndexedEntity.class, draftTwinFieldSimpleNonIndexedRepository);
            saveEntities(draftCollector, DraftTwinFieldBooleanEntity.class, draftTwinFieldBooleanRepository);
            saveEntities(draftCollector, DraftTwinFieldUserEntity.class, draftTwinFieldUserRepository);
            saveEntities(draftCollector, DraftTwinFieldTwinClassEntity.class, draftTwinFieldTwinClassRepository);
            saveEntities(draftCollector, DraftTwinFieldDataListEntity.class, draftTwinFieldDataListRepository);

            if (!draftCollector.getDraftEntitiesMap().isEmpty())
                for (Map.Entry<Class<?>, Set<Object>> classChanges : draftCollector.getDraftEntitiesMap().entrySet()) {
                    log.warn("Unsupported entity class[{}] for saving", classChanges.getKey().getSimpleName());
                }
        }
        saveDraftHistory(draftCollector);
//        entityManager.flush();
        draftCollector.clear();
    }

    private void saveDraftHistory(DraftCollector draftCollector) throws ServiceException {
        List<HistoryEntity> historyEntities = historyService.convertToEntities(draftCollector.getHistoryCollector());
        if (CollectionUtils.isEmpty(historyEntities))
            return;
        List<DraftHistoryEntity> draftHistoryEntities = new ArrayList<>();
        for (HistoryEntity historyEntity : historyEntities) {
            draftHistoryEntities.add(new DraftHistoryEntity()
                    .setHistoryType(historyEntity.getHistoryType())
                    .setContext(historyEntity.getContext())
                    .setDraftId(draftCollector.getDraftId())
                    .setActorUserId(historyEntity.getActorUserId())
                    .setSnapshotMessage(historyEntity.getSnapshotMessage())
                    .setTwinId(historyEntity.getTwinId())
                    .setCreatedAt(historyEntity.getCreatedAt())
                    .setTwinClassFieldId(historyEntity.getTwinClassFieldId())
            );
        }
        entitySmartService.saveAllAndLog(draftHistoryEntities, draftHistoryRepository);
        draftCollector.getHistoryCollector().clear();
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
        switch (draftCollector.getDraftEntity().getStatus()) {
            case UNDER_CONSTRUCTION: // case for minor draft, with no deletion (called from start draft thread)
            case ERASE_SCOPE_COLLECT_IN_PROGRESS:  // case for major draft, with deletion (called from erase scope collect task)
                draftCollector.getDraftEntity().setStatus(DraftStatus.UNCOMMITED);
                try {
                    draftNormalizeService.normalizeDraft(draftCollector);
                } catch (Exception e) { //todo create DraftException extends ServiceException + Status
                    draftCollector.getDraftEntity()
                            .setStatus(DraftStatus.NORMALIZE_EXCEPTION)
                            .setStatusDetails(e.getMessage());
                    break;
                }
                try {
                    draftCheckConflictsService.checkConflicts(draftCollector);
                } catch (ServiceException e) { //todo DraftException
                    draftCollector.getDraftEntity()
                            .setStatus(DraftStatus.CHECK_CONFLICTS_EXCEPTION)
                            .setStatusDetails(e.getMessage());
                    break;
                }
                break;
            case ERASE_SCOPE_COLLECT_PLANNED:  // case for major draft, with deletion (called from start draft thread)
                draftCollector.getDraftEntity().setStatus(DraftStatus.ERASE_SCOPE_COLLECT_NEED_START);
                break;
        }
        entitySmartService.save(draftCollector.getDraftEntity(), draftRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
    }




}
