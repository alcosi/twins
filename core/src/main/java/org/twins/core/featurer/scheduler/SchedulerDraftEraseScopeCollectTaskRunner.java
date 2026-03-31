package org.twins.core.featurer.scheduler;

import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dao.draft.DraftRepository;
import org.twins.core.enums.draft.DraftStatus;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.scheduler.tasks.DraftEraseScopeCollectTask;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;

@Service
@Featurer(
        id = FeaturerTwins.ID_5004,
        name = "SchedulerDraftEraseScopeCollectTaskRunner",
        description = "Scheduler for executing draft erases"
)
public class SchedulerDraftEraseScopeCollectTaskRunner extends SchedulerTaskRunner<DraftEraseScopeCollectTask, DraftEntity> {

    private final DraftRepository draftRepository;

    protected SchedulerDraftEraseScopeCollectTaskRunner(@Qualifier("draftCollectEraseScopeExecutor") Executor taskExecutor,
                                                        DraftRepository draftRepository) {
        super(taskExecutor);
        this.draftRepository = draftRepository;
    }

    @Override
    protected Class<DraftEraseScopeCollectTask> getTaskClass() {
        return DraftEraseScopeCollectTask.class;
    }

    @Override
    protected Collection<DraftEntity> setStatusAndSave(Collection<DraftEntity> collectedEntities) {
        collectedEntities.forEach(entity -> entity.setStatus(DraftStatus.ERASE_SCOPE_COLLECT_IN_PROGRESS));
        return draftRepository.saveAll(collectedEntities);
    }

    @Override
    protected List<DraftEntity> collectAll() {
        return draftRepository.findByStatusIn(List.of(DraftStatus.ERASE_SCOPE_COLLECT_NEED_START));
    }

    @Override
    protected List<DraftEntity> collectBatch(int batchSize) {
        return draftRepository.findByStatusIn(List.of(DraftStatus.ERASE_SCOPE_COLLECT_NEED_START), PageRequest.of(0, batchSize));
    }
}
