package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.service.draft.DraftCommitService;
import org.twins.core.service.draft.DraftService;

import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class TwinEraserService {
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final DraftService draftService;
    private final DraftCommitService draftCommitService;
    private final TwinRepository twinRepository;
    private final EntitySmartService entitySmartService;

    public void eraseTwin(UUID twinId) throws ServiceException {
        eraseTwin(twinService.findEntitySafe(twinId));
    }

    public DraftEntity eraseTwinDrafted(UUID twinId) throws ServiceException {
        return eraseTwinDrafted(twinService.findEntitySafe(twinId));
    }

    public DraftEntity eraseTwinDrafted(TwinEntity twinEntity) throws ServiceException {
        return draftService.draftErase(twinEntity);
    }

    public void eraseTwin(TwinEntity twinEntity) throws ServiceException {
        DraftEntity draftEntity = draftService.draftErase(twinEntity);
        draftCommitService.commitNowOrInQueue(draftEntity);
    }

    public void irrevocableDelete(Set<UUID> irrevocableDeleteTwinIds) {
        entitySmartService.deleteAllAndLog(irrevocableDeleteTwinIds, twinRepository);
    }
}
