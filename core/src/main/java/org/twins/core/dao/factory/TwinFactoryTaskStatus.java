package org.twins.core.dao.factory;

import lombok.Getter;

@Getter
public enum TwinFactoryTaskStatus {
    NEED_START,
    IN_PROGRESS,
    DONE,
    FAILED;
}
