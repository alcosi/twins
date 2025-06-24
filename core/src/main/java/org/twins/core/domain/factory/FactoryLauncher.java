package org.twins.core.domain.factory;

public enum FactoryLauncher {
    transition, targetDeletion, cascadeDeletion, twinCreate;

    public boolean isDeletion() {
        return targetDeletion == this || cascadeDeletion == this;
    }

}
