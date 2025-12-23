package org.twins.core.service.draft;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.draft.DraftTwinPersistRepository;
import org.twins.core.domain.draft.DraftCollector;
import org.twins.core.domain.draft.DraftCounters;
import org.twins.core.exception.ErrorCodeTwins;

import static org.twins.core.domain.draft.DraftCounters.Counter.ERASE_IRREVOCABLE_HANDLED;
import static org.twins.core.domain.draft.DraftCounters.CounterGroup.PERSISTS;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
public class DraftCheckConflictsService {
    private final DraftCounterService draftCounterService;
    private final DraftTwinPersistRepository draftTwinPersistRepository;


    public void checkConflicts(DraftCollector draftCollector) throws ServiceException {
        DraftCounters draftCounters = draftCounterService.syncCounters(draftCollector.getDraftEntity());
        if (!draftCounters.canBeCommited())
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_CAN_NOT_BE_COMMITED, draftCollector.getDraftEntity().logNormal() + " can not be commited, because of unsuitable erase statuses");
        if (draftCollector.getDraftCounters().isZero(ERASE_IRREVOCABLE_HANDLED))
            return; //hope we will have no conflicts in such case
        boolean hasConflicts = false;
        int count = 0;
        if (draftCollector.getDraftCounters().moreThenZero(PERSISTS)) {
            count = draftTwinPersistRepository.countPersistedWithDeletedHead(draftCollector.getDraftId());
            if (count > 0) {
                hasConflicts = true;
                log.error("{} has {} [persistedWithDeletedHead] conflicts", draftCollector.getDraftEntity().logNormal(), count);
            }
        }
        if (hasConflicts)
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_CAN_NOT_BE_COMMITED, draftCollector.getDraftEntity().logNormal() + " has unresolvable conflicts");
    }
}
