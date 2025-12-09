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
import org.twins.core.service.draft.DraftCommitTask;

import java.util.List;
import java.util.Properties;

@RequiredArgsConstructor
@Service
@Slf4j
@Featurer(
        id = FeaturerTwins.ID_4705,
        name = "DraftCommitScheduler",
        description = "Scheduler for executing draft commits"
)
public class DraftCommitScheduler extends Scheduler {

    @FeaturerParam(
            name = "batchSize",
            description = "Param to specify the number of tasks that will be collected from db for execution"
    )
    public static final FeaturerParamInt batchSizeParam = new FeaturerParamInt("batchSize");

    final DraftRepository draftRepository;
    @Qualifier("draftCommitExecutor")
    final TaskExecutor taskExecutor;

    protected String processTasks(Properties properties) {
        try {
            LoggerUtils.logSession();
            LoggerUtils.logController("draftCommitScheduler$");

            var collectedEntities = collectTasks(batchSizeParam.extract(properties));

            if (CollectionUtils.isEmpty(collectedEntities)) {
                log.debug("No draft need to be commited");
                return "";
            }

            collectedEntities.forEach(entity -> entity.setStatus(DraftStatus.COMMIT_IN_PROGRESS));
            var savedEntities = draftRepository.saveAll(collectedEntities);

            log.info("{} drafts need to be commited", collectedEntities.size());
            for (var draftEntity : savedEntities) {
                try {
                    log.info("Running draft commit[{}] from status[{}]", draftEntity.getId(), draftEntity.getStatus());
                    var draftCommitTask = applicationContext.getBean(DraftCommitTask.class, draftEntity);
                    taskExecutor.execute(draftCommitTask);
                } catch (Exception e) {
                    log.error("Exception ex: {}", e.getMessage(), e);
                }
            }

            return STR."\{savedEntities.size()} task(s) from db was processed";
        } catch (Exception e) {
            log.error("Exception: ", e);
        } finally {
            LoggerUtils.cleanMDC();
        }

        return "";
    }

    private List<DraftEntity> collectTasks(Integer batchSize) {
        log.debug("Loading draft commit task from database");

        if (batchSize == null) {
            return draftRepository.findByStatusInAndAutoCommit(List.of(DraftStatus.UNCOMMITED), true);
        } else {
            return draftRepository.findByStatusInAndAutoCommit(List.of(DraftStatus.UNCOMMITED), true, PageRequest.of(0, batchSize));
        }
    }
}
