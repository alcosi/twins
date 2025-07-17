package org.twins.core.domain.factory;

public enum FactoryLauncher {
    transition, targetDeletion, cascadeDeletion, twinCreate, twinUpdate, twinSketch;

    public boolean isDeletion() {
        return targetDeletion == this || cascadeDeletion == this;
    }

}
