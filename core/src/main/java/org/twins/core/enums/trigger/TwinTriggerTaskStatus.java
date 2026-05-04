package org.twins.core.enums.trigger;

import lombok.Getter;

@Getter
public enum TwinTriggerTaskStatus {
    NEED_START,
    IN_PROGRESS,
    DONE,
    FAILED,
    SYNC_EXECUTION;
}
