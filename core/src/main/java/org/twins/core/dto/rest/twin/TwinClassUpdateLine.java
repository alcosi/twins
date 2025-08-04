package org.twins.core.dto.rest.twin;

public enum TwinClassUpdateLine {

    throwOnHeadCantBeTransferred,
    throwOnFieldCantBeTransferred;

    @Override
    public String toString() {
        return name();
    }
}
