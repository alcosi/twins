package org.twins.core.featurer.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dao.draft.DraftRepository;
import org.twins.core.enums.draft.DraftStatus;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.draft.DraftEraseScopeCollectTask;

import java.util.List;
import java.util.Properties;

@RequiredArgsConstructor
@Service
@Slf4j
@Featurer(
        id = FeaturerTwins.ID_4704,
        name = "DraftEraseScopeCollectScheduler",
        description = "Scheduler for executing draft erases"
)
public class DraftEraseScopeCollectScheduler extends Scheduler {

    @FeaturerParam(
            name = "batchSize",
            description = "Param to specify the number of tasks that will be collected from db for execution"
    )
    public static final FeaturerParamInt batchSizeParam = new FeaturerParamInt("batchSize");

    final DraftRepository draftRepository;
    @Qualifier("draftCollectEraseScopeExecutor")
    final TaskExecutor taskExecutor;

    public String processTasks(Properties properties) {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("draftCollectEraseScopeScheduler$");

            var draftEntities = collectTasks(batchSizeParam.extract(properties));

            if (CollectionUtils.isEmpty(draftEntities)) {
                log.debug("No erase scopes collect tasks");
                return "";
            }

            log.info("{} drafts erase scopes need to be collected", draftEntities.size());
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

            return STR."\{draftEntities.size()} task(s) from db was processed";
        } catch (Exception e) {
            log.error("Exception: ", e);
        } finally {
            LoggerUtils.cleanMDC();
        }

        return "";
    }

    private List<DraftEntity> collectTasks(Integer batchSize) {
        log.debug("Loading erase scope collect tasks from database");

        if (batchSize == null) {
            return draftRepository.findByStatusIn(List.of(DraftStatus.ERASE_SCOPE_COLLECT_NEED_START));
        } else {
            return draftRepository.findByStatusIn(List.of(DraftStatus.ERASE_SCOPE_COLLECT_NEED_START), PageRequest.of(0, batchSize));
        }
    }
}
