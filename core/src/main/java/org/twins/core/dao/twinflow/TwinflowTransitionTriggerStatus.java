package org.twins.core.dao.twinflow;

import lombok.Getter;

@Getter
public enum TwinflowTransitionTriggerStatus {
    NEED_START,
    IN_PROGRESS,
    DONE,
    FAILED;
}
