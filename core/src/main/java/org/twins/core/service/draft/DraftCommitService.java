package org.twins.core.service.draft;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.draft.*;
import org.twins.core.enums.draft.DraftStatus;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.UUID;
import java.util.function.Function;

import static org.twins.core.domain.draft.DraftCounters.Counter.*;
import static org.twins.core.domain.draft.DraftCounters.CounterGroup.*;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
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
    private final DraftTwinFieldSimpleNonIndexedRepository draftTwinFieldSimpleNonIndexedRepository;
    private final DraftTwinFieldBooleanRepository draftTwinFieldBooleanRepository;
    private final DraftTwinFieldUserRepository draftTwinFieldUserRepository;
    private final DraftTwinFieldDataListRepository draftTwinFieldDataListRepository;
    private final DraftTwinPersistRepository draftTwinPersistRepository;
    private final DraftTwinFieldTwinClassRepository draftTwinFieldTwinClassRepository;
    @Lazy
    private final DraftService draftService;
    @Lazy
    private final DraftCounterService draftCounterService;

    @Transactional
    public DraftEntity commitNowOrInQueue(UUID draftId) throws ServiceException {
        return commitNowOrInQueue(draftService.findEntitySafe(draftId));
    }

    @Transactional
    public DraftEntity commitNowOrInQueue(DraftEntity draftEntity) throws ServiceException {
        switch (draftEntity.getStatus()) {
            case UNCOMMITED:
                if (isMinor(draftEntity)) {
                    commitNow(draftEntity);
                } else {
                    markAutocommit(draftEntity);
                }
                break;
            case ERASE_SCOPE_COLLECT_PLANNED:
            case ERASE_SCOPE_COLLECT_NEED_START:
                markAutocommit(draftEntity);
                break;
            default:
                throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_CAN_NOT_BE_COMMITED, "current draft can not be commited");
        }
        return draftEntity;
    }

    private void markAutocommit(DraftEntity draftEntity) {
        draftEntity.setAutoCommit(true); // this will trigger commit after finishing of scope creation
        draftRepository.save(draftEntity);
    }


    // all draft data should be normalized and check before (for best performance)
    @Transactional(rollbackFor = Throwable.class)
    public void commitNow(DraftEntity draftEntity) throws ServiceException {
        try {
            draftCounterService.syncCounters(draftEntity);
            if (!draftEntity.getCounters().canBeCommited())
                throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_CAN_NOT_BE_COMMITED, draftEntity.logNormal() + " can not be commited, because of unsuitable erase statuses");
            commitTwinErase(draftEntity);
            commitTwinPersist(draftEntity);
            commitTwinFields(draftEntity);
            commitTwinLinks(draftEntity);
            commitTwinMarkers(draftEntity);
            commitTwinTags(draftEntity);
            commitTwinAttachments(draftEntity);
            commitHistory(draftEntity);
            draftEntity.setStatus(DraftStatus.COMMITED);
            draftRepository.save(draftEntity);
        } catch (Exception e) {
            draftEntity
                    .setStatus(DraftStatus.COMMIT_EXCEPTION)
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
        log.info("commiting history");
        draftHistoryRepository.moveFromDraft(draftEntity.getId());
    }

    private void commitTwinErase(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getCounters().isZero(ERASES))
            return;
        log.info("commiting {} erase", draftEntity.getCounters().getOrZero(ERASES));
        commitTwinEraseIrrevocable(draftEntity);
        //todo todo check if such twins are stored in draft_persist table, looks so
