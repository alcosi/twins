package org.twins.core.service.notification;

import lombok.Getter;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;

import java.util.*;

@Getter
public class HistoryNotificationChunk {
    /**
     * task → valid configs (used by processTask). Empty if no task matched any config.
     */
    private final Map<HistoryNotificationTaskEntity, List<HistoryNotificationEntity>> configsByTask;
    /**
     * config → tasks that matched AND passed validation (inverse projection, used by recipient precompute).
     */
    private final Map<HistoryNotificationEntity, LinkedHashSet<HistoryNotificationTaskEntity>> tasksByConfig;
    /**
     * chunk-wide domain id (chunk = one domain).
     */
    private final UUID domainId;

    public HistoryNotificationChunk() {
        this(new HashMap<>(), new HashMap<>(), null);
    }

    public HistoryNotificationChunk(
            Map<HistoryNotificationTaskEntity, List<HistoryNotificationEntity>> configsByTask,
            Map<HistoryNotificationEntity, LinkedHashSet<HistoryNotificationTaskEntity>> tasksByConfig,
            UUID domainId) {
        this.configsByTask = configsByTask;
        this.tasksByConfig = tasksByConfig;
        this.domainId = domainId;
    }
}
