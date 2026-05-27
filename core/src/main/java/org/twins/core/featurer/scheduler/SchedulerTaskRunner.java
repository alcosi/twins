package org.twins.core.featurer.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

@Slf4j
public abstract class SchedulerTaskRunner<T extends Runnable, E extends EasyLoggable> extends Scheduler {

    @FeaturerParam(
            name = "batchSize",
            description = "Param to specify the number of tasks that will be collected from db for execution",
            optional = true
    )
    public static final FeaturerParamInt batchSizeParam = new FeaturerParamInt("batchSize");

    @FeaturerParam(
            name = "alertExecutionTime",
            description = "Alert threshold in ms. If a single task execution exceeds this time, a warning will be logged. Default: Integer.MAX_VALUE (no alert)",
            optional = true
    )
    public static final FeaturerParamInt alertExecutionTimeParam = new FeaturerParamInt("alertExecutionTime");

    private final Executor taskExecutor;

    protected SchedulerTaskRunner(Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /*
        possible race condition between collectTasks & setStatusAndSave with low fixed rate. should not happen for our load but...
        if it's happening:
        use batchSize param
        OR
        create new method with these 2 inside and mark it @Transactional. also use for update psql
        FOR UPDATE SKIP LOCKED mechanism in collectTasks, this will help (at least I hope so)
     */
    protected String processTask(Properties properties) {
        try {
            LoggerUtils.logController(getLogSource());

            var collectedEntities = collectTasks(batchSizeParam.extract(properties));

            if (CollectionUtils.isEmpty(collectedEntities)) {
                log.debug("No tasks were collected");
                return "";
            }

            var alertExecutionTime = alertExecutionTimeParam.extract(properties);
            if (alertExecutionTime == null) {
                alertExecutionTime = Integer.MAX_VALUE;
            }

            var savedEntities = setStatusAndSave(collectedEntities);

            log.info("{} tasks need to be done", savedEntities.size());
            for (var entity : savedEntities) {
                try {
                    log.info("Running {}", entity.logNormal());
                    var task = applicationContext.getBean(getTaskClass(), entity);
                    taskExecutor.execute(withExecutionTimeAlert(task, entity, alertExecutionTime));
                } catch (Exception e) {
                    log.error("Exception ex: {}", e.getMessage(), e);
                }
            }

            return savedEntities.size() + " task(s) from db was processed";
        } catch (Exception e) {
            log.error("Exception: ", e);

            return "Processing tasks failed with exception: " + e;
        } finally {
            LoggerUtils.cleanMDC();
        }
    }

    private Runnable withExecutionTimeAlert(Runnable task, E entity, int alertExecutionTime) {
        return () -> {
            var alertThread = Thread.ofVirtual()
                    .name("alert-" + entity.logShort())
                    .start(() -> {
                        try {
                            Thread.sleep(alertExecutionTime);
                            LoggerUtils.alertLog.warn("Task {} exceeded expected execution time of {} ms",
                                    entity.logNormal(), alertExecutionTime);
                        } catch (InterruptedException ignored) {
                            // task finished before the threshold — exit quietly
                        }
                    });
            try {
                task.run();
            } finally {
                alertThread.interrupt();
            }
        };
    }

    private List<E> collectTasks(Integer batchSize) {
        log.debug("Loading tasks from database");

        if (batchSize == null) {
            return collectAll();
        } else {
            return collectBatch(batchSize);
        }
    }

    protected abstract Class<T> getTaskClass();
    protected abstract Collection<E> setStatusAndSave(Collection<E> collectedEntities);
    protected abstract List<E> collectAll();
    protected abstract List<E> collectBatch(int batchSize);
}
