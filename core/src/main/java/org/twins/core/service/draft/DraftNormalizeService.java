package org.twins.core.service.draft;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.draft.*;
import org.twins.core.domain.draft.DraftCollector;
import org.twins.core.domain.draft.DraftCounters;

import static org.twins.core.domain.draft.DraftCounters.Counter.*;
import static org.twins.core.domain.draft.DraftCounters.CounterGroup.*;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
public class DraftNormalizeService {
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
    private final DraftCounterService draftCounterService;

    // let's try to clean some possible garbage (updates of twins that will be fully deleted)
    public void normalizeDraft(DraftCollector draftCollector) throws ServiceException {
        log.info("Normalize draft start");
        DraftCounters draftCounters = draftCounterService.syncCounters(draftCollector.getDraftEntity());
        if (draftCounters.getOrZero(ERASE_IRREVOCABLE_HANDLED) == 0)
            return; //nothing need to be normalized
        int normalizeCount = 0;
        if (draftCounters.moreThenZero(ERASE_CASCADE_PAUSE)) {
            // we can delete all persisted draft twins, if they must be deleted in future
            normalizeCount = draftTwinEraseRepository.normalizeDraft(draftCollector.getDraftId());
            if (normalizeCount > 0) {
                log.info("{} was normalized: {} erase items were moved from ERASE_CASCADE_PAUSE to ERASE_CASCADE_EXTRACTED status", draftCollector.getDraftEntity().logShort(), normalizeCount);
                draftCounters
                        .subtract(ERASE_CASCADE_PAUSE, normalizeCount)
                        .add(ERASE_CASCADE_EXTRACTED, normalizeCount);
            }
        }
        if (draftCounters.moreThenZero(PERSIST_CREATE)) {
            // we can delete all persisted draft twins, if they must be deleted in future
            normalizeCount = draftTwinPersistRepository.normalizeDraft(draftCollector.getDraftId());
            draftCounters
                    .subtract(PERSIST_CREATE, normalizeCount);
        }
        if (draftCounters.moreThenZero(TAGS)) {
            // we can delete all persisted draft twins tags, if twins must be deleted in future
            normalizeCount = draftTwinTagRepository.normalizeDraft(draftCollector.getDraftId());
            draftCounters
                    .invalidateIfNotZero(TAGS, normalizeCount);
        }
        if (draftCounters.moreThenZero(MARKERS)) {
            // we can delete all persisted draft twins markers, if twins must be deleted in future
            normalizeCount = draftTwinMarkerRepository.normalizeDraftByTwinDeletion(draftCollector.getDraftId());
            draftCounters
                    .invalidateIfNotZero(MARKERS, normalizeCount);
        }
        if (draftCounters.moreThenZero(ATTACHMENTS)) {
            // we can delete all persisted draft twins attachments, if twins must be deleted in future
            normalizeCount = draftTwinAttachmentRepository.normalizeDraft(draftCollector.getDraftId());
            draftCounters
                    .invalidateIfNotZero(ATTACHMENTS, normalizeCount);
        }
        if (draftCounters.moreThenZero(FIELDS_SIMPLE)) {
            // we can delete all persisted draft twins fields simple, if twins must be deleted in future
            normalizeCount = draftTwinFieldSimpleRepository.normalizeDraft(draftCollector.getDraftId());
            draftCounters
                    .invalidateIfNotZero(FIELDS_SIMPLE, normalizeCount);
        }
        if (draftCounters.moreThenZero(FIELDS_SIMPLE_NON_INDEXED)) {
            // we can delete all persisted draft twins fields simple npn indexed, if twins must be deleted in future
            normalizeCount = draftTwinFieldSimpleNonIndexedRepository.normalizeDraft(draftCollector.getDraftId());
            draftCounters
                    .invalidateIfNotZero(FIELDS_SIMPLE_NON_INDEXED, normalizeCount);
        }
        if (draftCounters.moreThenZero(FIELDS_BOOLEAN)) {
            // we can delete all persisted draft twins fields boolean, if twins must be deleted in future
            normalizeCount = draftTwinFieldBooleanRepository.normalizeDraft(draftCollector.getDraftId());
            draftCounters
                    .invalidateIfNotZero(FIELDS_BOOLEAN, normalizeCount);
        }
        if (draftCounters.moreThenZero(FIELDS_USER)) {
            // we can delete all persisted draft twins fields user, if twins must be deleted in future
            normalizeCount = draftTwinFieldUserRepository.normalizeDraft(draftCollector.getDraftId());
            draftCounters
                    .invalidateIfNotZero(FIELDS_USER, normalizeCount);
        }
        if (draftCounters.moreThenZero(FIELDS_TWIN_CLASS)) {
            // we can delete all persisted draft twins fields twin class, if twins must be deleted in future
            normalizeCount = draftTwinFieldTwinClassRepository.normalizeDraft(draftCollector.getDraftId());
            draftCounters
                    .invalidateIfNotZero(FIELDS_TWIN_CLASS, normalizeCount);
        }
        if (draftCounters.moreThenZero(FIELDS_DATALIST)) {
            // we can delete all persisted draft twins fields datalist, if twins must be deleted in future
            normalizeCount = draftTwinFieldDataListRepository.normalizeDraft(draftCollector.getDraftId());
            draftCounters
                    .invalidateIfNotZero(FIELDS_DATALIST, normalizeCount);
        }
        if (draftCounters.moreThenZero(LINK_DELETE)) {
            //we can clean only links, which must be deleted, all other should be checked during commit
            normalizeCount = draftTwinLinkRepository.normalizeDraft(draftCollector.getDraftId());
            draftCounters
                    .subtract(LINK_DELETE, normalizeCount);
        }
        //todo draft_history must be updated
    }
}
