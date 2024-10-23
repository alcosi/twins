package org.twins.core.service.draft;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.LoggerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.domain.draft.DraftCollector;
import org.twins.core.service.auth.AuthService;

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
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("draftEraseScopeCollect$");
            LoggerUtils.logPrefix("DRAFT[" + draftEntity.getId() + "]:");
            log.info("Performing draft erase scope collect: {}", draftEntity.logNormal());
            authService.getApiUser();
            DraftCollector draftCollector = new DraftCollector(draftEntity);
            draftService.createEraseScope(draftCollector);
            draftService.endDraft(draftCollector);
        } catch (ServiceException e) {
            log.error(e.log());
        } catch (Throwable e) {
            log.error("Exception: ", e);
        } finally {
            LoggerUtils.cleanMDC();
        }
    }

}
