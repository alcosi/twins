package org.twins.core.featurer.scheduler.tasks;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.LoggerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.service.draft.DraftCommitService;

@Component
@Scope("prototype")
@Slf4j
public class DraftCommitTask implements Runnable {
    private final DraftEntity draftEntity;

    @Autowired
    private DraftCommitService draftCommitService;


    public DraftCommitTask(DraftEntity draftEntity) {
        this.draftEntity = draftEntity;
    }

    @Override
    public void run() {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("draftCommit$");
            LoggerUtils.logPrefix("DRAFT[" + draftEntity.getId() + "]:");
            log.info("Performing draft commit: {}", draftEntity.logNormal());
            draftCommitService.commitNow(draftEntity);
        } catch (ServiceException e) {
            log.error(e.log());
        } catch (Throwable e) {
            log.error("Exception: ", e);
        } finally {
            LoggerUtils.cleanMDC();
        }
    }

}
