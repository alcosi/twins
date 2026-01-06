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
    private final Executor taskExecutor;

    protected SchedulerTaskRunner(Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    protected String processTasks(Properties properties) {
        try {
            LoggerUtils.logController(getLogSource() + "$");

            var collectedEntities = collectTasks(batchSizeParam.extract(properties));

            if (CollectionUtils.isEmpty(collectedEntities)) {
                log.debug("No tasks were collected");
                return "";
            }

            var savedEntities = setStatusAndSave(collectedEntities);

            log.info("{} tasks need to be done", savedEntities.size());
            for (var entity : savedEntities) {
                try {
                    log.info("Running {}", entity.logNormal());
                    var task = applicationContext.getBean(getTaskClass(), entity);
                    taskExecutor.execute(task);
                } catch (Exception e) {
                    log.error("Exception ex: {}", e.getMessage(), e);
                }
            }

            return STR."\{savedEntities.size()} task(s) from db was processed";
        } catch (Exception e) {
            log.error("Exception: ", e);

            return STR."Processing tasks failed with exception: \{e}";
        } finally {
            LoggerUtils.cleanMDC();
        }
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
