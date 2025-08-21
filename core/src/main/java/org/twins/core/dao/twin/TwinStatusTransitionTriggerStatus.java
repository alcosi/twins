package org.twins.core.dao.twin;

import lombok.Getter;

@Getter
public enum TwinStatusTransitionTriggerStatus {
    NEED_START,
    IN_PROGRESS,
    DONE,
    FAILED;
}
