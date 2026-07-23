package org.twins.core.enums.factory;

public enum FactoryLauncher {
    transition,
    factoryPipeline,
    targetDeletion,
    cascadeDeletion,
    onTwinCreate,
    onTwinCreateAfterRecompute,
    onTwinUpdate,
    onTwinUpdateAfterRecompute,
    onSketchCreate,
    onSketchCreateAfterRecompute,
    onSketchUpdate,
    onSketchUpdateAfterRecompute,
    onSketchFinalize,
    afterTwinCreate,
    afterTwinUpdate,
    afterSketchCreate,
    afterSketchUpdate,
    afterSketchFinalize,
    afterSketchFinalizeRestricted,
    afterTransitionPerform;

    public boolean isDeletion() {
        return targetDeletion == this || cascadeDeletion == this;
    }
}
