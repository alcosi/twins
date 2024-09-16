package org.twins.core.service.draft;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.draft.*;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class DraftCommitService {
    private final DraftRepository draftRepository;
    private final DraftHistoryRepository draftHistoryRepository;
    private final DraftTwinTagRepository draftTwinTagRepository;
    private final DraftTwinMarkerRepository draftTwinMarkerRepository;
    private final DraftTwinEraseRepository draftTwinEraseRepository;
    private final DraftTwinAttachmentRepository draftTwinAttachmentRepository;
    private final DraftTwinLinkRepository draftTwinLinkRepository;
    private final DraftTwinFieldSimpleRepository draftTwinFieldSimpleRepository;
    private final DraftTwinFieldUserRepository draftTwinFieldUserRepository;
    private final DraftTwinFieldDataListRepository draftTwinFieldDataListRepository;
    private final DraftTwinPersistRepository draftTwinPersistRepository;
    @Lazy
    private final DraftService draftService;

    @Transactional
    public DraftEntity commitNowOrInQueue(UUID draftId) throws ServiceException {
        return commitNowOrInQueue(draftService.findEntitySafe(draftId));
    }

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

    // all draft data should be normalized and check before (for best performance)
    @Transactional(rollbackFor = Throwable.class)
    public void commitNow(DraftEntity draftEntity) throws ServiceException {
        try {
            commitTwinErase(draftEntity);
            commitTwinPersist(draftEntity);
            commitTwinFields(draftEntity);
            commitTwinLinks(draftEntity);
            commitTwinMarkers(draftEntity);
            commitTwinTags(draftEntity);
            commitTwinAttachments(draftEntity);
            commitHistory(draftEntity);
            draftEntity.setStatus(DraftEntity.Status.COMMITED);
            draftRepository.save(draftEntity);
        } catch (Exception e) {
            draftEntity
                    .setStatus(DraftEntity.Status.COMMIT_EXCEPTION)
                    .setStatusDetails(e instanceof ServiceException se ? se.log() : e.getMessage());
            updateDraftInNewTransaction(draftEntity);
            throw e;
        }
    }

    // If something goes wrong during draft commit, commit transaction should be rolled back,
    // but draft should change status. This must be done in 2 separate transactions
    private void updateDraftInNewTransaction(DraftEntity draftEntity) {
        Thread thread = new Thread(() -> {
                draftRepository.save(draftEntity);
        });
        thread.start();
    }


    private void commitHistory(DraftEntity draftEntity) {
        draftHistoryRepository.moveFromDraft(draftEntity.getId());
    }

    private void commitTwinErase(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinEraseCount() <= 0)
            return;
        log.info("commiting {} erase", draftEntity.getTwinEraseCount());
        commitTwinEraseIrrevocable(draftEntity);
        commit(draftEntity, draftEntity.getTwinEraseByStatusCount(), draftTwinEraseRepository::commitEraseByStatus, "erase by status changes");
    }

    private void commitTwinEraseIrrevocable(DraftEntity draftEntity) {
        if (draftEntity.getTwinEraseIrrevocableCount() <= 0)
            return;
        log.info("commiting {} erase irrevocable", draftEntity.getTwinEraseIrrevocableCount());
        String irrevocableDeleteIds = draftTwinEraseRepository.getIrrevocableDeleteIds(draftEntity.getId());
        if (StringUtils.isEmpty(irrevocableDeleteIds))
            return;
        checkCount(
                draftTwinEraseRepository.commitEraseIrrevocable(draftEntity.getId()),
                draftEntity.getTwinEraseIrrevocableCount(),
                "commitTwinEraseIrrevocable"); //this is the fastest way
        log.info("twins[{}] perhaps were deleted", irrevocableDeleteIds);

    }


    private void commitTwinPersist(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinPersistCount() <= 0)
            return;
        log.info("commiting {} persisted twins", draftEntity.getTwinPersistCount());
        commit(draftEntity, draftEntity.getTwinPersistCreateCount(), draftTwinPersistRepository::commitTwinsCreates, "twins: creation");
        commit(draftEntity, draftEntity.getTwinPersistUpdateCount(), draftTwinPersistRepository::commitTwinsUpdates, "twins: update");
    }

    private void commitTwinFields(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinFieldCount() <= 0)
            return;
        log.info("commiting {} twin fields", draftEntity.getTwinFieldCount());
        commitTwinFieldSimple(draftEntity);
        commitTwinFieldUser(draftEntity);
        commitTwinFieldDataList(draftEntity);
    }


    private void commitTwinFieldSimple(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinFieldSimpleCount() <= 0)
            return;
        log.info("commiting {} twin fields simple", draftEntity.getTwinFieldSimpleCount());
        commitTwinFieldSimpleDelete(draftEntity);
        commit(draftEntity, draftEntity.getTwinFieldSimpleCreateCount(), draftTwinFieldSimpleRepository::commitCreates, "twin fields [simple]: creation");
        commit(draftEntity, draftEntity.getTwinFieldSimpleUpdateCount(), draftTwinFieldSimpleRepository::commitUpdates, "twin fields [simple]: update");
    }

    private void commitTwinFieldSimpleDelete(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinFieldSimpleDeleteCount() <= 0)
            return;
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, "twin simple fields deletion is not implemented");
    }

    private void commitTwinFieldUser(DraftEntity draftEntity) {
        if (draftEntity.getTwinFieldUserCount() <= 0)
            return;
        log.info("commiting {} twin fields [user]", draftEntity.getTwinFieldUserCount());
        commit(draftEntity, draftEntity.getTwinFieldUserCreateCount(), draftTwinFieldUserRepository::commitCreates, "twin fields [user]: creation");
        commit(draftEntity, draftEntity.getTwinFieldUserUpdateCount(), draftTwinFieldUserRepository::commitUpdates, "twin fields [user]: update");
        commit(draftEntity, draftEntity.getTwinFieldUserDeleteCount(), draftTwinFieldUserRepository::commitDeletes, "twin fields [user]: deletion");
    }

    private void commitTwinFieldDataList(DraftEntity draftEntity) {
        if (draftEntity.getTwinFieldDataListCount() <= 0)
            return;
        log.info("commiting {} twin fields [data_list]", draftEntity.getTwinFieldDataListCount());
        commit(draftEntity, draftEntity.getTwinFieldDataListCreateCount(), draftTwinFieldDataListRepository::commitCreates, "twin fields [data_list]: creation");
        commit(draftEntity, draftEntity.getTwinFieldDataListUpdateCount(), draftTwinFieldDataListRepository::commitUpdates, "twin fields [data_list]: update");
        commit(draftEntity, draftEntity.getTwinFieldDataListDeleteCount(), draftTwinFieldDataListRepository::commitDeletes, "twin fields [data_list]: deletion");
    }

    private void commitTwinLinks(DraftEntity draftEntity) {
        if (draftEntity.getTwinLinkCount() <= 0)
            return;
        log.info("commiting {} twin links", draftEntity.getTwinLinkCount());
        commit(draftEntity, draftEntity.getTwinLinkCreateCount(), draftTwinLinkRepository::commitCreates, "links: creation");
        commit(draftEntity, draftEntity.getTwinLinkUpdateCount(), draftTwinLinkRepository::commitUpdates, "links: update");
        commit(draftEntity, draftEntity.getTwinLinkDeleteCount(), draftTwinLinkRepository::commitDeletes, "links: deletion");
    }


    private void commitTwinMarkers(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinMarkerCount() <= 0)
            return;
        log.info("commiting {} markers", draftEntity.getTwinMarkerCount());
        commit(draftEntity, draftEntity.getTwinMarkerCreateCount(), draftTwinMarkerRepository::commitMarkersAdd, "markers: added");
        commit(draftEntity, draftEntity.getTwinMarkerDeleteCount(), draftTwinMarkerRepository::commitMarkersDelete, "markers: deletion");
    }

    private void commitTwinTags(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinTagCount() <= 0)
            return;
        log.info("commiting {} tags", draftEntity.getTwinTagCount());
        commit(draftEntity, draftEntity.getTwinTagCreateCount(), draftTwinTagRepository::commitTagsAdd, "tags: creation");
        commit(draftEntity, draftEntity.getTwinTagDeleteCount(), draftTwinTagRepository::commitTagsDelete, "tags: deletion");
    }

    // we can check if current draft it minor
    public boolean isMinor(DraftEntity draftEntity) {
        //todo move to properties
        return draftEntity.getAllChangesCount() <= 20;
    }

    private void commitTwinAttachments(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getTwinAttachmentCount() <= 0)
            return;
        log.info("commiting {} attachments", draftEntity.getTwinAttachmentCount());
        commit(draftEntity, draftEntity.getTwinAttachmentCreateCount(), draftTwinAttachmentRepository::commitAttachmentsCreate, "attachments: creation");
        commit(draftEntity, draftEntity.getTwinAttachmentUpdateCount(), draftTwinAttachmentRepository::commitAttachmentsUpdate, "attachments: update");
        commit(draftEntity, draftEntity.getTwinAttachmentDeleteCount(), draftTwinAttachmentRepository::commitAttachmentsDelete, "attachments: deletion");
    }

    private void commit(DraftEntity draftEntity, int expectedCounter, Function<UUID, Integer> commitFunction, String what) {
        if (expectedCounter <= 0)
            return;
        log.info("commiting {} {}", expectedCounter, what);
        checkCount(
                commitFunction.apply(draftEntity.getId()),
                expectedCounter,
                commitFunction.toString());
    }

    private static void checkCount(long actualCount, long expectedCount, String where) {
        if (expectedCount != actualCount)
            log.warn("{}: expected count {} but got count {}", where, expectedCount, actualCount);
    }
}
