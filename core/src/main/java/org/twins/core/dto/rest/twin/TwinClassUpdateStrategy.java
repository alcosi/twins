package org.twins.core.dto.rest.twin;

public enum TwinClassUpdateStrategy {

    throwOnFieldCantBeTransferred,
    throwOnFieldRequiredNotFilled;

    @Override
    public String toString() {
        return name();
    }
}
