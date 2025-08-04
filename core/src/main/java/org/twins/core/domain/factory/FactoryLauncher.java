package org.twins.core.domain.factory;

public enum FactoryLauncher {
    transition,
    targetDeletion,
    cascadeDeletion,
    onTwinCreate,
    onTwinUpdate,
    onSketchCreate,
    onSketchUpdate,
    onSketchFinalize,
    afterTwinCreate,
    afterTwinUpdate,
    afterSketchCreate,
    afterSketchUpdate,
    afterSketchFinalize,
    afterTransitionPerform;

    public boolean isDeletion() {
        return targetDeletion == this || cascadeDeletion == this;
    }
}
