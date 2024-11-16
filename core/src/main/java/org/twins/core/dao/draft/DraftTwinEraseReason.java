package org.twins.core.dao.draft;

import lombok.Getter;

@Getter
public enum DraftTwinEraseReason {
    TARGET,
    CHILD,
    LINK,
    FACTORY;
}
