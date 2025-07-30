package org.twins.core.dao.twin;

import lombok.Getter;

@Getter
public enum TwinChangeTaskStatus {
    NEED_START,
    WAITING_FOR_DRAFT_COMMIT, //todo implement logic in draft service to change this status
    IN_PROGRESS,
    DONE,
    FAILED;
}
