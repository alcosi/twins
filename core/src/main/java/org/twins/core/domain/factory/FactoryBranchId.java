package org.twins.core.domain.factory;

import java.util.UUID;

public class FactoryBranchId {
    String trace;

    public FactoryBranchId(String trace) {
        this.trace = trace;
    }

    public static FactoryBranchId root(UUID inbuiltFactoryId) {
        return new FactoryBranchId(inbuiltFactoryId.toString());
    }

    @Override
    public String toString() {
        return trace;
    }

    public boolean alreadyVisited(UUID factoryId) {
        return trace.contains(factoryId.toString());
    }

    public FactoryBranchId next(UUID id) {
        return new FactoryBranchId(trace + " > " + id.toString());
    }

    public boolean accessibleFrom(FactoryBranchId currentFactoryBranchId) {
        return currentFactoryBranchId.trace.startsWith(trace);
    }
}
