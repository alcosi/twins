package org.twins.core.service.draft;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.draft.DraftTwinPersistRepository;
import org.twins.core.domain.draft.DraftCollector;
import org.twins.core.domain.draft.DraftCounters;
import org.twins.core.exception.ErrorCodeTwins;

@Service
@Slf4j
@RequiredArgsConstructor
public class DraftCheckConflictsService {
    private final DraftCounterService draftCounterService;
    private final DraftTwinPersistRepository draftTwinPersistRepository;


    public void checkConflicts(DraftCollector draftCollector) throws ServiceException {
        DraftCounters draftCounters = draftCounterService.syncCounters(draftCollector);
        if (!draftCounters.canBeCommited())
            throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_CAN_NOT_BE_COMMITED, draftCollector.getDraftEntity().logNormal() + " can not be commited, because of unsuitable erase statuses");
        if (draftCollector.getDraftEntity().getTwinEraseIrrevocableCount() == 0)
            return; //hope we will have no conflicts in such case
        boolean hasConflicts = false;
        int count = 0;
        if (draftCollector.getDraftEntity().getTwinPersistCount() > 0) {
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
