package org.twins.core.service.draft;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.draft.*;
import org.twins.core.domain.draft.DraftCollector;
import org.twins.core.domain.draft.DraftCounters;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.List;

import static org.twins.core.domain.draft.DraftCounters.Counter.*;
import static org.twins.core.domain.draft.DraftCounters.CounterGroup.ERASES;
import static org.twins.core.domain.draft.DraftCounters.CounterGroup.PERSISTS;

@Service
@RequiredArgsConstructor
public class DraftCounterService {
    private final DraftTwinTagRepository draftTwinTagRepository;
    private final DraftTwinMarkerRepository draftTwinMarkerRepository;
    private final DraftTwinEraseRepository draftTwinEraseRepository;
    private final DraftTwinAttachmentRepository draftTwinAttachmentRepository;
    private final DraftTwinLinkRepository draftTwinLinkRepository;
    private final DraftTwinFieldSimpleRepository draftTwinFieldSimpleRepository;
    private final DraftTwinFieldUserRepository draftTwinFieldUserRepository;
    private final DraftTwinFieldDataListRepository draftTwinFieldDataListRepository;
    private final DraftTwinPersistRepository draftTwinPersistRepository;

    public DraftCounters syncCounters(DraftCollector draftCollector) throws ServiceException {
        DraftCounters counters = draftCollector.getDraftCounters();
        if (counters.getInvalid().isEmpty())
            return counters;
        syncPersists(draftCollector);
        syncErases(draftCollector);
        return counters;
    }

    private void syncPersists(DraftCollector draftCollector) {
        if (!draftCollector.getDraftCounters().isInvalid(PERSISTS))
            return;
        List<Object[]> count = draftTwinPersistRepository.getCounters(draftCollector.getDraftId());
        for (Object[] row : count) {
            if ((Boolean)row[0]) //createElseUpdate
                draftCollector.getDraftCounters().set(PERSIST_CREATE, (Integer) row[1]);
            else
                draftCollector.getDraftCounters().set(PERSIST_UPDATE, (Integer) row[1]);
        }
    }

    private void syncErases(DraftCollector draftCollector) throws ServiceException {
        if (!draftCollector.getDraftCounters().isInvalid(ERASES))
            return;
        List<Object[]> counters = draftTwinEraseRepository.getCounters(draftCollector.getDraftId());
        DraftCounters.Counter counter = null;
        for (Object[] row : counters) {
            counter = switch ((DraftTwinEraseEntity.Status) row[0]) {
                case UNDETECTED -> ERASE_UNDETECTED;
                case STATUS_CHANGE_ERASE_DETECTED -> ERASE_BY_STATUS;
                case LOCK_DETECTED -> ERASE_LOCK;
                case IRREVOCABLE_ERASE_DETECTED -> ERASE_IRREVOCABLE_DETECTED;
                case IRREVOCABLE_ERASE_HANDLED -> ERASE_IRREVOCABLE_HANDLED;
                case CASCADE_DELETION_PAUSE -> ERASE_CASCADE_PAUSE;
                case CASCADE_DELETION_EXTRACTED -> ERASE_CASCADE_EXTRACTED;
                case SKIP_DETECTED -> ERASE_SKIP;
                default -> throw new ServiceException(ErrorCodeTwins.TWIN_DRAFT_GENERAL_ERROR, "Unknown erase status[" + row[0] + "]");
            };
            draftCollector.getDraftCounters().set(counter, (Integer) row[1]);
        }
    }
}
