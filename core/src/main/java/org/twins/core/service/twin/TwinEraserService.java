package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.draft.DraftService;

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

    public void deleteTwin(UUID twinId) throws ServiceException {
        deleteTwin(twinService.findEntitySafe(twinId));
    }

    public DraftEntity deleteTwinDrafted(UUID twinId) throws ServiceException {
        return deleteTwinDrafted(twinService.findEntitySafe(twinId));
    }

    public DraftEntity deleteTwinDrafted(TwinEntity twinEntity) throws ServiceException {
        return draftService.draftErase(twinEntity);
    }

    public void deleteTwin(TwinEntity twinEntity) throws ServiceException {
        DraftEntity draftEntity = draftService.draftErase(twinEntity);
        draftService.commit(draftEntity.getId());
    }
}