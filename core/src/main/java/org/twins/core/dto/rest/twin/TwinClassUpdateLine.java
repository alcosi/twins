package org.twins.core.dto.rest.twin;

public enum TwinClassUpdateLine {

    throwOnHeadCantBeTransferred,
    throwOnFieldCantBeTransferred,
    throwOnFieldRequiredNotFilled;

    @Override
    public String toString() {
        return name();
    }
}
