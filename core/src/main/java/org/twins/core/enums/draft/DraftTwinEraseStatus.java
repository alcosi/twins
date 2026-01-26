package org.twins.core.enums.draft;

import lombok.Getter;

@Getter
public enum DraftTwinEraseStatus {
    // we do not know what should be done with current twin. so we will run target delete factory for it
    UNDETECTED,
    // current twin must be deleted. but we still do run cascade erase children and string links
    IRREVOCABLE_ERASE_DETECTED,
    IRREVOCABLE_ERASE_HANDLED,
    CASCADE_DELETION_PAUSE,
    CASCADE_DELETION_EXTRACTED,
    STATUS_CHANGE_ERASE_DETECTED,
    SKIP_DETECTED,
    // current twin locks deletion
    LOCK_DETECTED;
}
