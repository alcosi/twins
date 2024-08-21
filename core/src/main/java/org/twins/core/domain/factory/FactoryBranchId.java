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

    public static final String DELIMITER = " > ";

    public FactoryBranchId next(UUID id) {
        return new FactoryBranchId(trace + DELIMITER + id.toString());
    }

    public boolean accessibleFrom(FactoryBranchId currentFactoryBranchId) {
        return currentFactoryBranchId.trace.startsWith(trace);
    }

    public FactoryBranchId previous() {
        String newTrace;
        if (trace.contains(DELIMITER))
            newTrace = trace.substring(0, trace.indexOf(DELIMITER));
        else
            newTrace = "";
        return new FactoryBranchId(newTrace);
    }
}
