package org.twins.core.service.draft;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dao.draft.DraftRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class DraftCommitScheduler {
    final ApplicationContext applicationContext;
    final DraftRepository draftRepository;
    @Qualifier("draftCommitExecutor")
    final TaskExecutor taskExecutor;

    //todo add to settings
    @Scheduled(fixedDelayString = "2000")
    public void processNotifications() {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("draftCommitScheduler$");
            log.debug("Loading email task from database");
            List<DraftEntity> draftEntities = draftRepository.findByStatusIdIn(List.of(DraftEntity.Status.COMMIT_NEED_START));
            if (CollectionUtils.isEmpty(draftEntities)) {
                log.debug("No draft need to be commited");
                return;
            }
            log.info("{} drafts need to be commited", draftEntities.size());
            for (DraftEntity draftEntity : draftEntities) {
                try {
                    log.info("Running draft commit[{}] from status[{}]", draftEntity.getId(), draftEntity.getStatus());
                    draftEntity.setStatus(DraftEntity.Status.COMMIT_IN_PROGRESS);
                    draftRepository.save(draftEntity);
                    DraftCommitTask draftCommitTask = applicationContext.getBean(DraftCommitTask.class, draftEntity);
                    taskExecutor.execute(draftCommitTask);
                } catch (Exception e) {
                    log.error("Exception ex: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Exception: ", e);
        } finally {
            LoggerUtils.cleanMDC();
        }
    }
}
