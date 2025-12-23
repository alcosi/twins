package org.twins.core.service.draft;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
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
import org.twins.core.enums.draft.DraftStatus;

import java.util.List;

@RequiredArgsConstructor
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
public class DraftCommitScheduler {
    final ApplicationContext applicationContext;
    final DraftRepository draftRepository;
    @Qualifier("draftCommitExecutor")
    final TaskExecutor taskExecutor;

    @Scheduled(fixedDelayString = "${draft.commit.scheduler.delay:2000}")
    public void processDraftCommits() {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("draftCommitScheduler$");
            log.debug("Loading draft commit task from database");
            List<DraftEntity> draftEntities = draftRepository.findDraftsForCommit();
            if (CollectionUtils.isEmpty(draftEntities)) {
                log.debug("No draft need to be commited");
                return;
            }
            log.info("{} drafts need to be commited", draftEntities.size());
            for (DraftEntity draftEntity : draftEntities) {
                try {
                    log.info("Running draft commit[{}] from status[{}]", draftEntity.getId(), draftEntity.getStatus());
                    draftEntity.setStatus(DraftStatus.COMMIT_IN_PROGRESS);
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
