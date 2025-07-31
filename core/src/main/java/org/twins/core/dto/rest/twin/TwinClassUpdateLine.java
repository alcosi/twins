package org.twins.core.dto.rest.twin;

public enum TwinClassUpdateLine {
    headNull, anythingElse;

    @Override
    public String toString() {
        return name();
    }
}
