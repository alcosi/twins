package org.twins.core.featurer.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dao.draft.DraftRepository;
import org.twins.core.enums.draft.DraftStatus;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.scheduler.tasks.DraftCommitTask;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

@Service
@Slf4j
@Featurer(
        id = FeaturerTwins.ID_5005,
        name = "SchedulerDraftCommitTaskRunner",
        description = "Scheduler for executing draft commits"
)
public class SchedulerDraftCommitTaskRunner extends SchedulerTaskRunner<DraftCommitTask, DraftEntity> {

    private final DraftRepository draftRepository;

    protected SchedulerDraftCommitTaskRunner(@Qualifier("draftCommitExecutor") Executor taskExecutor,
                                             DraftRepository draftRepository) {
        super(taskExecutor);
        this.draftRepository = draftRepository;
    }

    @Override
    protected Class<DraftCommitTask> getTaskClass() {
        return DraftCommitTask.class;
    }

    @Override
    protected Collection<DraftEntity> setStatusAndSave(Collection<DraftEntity> collectedEntities) {
        collectedEntities.forEach(entity -> entity.setStatus(DraftStatus.COMMIT_IN_PROGRESS));
        return draftRepository.saveAll(collectedEntities);
    }

    @Override
    protected List<DraftEntity> collectAll() {
        return draftRepository.findByStatusInAndAutoCommit(List.of(DraftStatus.UNCOMMITED), true);
    }

    @Override
    protected List<DraftEntity> collectBatch(int batchSize) {
        return draftRepository.findByStatusInAndAutoCommit(List.of(DraftStatus.UNCOMMITED), true, PageRequest.of(0, batchSize));
    }
}
