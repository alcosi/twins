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
public class DraftEraseScopeCollectScheduler {
    final ApplicationContext applicationContext;
    final DraftRepository draftRepository;
    @Qualifier("draftCollectEraseScopeExecutor")
    final TaskExecutor taskExecutor;

    @Scheduled(fixedDelayString = "${draft.erase.scope.collect.scheduler.delay:2000}")
    public void collectEraseScope() {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("draftCollectEraseScopeScheduler$");
            log.debug("Loading erase scope collect tasks from database");
            List<DraftEntity> draftEntities = draftRepository.findByStatusIdIn(List.of(DraftStatus.ERASE_SCOPE_COLLECT_NEED_START));
            if (CollectionUtils.isEmpty(draftEntities)) {
                log.debug("No erase scopes collect tasks");
                return;
            }
            log.info("{} drafts ease scopes need to be collected", draftEntities.size());
            for (DraftEntity draftEntity : draftEntities) {
                try {
                    log.info("Running draft[{}] erase scope collect from status[{}]", draftEntity.getId(), draftEntity.getStatus());
                    draftEntity.setStatus(DraftStatus.ERASE_SCOPE_COLLECT_IN_PROGRESS);
                    draftRepository.save(draftEntity);
                    DraftEraseScopeCollectTask draftCommitTask = applicationContext.getBean(DraftEraseScopeCollectTask.class, draftEntity);
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
