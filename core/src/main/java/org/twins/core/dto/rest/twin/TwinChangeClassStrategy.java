package org.twins.core.dto.rest.twin;

public enum TwinChangeClassStrategy {

    throwOnFieldCantBeTransferred,
    throwOnFieldRequiredNotFilled;

    @Override
    public String toString() {
        return name();
    }
}
