package org.twins.core.enums.twinflow;

import lombok.Getter;

@Getter
public enum TwinflowTransitionType {
    STATUS_CHANGE,
    OPERATION,
    MARKETING,
    STATUS_CHANGE_MARKETING,
    OPERATION_DISABLE;
}
