package org.twins.core.dao.twin;

import lombok.Getter;

@Getter
public enum TwinChangeTaskStatus {
    NEED_START,
    IN_PROGRESS,
    DONE,
    FAILED;
}
