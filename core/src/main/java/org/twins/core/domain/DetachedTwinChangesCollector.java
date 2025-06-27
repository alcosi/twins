package org.twins.core.domain;

import jakarta.persistence.EntityManager;
import org.cambium.common.util.ChangesHelper;

public class DetachedTwinChangesCollector extends TwinChangesCollector {
    private final EntityManager entityManager;
    public DetachedTwinChangesCollector(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    protected ChangesHelper detectChangesHelper(Object entity) {
        entityManager.detach(entity);
        return super.detectChangesHelper(entity);
    }
}