//        commit(draftEntity, draftEntity.getCounters().getOrZero(ERASE_BY_STATUS), draftTwinEraseRepository::commitEraseByStatus, "erase by status changes");
    }

    private void commitTwinEraseIrrevocable(DraftEntity draftEntity) {
        int counter = draftEntity.getCounters().getOrZero(ERASE_IRREVOCABLE_HANDLED);
        if (counter == 0)
            return;
        log.info("commiting {} erase irrevocable", counter);
        String irrevocableDeleteIds = draftTwinEraseRepository.getIrrevocableDeleteIds(draftEntity.getId());
        if (StringUtils.isEmpty(irrevocableDeleteIds))
            return;
        checkCount(
                draftTwinEraseRepository.commitEraseIrrevocable(draftEntity.getId()),
                counter,
                "commitTwinEraseIrrevocable"); //this is the fastest way
        log.info("twins[{}] perhaps were deleted", irrevocableDeleteIds);

    }


    private void commitTwinPersist(DraftEntity draftEntity) throws ServiceException {
        int counter = draftEntity.getCounters().getOrZero(PERSISTS);
        if (counter == 0)
            return;
        log.info("commiting {} persisted twins", counter);
        //todo run runTwinStatusTransitionTriggers
        commit(draftEntity, draftEntity.getCounters().getOrZero(PERSIST_CREATE), draftTwinPersistRepository::commitTwinsCreates, "twins: creation");
        commit(draftEntity, draftEntity.getCounters().getOrZero(PERSIST_UPDATE), draftTwinPersistRepository::commitTwinsUpdates, "twins: update");
    }

    private void commitTwinFields(DraftEntity draftEntity) throws ServiceException {
        int counter = draftEntity.getCounters().getOrZero(FIELDS);

        if (counter == 0) {
            return;
        }

        log.info("commiting {} twin fields", counter);
        commitTwinFieldSimple(draftEntity);
        commitTwinFieldSimpleNonIndexed(draftEntity);
        commitTwinFieldBoolean(draftEntity);
        commitTwinFieldUser(draftEntity);
        commitTwinFieldDataList(draftEntity);
        commitTwinFieldTwinClass(draftEntity);
    }


    private void commitTwinFieldSimple(DraftEntity draftEntity) throws ServiceException {
        int counter = draftEntity.getCounters().getOrZero(FIELDS_SIMPLE);
        if (counter == 0)
            return;
        log.info("commiting {} twin fields simple", counter);
        commitTwinFieldSimpleDelete(draftEntity);
        commit(draftEntity, draftEntity.getCounters().getOrZero(FIELD_SIMPLE_CREATE), draftTwinFieldSimpleRepository::commitCreates, "twin fields [simple]: creation");
        commit(draftEntity, draftEntity.getCounters().getOrZero(FIELD_SIMPLE_UPDATE), draftTwinFieldSimpleRepository::commitUpdates, "twin fields [simple]: update");
    }

    private void commitTwinFieldSimpleNonIndexed(DraftEntity draftEntity) throws ServiceException {
        int counter = draftEntity.getCounters().getOrZero(FIELDS_SIMPLE_NON_INDEXED);

        if (counter == 0) {
            return;
        }

        log.info("commiting {} twin fields simple non indexed", counter);
        commitTwinFieldSimpleNonIndexedDelete(draftEntity);
        commit(draftEntity, draftEntity.getCounters().getOrZero(FIELD_SIMPLE_NON_INDEXED_CREATE), draftTwinFieldSimpleNonIndexedRepository::commitCreates, "twin fields [simple non indexed]: creation");
        commit(draftEntity, draftEntity.getCounters().getOrZero(FIELD_SIMPLE_NON_INDEXED_UPDATE), draftTwinFieldSimpleNonIndexedRepository::commitUpdates, "twin fields [simple non indexed]: update");
    }

    private void commitTwinFieldBoolean(DraftEntity draftEntity) throws ServiceException {
        int counter = draftEntity.getCounters().getOrZero(FIELDS_BOOLEAN);

        if (counter == 0) {
            return;
        }

        log.info("commiting {} twin fields boolean", counter);
        commitTwinFieldBooleanDelete(draftEntity);
        commit(draftEntity, draftEntity.getCounters().getOrZero(FIELD_BOOLEAN_CREATE), draftTwinFieldBooleanRepository::commitCreates, "twin fields [boolean]: creation");
        commit(draftEntity, draftEntity.getCounters().getOrZero(FIELD_BOOLEAN_UPDATE), draftTwinFieldBooleanRepository::commitUpdates, "twin fields [boolean]: update");
    }

    private void commitTwinFieldSimpleDelete(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getCounters().isZero(FIELD_SIMPLE_DELETE)) {
            return;
        }

        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, "twin simple fields deletion is not implemented");
    }

    private void commitTwinFieldSimpleNonIndexedDelete(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getCounters().isZero(FIELD_SIMPLE_DELETE)) {
            return;
        }

        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, "twin simple fields deletion is not implemented");
    }

    private void commitTwinFieldBooleanDelete(DraftEntity draftEntity) throws ServiceException {
        if (draftEntity.getCounters().isZero(FIELD_SIMPLE_DELETE)) {
            return;
        }

        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, "twin simple fields deletion is not implemented");
    }

    private void commitTwinFieldUser(DraftEntity draftEntity) {
        int counter = draftEntity.getCounters().getOrZero(FIELDS_USER);
        if (counter == 0)
            return;
        log.info("commiting {} twin fields [user]", counter);
        commit(draftEntity, draftEntity.getCounters().getOrZero(FIELD_USER_CREATE), draftTwinFieldUserRepository::commitCreates, "twin fields [user]: creation");
        commit(draftEntity, draftEntity.getCounters().getOrZero(FIELD_USER_UPDATE), draftTwinFieldUserRepository::commitUpdates, "twin fields [user]: update");
        commit(draftEntity, draftEntity.getCounters().getOrZero(FIELD_USER_DELETE), draftTwinFieldUserRepository::commitDeletes, "twin fields [user]: deletion");
    }

    private void commitTwinFieldTwinClass(DraftEntity draftEntity) {
        int counter = draftEntity.getCounters().getOrZero(FIELDS_TWIN_CLASS);
        if (counter == 0)
            return;
        log.info("commiting {} twin fields [twin_class]", counter);
        commit(draftEntity, draftEntity.getCounters().getOrZero(FIELD_TWIN_CLASS_CREATE), draftTwinFieldTwinClassRepository::commitCreates, "twin fields [twin_class]: creation");
        commit(draftEntity, draftEntity.getCounters().getOrZero(FIELD_TWIN_CLASS_UPDATE), draftTwinFieldTwinClassRepository::commitUpdates, "twin fields [twin_class]: update");
        commit(draftEntity, draftEntity.getCounters().getOrZero(FIELD_TWIN_CLASS_DELETE), draftTwinFieldTwinClassRepository::commitDeletes, "twin fields [twin_class]: deletion");
    }

    private void commitTwinFieldDataList(DraftEntity draftEntity) {
        int counter = draftEntity.getCounters().getOrZero(FIELDS_DATALIST);
        if (counter == 0)
            return;
        log.info("commiting {} twin fields [data_list]", counter);
        commit(draftEntity, draftEntity.getCounters().getOrZero(FIELD_DATALIST_CREATE), draftTwinFieldDataListRepository::commitCreates, "twin fields [data_list]: creation");
        commit(draftEntity, draftEntity.getCounters().getOrZero(FIELD_DATALIST_UPDATE), draftTwinFieldDataListRepository::commitUpdates, "twin fields [data_list]: update");
        commit(draftEntity, draftEntity.getCounters().getOrZero(FIELD_DATALIST_DELETE), draftTwinFieldDataListRepository::commitDeletes, "twin fields [data_list]: deletion");
    }

    private void commitTwinLinks(DraftEntity draftEntity) {
        int counter = draftEntity.getCounters().getOrZero(LINKS);
        if (counter == 0)
            return;
        log.info("commiting {} twin links", counter);
        commit(draftEntity, draftEntity.getCounters().getOrZero(LINK_CREATE), draftTwinLinkRepository::commitCreates, "links: creation");
        commit(draftEntity, draftEntity.getCounters().getOrZero(LINK_UPDATE), draftTwinLinkRepository::commitUpdates, "links: update");
        commit(draftEntity, draftEntity.getCounters().getOrZero(LINK_DELETE), draftTwinLinkRepository::commitDeletes, "links: deletion");
    }


    private void commitTwinMarkers(DraftEntity draftEntity) throws ServiceException {
        int counter = draftEntity.getCounters().getOrZero(MARKERS);
        if (counter == 0)
            return;
        log.info("commiting {} markers", counter);
        commit(draftEntity, draftEntity.getCounters().getOrZero(MARKER_CREATE), draftTwinMarkerRepository::commitMarkersAdd, "markers: added");
        commit(draftEntity, draftEntity.getCounters().getOrZero(MARKER_DELETE), draftTwinMarkerRepository::commitMarkersDelete, "markers: deletion");
    }

    private void commitTwinTags(DraftEntity draftEntity) throws ServiceException {
        int counter = draftEntity.getCounters().getOrZero(TAGS);
        if (counter == 0)
            return;
        log.info("commiting {} tags", counter);
        commit(draftEntity, draftEntity.getCounters().getOrZero(TAG_CREATE), draftTwinTagRepository::commitTagsAdd, "tags: creation");
        commit(draftEntity, draftEntity.getCounters().getOrZero(TAG_DELETE), draftTwinTagRepository::commitTagsDelete, "tags: deletion");
    }

    // we can check if current draft it minor
    public boolean isMinor(DraftEntity draftEntity) {
        //todo move to properties
        return draftEntity.getCounters().allCountersValues() <= 20;
    }

    private void commitTwinAttachments(DraftEntity draftEntity) throws ServiceException {
        int counter = draftEntity.getCounters().getOrZero(ATTACHMENTS);
        if (counter == 0)
            return;
        log.info("commiting {} attachments", counter);
        commit(draftEntity, draftEntity.getCounters().getOrZero(ATTACHMENT_CREATE), draftTwinAttachmentRepository::commitAttachmentsCreate, "attachments: creation");
        commit(draftEntity, draftEntity.getCounters().getOrZero(ATTACHMENT_UPDATE), draftTwinAttachmentRepository::commitAttachmentsUpdate, "attachments: update");
        commit(draftEntity, draftEntity.getCounters().getOrZero(ATTACHMENT_DELETE), draftTwinAttachmentRepository::commitAttachmentsDelete, "attachments: deletion");
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
