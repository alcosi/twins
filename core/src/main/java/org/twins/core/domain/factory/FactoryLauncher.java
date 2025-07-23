package org.twins.core.domain.factory;

public enum FactoryLauncher {
    transition,
    targetDeletion,
    cascadeDeletion,
    beforeTwinCreate,
    beforeTwinUpdate,
    beforeTwinSketch,
    afterTwinCreate,
    afterTwinUpdate,
    afterTwinSketch;

    public boolean isDeletion() {
        return targetDeletion == this || cascadeDeletion == this;
    }

}
