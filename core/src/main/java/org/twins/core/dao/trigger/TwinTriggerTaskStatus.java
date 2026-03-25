package org.twins.core.dao.trigger;

import lombok.Getter;

@Getter
public enum TwinTriggerTaskStatus {
    NEED_START,
    IN_PROGRESS,
    DONE,
    FAILED;
}
