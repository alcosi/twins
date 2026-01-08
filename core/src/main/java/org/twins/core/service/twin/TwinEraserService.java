package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.draft.DraftCommitService;
import org.twins.core.service.draft.DraftService;

import java.util.Set;
import java.util.UUID;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Lazy
@RequiredArgsConstructor
public class TwinEraserService {

    @Lazy
    private final TwinService twinService;
    @Lazy
    private final DraftService draftService;
    private final DraftCommitService draftCommitService;

    public DraftEntity eraseTwin(UUID twinId) throws ServiceException {
        return eraseTwins(twinService.findEntitySafe(twinId));
    }

    public DraftEntity eraseTwinDrafted(UUID twinId) throws ServiceException {
        return eraseTwinDrafted(twinService.findEntitySafe(twinId));
    }

    public DraftEntity eraseTwinDrafted(TwinEntity twinEntity) throws ServiceException {
        twinService.checkDeletePermission(twinEntity);
        return draftService.draftErase(twinEntity);
    }

    public DraftEntity eraseTwins(TwinEntity... twinEntityList) throws ServiceException {
        for (TwinEntity twinEntity : twinEntityList)
            twinService.checkDeletePermission(twinEntity);
        DraftEntity draftEntity = draftService.draftErase(twinEntityList);
        draftCommitService.commitNowOrInQueue(draftEntity);
        return draftEntity;
    }

    public DraftEntity eraseTwins(Set<UUID> twinIds) throws ServiceException {
        return eraseTwins(twinService.findEntitiesSafe(twinIds).getList().toArray(TwinEntity[]::new));
    }


}
