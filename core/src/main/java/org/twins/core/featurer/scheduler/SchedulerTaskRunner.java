package org.twins.core.featurer.scheduler;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.Executor;

@Slf4j
public abstract class SchedulerTaskRunner<T extends Runnable, E extends EasyLoggable> extends Scheduler {

    @FeaturerParam(
            name = "batchSize",
            description = "Param to specify the number of tasks that will be collected from db for execution",
            optional = true,
            defaultValue = "30"
    )
    public static final FeaturerParamInt batchSizeParam = new FeaturerParamInt("batchSize");
    private final Executor taskExecutor;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @PersistenceContext
    private EntityManager entityManager;

    protected SchedulerTaskRunner(Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /*
        The collect-vs-mark race this class used to warn about is fixed by {@link #collectAndMarkInProgress}:
        collect + setStatusAndSave run in one transaction. Implementations must make their collect methods
        select claimable rows FOR UPDATE SKIP LOCKED (see HistoryNotificationTaskRepository.findClaimableByStatusIdIn)
        — then overlapping scheduler ticks / app nodes claim disjoint task sets: no deadlocks, no double dispatch.
     */
    protected String processTask(Properties properties) {
        try {
            LoggerUtils.logController(getLogSource());

            var collectedEntities = collectAndMarkInProgress(batchSizeParam.extract(properties));

            if (CollectionUtils.isEmpty(collectedEntities)) {
                log.debug("No tasks were collected");
                return "";
            }

            log.info("{} tasks need to be done", collectedEntities.size());

            var failed = new ArrayList<E>();
            for (var entity : collectedEntities) {
                try {
                    log.info("Running {}", entity.logNormal());
                    var task = applicationContext.getBean(getTaskClass(), entity);
                    taskExecutor.execute(task);
                } catch (Exception e) {
                    log.error("Task {} submission rejected, will be reverted", entity.logNormal(), e);
                    failed.add(entity);
                }
            }

            if (!failed.isEmpty()) {
                try {
                    revertStatusAndSave(failed);
                    log.warn("{} task(s) submission failed — status reverted, they will be recollected", failed.size());
                } catch (Exception revertEx) {
                    log.error("Failed to revert status for {} task(s)", failed.size(), revertEx);
                }
            }

            return collectedEntities.size() + " task(s) from db was processed";
        } catch (Exception e) {
            log.error("Exception: ", e);

            return "Processing tasks failed with exception: " + e;
        } finally {
            LoggerUtils.cleanMDC();
        }
    }

    /**
     * Atomic queue claim: collect + setStatusAndSave in one programmatic transaction. TransactionTemplate
     * (not @Transactional) because processTask reaches it via self-invocation, which the Spring proxy
     * would ignore. Pair with FOR UPDATE SKIP LOCKED selects in collectAll/collectBatch to make the
     * claim atomic against overlapping scheduler ticks and multiple app nodes.
     */
    protected List<E> collectAndMarkInProgress(Integer batchSize) {
        log.debug("Loading tasks from database");
        return transactionTemplate.execute(_ -> {
            List<E> collectedEntities = batchSize == null ? collectAll() : collectBatch(batchSize);
            if (CollectionUtils.isEmpty(collectedEntities)) {
                return Collections.emptyList();
            }
            if (detachClaimed()) {
                detachClaimed(collectedEntities);
            }
            return new ArrayList<>(setStatusAndSave(collectedEntities));
        });
    }

    protected abstract Class<T> getTaskClass();
    protected abstract Collection<E> setStatusAndSave(Collection<E> collectedEntities);
    protected abstract List<E> collectAll();
    protected abstract List<E> collectBatch(int batchSize);
    protected abstract void revertStatusAndSave(Collection<E> entities);

    /**
     * Opt-in hook for bulk-update-based runners: the claim select returns MANAGED entities inside the
     * claim transaction — mutating them later (IN_PROGRESS mark, final statuses) would make Hibernate
     * flush a per-row UPDATE before the bulk statement (query-space auto-flush) and at commit
     * (dirty-check), i.e. the very N atomic updates the grouped bulk updates are meant to replace.
     * Return true to detach the claimed entities right after the select, so all persistence goes
     * through the bulk path. Default false: saveAll-based runners rely on managed entities to skip
     * merge SELECTs.
     */
    protected boolean detachClaimed() {
        return false;
    }

    private void detachClaimed(Collection<E> tasks) {
        for (E task : tasks) {
            entityManager.detach(task);
        }
    }
}
