package org.twins.core.domain.factory;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
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

    @Override
    public boolean equals(Object obj) {
        return StringUtils.equals(trace, ((FactoryBranchId)obj).trace);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(trace);
    }

    public boolean alreadyVisited(UUID factoryId) {
        return trace.contains(factoryId.toString());
    }

    public static final String DELIMITER = " > ";

    public FactoryBranchId next(UUID id) {
        return new FactoryBranchId(trace + DELIMITER + id.toString());
    }

    public FactoryBranchId previous() {
        String newTrace;
        if (trace.contains(DELIMITER))
            newTrace = StringUtils.substringBeforeLast(trace, DELIMITER);
        else
            newTrace = "";
        return new FactoryBranchId(newTrace);
    }

    public FactoryBranchId enterPipeline(UUID id) {
        return new FactoryBranchId(trace + " [" + id.toString() + "]");
    }

    public FactoryBranchId exitPipeline() {
        String newTrace;
        if (trace.contains("["))
            newTrace = StringUtils.substringBeforeLast(trace, " [");
        else
            newTrace = "";
        return new FactoryBranchId(newTrace);
    }

    public FactoryBranchId getCurrentPipeline() {
        String newTrace;
        if (trace.contains("]"))
            newTrace = StringUtils.substringBeforeLast(trace, "]") + "]";
        else
            newTrace = "";
        return new FactoryBranchId(newTrace);
    }

    public boolean accessibleFrom(FactoryBranchId currentFactoryBranchId) {
        return currentFactoryBranchId.trace.startsWith(trace);
    }


}
