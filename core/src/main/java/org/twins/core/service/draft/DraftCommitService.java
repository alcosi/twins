package org.twins.core.service.draft;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.StringUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.CUD;
import org.twins.core.dao.draft.*;
import org.twins.core.dao.twin.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.TwinChangesService;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.twin.TwinService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class DraftCommitService {
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
    private final HistoryService historyService;
    @Lazy
    private final AuthService authService;
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final TwinChangesService twinChangesService;
    @Lazy
    private final AttachmentService attachmentService;
    @Lazy
    private final DraftService draftService;

    public DraftEntity commitNowOrInQueue(UUID draftId) throws ServiceException {
        return commitNowOrInQueue(draftService.findEntitySafe(draftId));
    }

    // commit can not be done by db function, because we need to create history
    // also if draft is huge we won't do it in current thread because it can be costly (if there are a lot of twins)
    @Transactional
    public DraftEntity commitNowOrInQueue(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getStatus() != DraftEntity.Status.UNCOMMITED)
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_CAN_NOT_BE_COMMITED, "current draft can not be commited");
        if (isMinor(draftEntity)) {
            commitNow(draftEntity);
        } else {
            draftRepository.save(draftEntity.setStatus(DraftEntity.Status.COMMIT_NEED_START));
        }
        return draftEntity;
    }

    public static final int PAGE_SIZE = 30;

    // all draft data should be normalized and check before (for best performance)
    @Transactional
    public void commitNow(DraftEntity draftEntity) throws ServiceException {
        commitTwinErase(draftEntity);
        commitTwinPersist(draftEntity);
        commitTwinFields(draftEntity);
        commitTwinMarkers(draftEntity);
        commitTwinTags(draftEntity);
        commitTwinAttachments(draftEntity);
    }

    private void commitTwinErase(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinEraseCount() <= 0)
            return;
        log.info("commiting {} erase", draftEntity.getTwinEraseCount());
        commitTwinEraseIrrevocable(draftEntity);
        commitTwinEraseWithStatusChange(draftEntity);
    }

    private void commitTwinEraseIrrevocable(DraftEntity draftEntity) {
        if (draftEntity.getTwinEraseIrrevocableCount() <= 0)
            return;
        log.info("commiting {} erase irrevocable", draftEntity.getTwinEraseIrrevocableCount());
        String irrevocableDeleteIds = draftTwinEraseRepository.getIrrevocableDeleteIds(draftEntity.getId());
        if (StringUtils.isNotEmpty(irrevocableDeleteIds)) {
            draftTwinEraseRepository.commitEraseIrrevocable(draftEntity.getId()); //this is the fastest way
            log.info("twins[{}] perhaps were deleted", irrevocableDeleteIds);
        }
    }

    private void commitTwinEraseWithStatusChange(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinEraseByStatusCount() <= 0)
            return;
        log.info("commiting {} erase by status changes", draftEntity.getTwinEraseByStatusCount());
        Slice<DraftTwinEraseEntity> slice = draftTwinEraseRepository.findByDraftIdAndEraseTwinStatusIdIsNotNullOrderByEraseTwinStatusId(draftEntity.getId(), PageRequest.of(0, PAGE_SIZE));
        boolean hasEraseStatusChange = slice.hasContent();
        boolean hasNext;
        List<DraftTwinEraseEntity> draftTwinEraseEntityList;
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        do {
            draftTwinEraseEntityList = slice.getContent();
            if (draftTwinEraseEntityList.isEmpty())
                break;
            for (DraftTwinEraseEntity draftTwinErase : draftTwinEraseEntityList) {
                if (draftTwinErase.isCauseGlobalLock() || !draftTwinErase.isEraseReady())
                    throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_CAN_NOT_BE_COMMITED, draftEntity.logShort() + " is locked by " + draftTwinErase);
                twinService.updateTwinStatus(draftTwinErase.getTwin(), draftTwinErase.getEraseTwinStatus(), twinChangesCollector);
            }
            //we use twinChangeCollector only to collect history. Status change can be done in one query
            twinChangesService.saveHistoryOnly(twinChangesCollector);
            twinChangesCollector.clear();
            hasNext = slice.hasNext();
            if (hasNext) {
                slice = draftTwinEraseRepository.findByDraftIdAndEraseTwinStatusIdIsNotNullOrderByEraseTwinStatusId(draftEntity.getId(), slice.nextPageable());
            }
        } while (hasNext);
        if (hasEraseStatusChange) //we will do update in one query
            draftTwinEraseRepository.commitEraseWithStatusChange(draftEntity.getId());
    }

    private void commitTwinPersist(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinPersistCount() <= 0)
            return;
        log.info("commiting {} persisted twins", draftEntity.getTwinPersistCount());
        commitTwinPersistUpdate(draftEntity);
        commitTwinPersistCreate(draftEntity);
    }

    private void commitTwinPersistUpdate(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinPersistUpdateCount() <= 0)
            return;
        log.info("commiting {} updated twins", draftEntity.getTwinPersistUpdateCount());
        Slice<DraftTwinPersistEntity> slice = draftTwinPersistRepository.findByDraftIdAndCreateElseUpdateFalse(draftEntity.getId(), PageRequest.of(0, PAGE_SIZE));
        boolean hasNext;
        List<DraftTwinPersistEntity> draftTwinUpdateEntityList;
        Kit<TwinEntity, UUID> dbEntities;
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        do {
            draftTwinUpdateEntityList = slice.getContent();
            if (draftTwinUpdateEntityList.isEmpty())
                break;
            // bulk load
            dbEntities = twinService.findEntitiesSafe(draftTwinUpdateEntityList.stream().map(DraftTwinPersistEntity::getTwinId).toList());
            for (DraftTwinPersistEntity draftTwinPersistEntity : draftTwinUpdateEntityList) {
                twinService.updateTwinBasics(convertDraft(draftTwinPersistEntity), dbEntities.get(draftTwinPersistEntity.getTwinId()), twinChangesCollector);
            }
            twinChangesService.applyChanges(twinChangesCollector);
            hasNext = slice.hasNext();
            if (hasNext) {
                slice = draftTwinPersistRepository.findByDraftIdAndCreateElseUpdateFalse(draftEntity.getId(), slice.nextPageable());
            }
        } while (hasNext);
    }

    private void commitTwinPersistCreate(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinPersistCreateCount() <= 0)
            return;
        log.info("commiting {} created twins", draftEntity.getTwinPersistCreateCount());
        Slice<DraftTwinPersistEntity> slice = draftTwinPersistRepository.findByDraftIdAndCreateElseUpdateTrue(draftEntity.getId(), PageRequest.of(0, PAGE_SIZE));
        boolean hasNext;
        List<DraftTwinPersistEntity> draftTwinCreateEntityList;
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        do {
            draftTwinCreateEntityList = slice.getContent();
            if (draftTwinCreateEntityList.isEmpty())
                break;
            for (DraftTwinPersistEntity draftTwinPersistEntity : draftTwinCreateEntityList) {
                twinService.createTwinEntity(convertDraft(draftTwinPersistEntity), twinChangesCollector);
            }
            twinChangesService.applyChanges(twinChangesCollector);
            hasNext = slice.hasNext();
            if (hasNext) {
                slice = draftTwinPersistRepository.findByDraftIdAndCreateElseUpdateTrue(draftEntity.getId(), slice.nextPageable());
            }
        } while (hasNext);
    }

    private void commitTwinFields(DraftEntity draftEntity) {
        if (draftEntity.getTwinFieldCount() <= 0)
            return;
        log.info("commiting {} twin fields", draftEntity.getTwinFieldCount());
        commitTwinFieldSimple(draftEntity);
        commitTwinFieldUser(draftEntity);
        commitTwinFieldDataList(draftEntity);
    }


    private void commitTwinFieldSimple(DraftEntity draftEntity) {
        if (draftEntity.getTwinFieldSimpleCount() <= 0)
            return;
        log.info("commiting {} twin fields simple", draftEntity.getTwinFieldSimpleCount());
        commitTwinFieldSimpleDelete(draftEntity);
        commitTwinFieldSimpleCreate(draftEntity);
        commitTwinFieldSimpleUpdate(draftEntity);
    }

    private void commitTwinFieldSimpleDelete(DraftEntity draftEntity) {

    }

    private void commitTwinFieldSimpleCreate(DraftEntity draftEntity) {
        if (draftEntity.getTwinFieldSimpleCreateCount() <= 0)
            return;
        log.info("commiting {} twin fields simple: created", draftEntity.getTwinFieldSimpleCreateCount());
        Slice<DraftTwinFieldSimpleEntity> slice = draftTwinFieldSimpleRepository.findByDraftIdAndCud(draftEntity.getId(), CUD.CREATE, PageRequest.of(0, PAGE_SIZE));
        boolean hasNext;
        List<DraftTwinFieldSimpleEntity> draftTwinFieldSimpleCreateEntityList;
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        do {
            draftTwinFieldSimpleCreateEntityList = slice.getContent();
            if (draftTwinFieldSimpleCreateEntityList.isEmpty())
                break;
            for (DraftTwinFieldSimpleEntity draftTwinPersistEntity : draftTwinFieldSimpleCreateEntityList) {
                twinChangesCollector.getHistoryCollector(fgdfg).add(historyService.fieldChangeSimple());
            }
            twinChangesService.applyChanges(twinChangesCollector);
            hasNext = slice.hasNext();
            if (hasNext) {
                slice = draftTwinPersistRepository.findByDraftIdAndCreateElseUpdateTrue(draftEntity.getId(), slice.nextPageable());
            }
        } while (hasNext);
    }

    private void commitTwinFieldSimpleUpdate(DraftEntity draftEntity) {

    }

    private void commitTwinFieldUser(DraftEntity draftEntity) {

    }

    private void commitTwinFieldDataList(DraftEntity draftEntity) {

    }

    private void commitTwinMarkers(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinMarkerCount() <= 0)
            return;
        log.info("commiting {} markers", draftEntity.getTwinMarkerCount());
        //todo add to history (currently no implemented)
        if (draftEntity.getTwinMarkerCreateCount() > 0)
            draftTwinMarkerRepository.commitMarkersAdd(draftEntity.getId());
        if (draftEntity.getTwinMarkerDeleteCount() > 0)
            draftTwinMarkerRepository.commitMarkersDelete(draftEntity.getId());
    }

    private void commitTwinTags(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinTagCount() <= 0)
            return;
        log.info("commiting {} tags", draftEntity.getTwinTagCount());
        //todo add to history (currently no implemented)
        if (draftEntity.getTwinTagCreateCount() > 0)
            draftTwinTagRepository.commitTagsAdd(draftEntity.getId());
        if (draftEntity.getTwinTagDeleteCount() > 0)
            draftTwinTagRepository.commitTagsDelete(draftEntity.getId());
    }

    // we can check if current draft it minor
    private boolean isMinor(DraftEntity draftEntity) {
        //todo move to properties
        return draftEntity.getAllChangesCount() <= 20;
    }

    private void commitTwinAttachments(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinAttachmentCount() <= 0)
            return;
        log.info("commiting {} attachments", draftEntity.getTwinAttachmentCount());
        commitAttachmentsCreate(draftEntity);
        commitAttachmentsUpdate(draftEntity);
        commitAttachmentsDelete(draftEntity);
    }

    private void commitAttachmentsCreate(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinAttachmentCreateCount() <= 0)
            return;
        log.info("commiting {} attachments create", draftEntity.getTwinAttachmentCreateCount());
        Slice<DraftTwinAttachmentEntity> slice = draftTwinAttachmentRepository.findByDraftIdAndCud(draftEntity.getId(), CUD.CREATE, PageRequest.of(0, PAGE_SIZE));
        boolean hasNext;
        List<DraftTwinAttachmentEntity> attachmentEntityList;
        List<TwinAttachmentEntity> attachmentAddList = new ArrayList<>();
        do {
            attachmentEntityList = slice.getContent();
            if (attachmentEntityList.isEmpty())
                break;
            for (DraftTwinAttachmentEntity draftTwinAttachmentCreate : attachmentEntityList) {
                attachmentAddList.add(convertDraft(draftTwinAttachmentCreate));
            }
            attachmentService.addAttachments(attachmentAddList);
            hasNext = slice.hasNext();
            if (hasNext) {
                slice = draftTwinAttachmentRepository.findByDraftIdAndCud(draftEntity.getId(), CUD.CREATE, PageRequest.of(0, PAGE_SIZE));
            }
        } while (hasNext);
    }

    private void commitAttachmentsUpdate(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinAttachmentUpdateCount() <= 0)
            return;
        log.info("commiting {} attachments update", draftEntity.getTwinAttachmentUpdateCount());
        Slice<DraftTwinAttachmentEntity> slice = draftTwinAttachmentRepository.findByDraftIdAndCud(draftEntity.getId(), CUD.UPDATE, PageRequest.of(0, PAGE_SIZE));
        boolean hasNext;
        List<DraftTwinAttachmentEntity> attachmentEntityList;
        List<TwinAttachmentEntity> attachmentUpdateList = new ArrayList<>();
        do {
            attachmentEntityList = slice.getContent();
            if (attachmentEntityList.isEmpty())
                break;
            for (DraftTwinAttachmentEntity draftTwinAttachmentCreate : attachmentEntityList) {
                attachmentUpdateList.add(convertDraft(draftTwinAttachmentCreate));
            }
            attachmentService.updateAttachments(attachmentUpdateList);
            hasNext = slice.hasNext();
            if (hasNext) {
                slice = draftTwinAttachmentRepository.findByDraftIdAndCud(draftEntity.getId(), CUD.UPDATE, PageRequest.of(0, PAGE_SIZE));
            }
        } while (hasNext);
    }

    private void commitAttachmentsDelete(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinAttachmentDeleteCount() <= 0)
            return;
        log.info("commiting {} attachments delete", draftEntity.getTwinAttachmentDeleteCount());
        Slice<DraftTwinAttachmentEntity> slice = draftTwinAttachmentRepository.findByDraftIdAndCud(draftEntity.getId(), CUD.DELETE, PageRequest.of(0, PAGE_SIZE));
        boolean hasNext;
        boolean hasAttachmentsToDelete = slice.hasContent();
        List<DraftTwinAttachmentEntity> attachmentEntityList;
        List<TwinAttachmentEntity> attachmentDeleteList = new ArrayList<>();
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        do {
            attachmentEntityList = slice.getContent();
            if (attachmentEntityList.isEmpty())
                break;
            for (DraftTwinAttachmentEntity draftTwinAttachmentCreate : attachmentEntityList) {
                attachmentDeleteList.add(convertDraft(draftTwinAttachmentCreate));
            }
            attachmentService.deleteAttachments(attachmentDeleteList, twinChangesCollector);
            //we use twinChangeCollector only to collect history. Deletes be done in one query
            twinChangesService.saveHistoryOnly(twinChangesCollector);
            hasNext = slice.hasNext();
            if (hasNext) {
                slice = draftTwinAttachmentRepository.findByDraftIdAndCud(draftEntity.getId(), CUD.DELETE, PageRequest.of(0, PAGE_SIZE));
            }
        } while (hasNext);
        //now we are ready to bulk delete
        if (hasAttachmentsToDelete)
            draftTwinAttachmentRepository.commitAttachmentDelete(draftEntity.getId());
    }

    private TwinAttachmentEntity convertDraft(DraftTwinAttachmentEntity draftTwinAttachmentCreate) {
        TwinAttachmentEntity ret = new TwinAttachmentEntity();
        switch (draftTwinAttachmentCreate.getCud()) {
            case UPDATE:
                ret
                        .setId(draftTwinAttachmentCreate.getId());
                //no break here
            case CREATE:
                ret
                        .setTwinId(draftTwinAttachmentCreate.getTwinId())
//                      .setTwin(draftTwinAttachmentCreate.getT) it can be new twin, not stored it twin table
                        .setCreatedByUser(draftTwinAttachmentCreate.getDraft().getCreatedByUser())
                        .setCreatedByUserId(draftTwinAttachmentCreate.getDraft().getCreatedByUserId())
                        .setExternalId(draftTwinAttachmentCreate.getExternalId())
                        .setTitle(draftTwinAttachmentCreate.getTitle())
                        .setDescription(draftTwinAttachmentCreate.getDescription())
                        .setStorageLink(draftTwinAttachmentCreate.getStorageLink())
                        .setCreatedAt(draftTwinAttachmentCreate.getDraft().getCreatedAt())
                        .setTwinCommentId(draftTwinAttachmentCreate.getTwinCommentId())
//                      .setTwinflowTransition(draftTwinAttachmentCreate.get)
                        .setTwinflowTransitionId(draftTwinAttachmentCreate.getTwinflowTransitionId())
//                      .setViewPermission(draftTwinAttachmentCreate.getViewPermissionId())
                        .setViewPermissionId(draftTwinAttachmentCreate.getViewPermissionId());
                break;
            case DELETE:
                ret
                        .setId(draftTwinAttachmentCreate.getId())
                        .setTwinId(draftTwinAttachmentCreate.getTwinId());
                break;
        }
        return ret;
    }

    private TwinEntity convertDraft(DraftTwinPersistEntity draftTwinPersistEntity) {
        TwinEntity twinEntity = new TwinEntity()
                .setTwinStatusId(draftTwinPersistEntity.getTwinStatusId())
                .setTwinStatus(draftTwinPersistEntity.getTwinStatus())
                .setName(draftTwinPersistEntity.getName())
                .setAssignerUserId(draftTwinPersistEntity.getAssignerUserId())
                .setAssignerUser(draftTwinPersistEntity.getAssigneeUser())
                .setCreatedByUserId(draftTwinPersistEntity.getCreatedByUserId())
                .setCreatedByUser(draftTwinPersistEntity.getCreatedByUser())
                .setCreatedAt(draftTwinPersistEntity.getDraft().getCreatedAt()) // not sure
                .setHeadTwinId(draftTwinPersistEntity.getHeadTwinId())
// it's difficult to fill headTwin, because it can be not present in twin table
//                .setHeadTwin(headTwin)
// we should not fill owner* fields, because they system level, anc can not be changes
//                .setOwnerUserId(draftTwinPersistEntity.getOwnerUserId())
//                .setOwnerBusinessAccountId(draftTwinPersistEntity.getOwnerUserId())
// we should not fill any *spaceId field, because they are calculated based on head
//                .setPermissionSchemaSpaceId(draftTwinPersistEntity.)
//                .setTwinflowSchemaSpaceId(twinflowSchemaSpaceId)
//                .setTwinClassSchemaSpaceId(twinClassSchemaSpaceId)
//                .setAliasSpaceId(aliasSpaceId)
                .setViewPermissionId(draftTwinPersistEntity.getViewPermissionId())
                .setExternalId(draftTwinPersistEntity.getExternalId())
                .setDescription(draftTwinPersistEntity.getDescription());

        if (!draftTwinPersistEntity.isCreateElseUpdate()) { // class must be filled only for creation
            twinEntity
                    .setTwinClassId(draftTwinPersistEntity.getTwinClassId())
                    .setTwinClass(draftTwinPersistEntity.getTwinClass());
        }
        return twinEntity;
    }
}
