package org.twins.core.featurer.scheduler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.LoggerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.domain.draft.DraftCollector;
import org.twins.core.enums.draft.DraftStatus;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.draft.DraftService;

@Component
@Scope("prototype")
@Slf4j
public class DraftEraseScopeCollectTask implements Runnable {
    private final DraftEntity draftEntity;

    @Autowired
    private DraftService draftService;

    @Autowired
    private AuthService authService;


    public DraftEraseScopeCollectTask(DraftEntity draftEntity) {
        this.draftEntity = draftEntity;
    }

    @Override
    public void run() {
        DraftCollector draftCollector = new DraftCollector(draftEntity);
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("draftEraseScopeCollect$");
            LoggerUtils.logPrefix("DRAFT[" + draftEntity.getId() + "]:");
            log.info("Performing draft erase scope collect: {}", draftEntity.logNormal());
            authService.setThreadLocalApiUser(draftEntity.getDomainId(), draftEntity.getBusinessAccountId(), draftEntity.getCreatedByUserId());
            draftService.createEraseScope(draftCollector);
        } catch (ServiceException e) {
            log.error(e.log());
            draftCollector.getDraftEntity()
                    .setStatus(DraftStatus.ERASE_SCOPE_COLLECT_EXCEPTION)
                    .setStatusDetails(e.log());
        } catch (Throwable e) {
            log.error("Exception: ", e);
            draftCollector.getDraftEntity()
                    .setStatus(DraftStatus.ERASE_SCOPE_COLLECT_EXCEPTION)
                    .setStatusDetails(e.getMessage());
        } finally {
            try {
                draftService.endDraft(draftCollector);
            } catch (ServiceException e) {
                log.error("End draft critical exception: ", e);
            }
            authService.removeThreadLocalApiUser();
            LoggerUtils.cleanMDC();
        }
    }

}
