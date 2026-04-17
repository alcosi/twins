package org.twins.core.service.twin;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequestScope
@Getter
public class TemporalIdContext {
    private final Map<String, UUID> temporalIdMap = new HashMap<>();
    private List<Integer> sortedIndices = null;

    public void put(String temporalId, UUID uuid) {
        temporalIdMap.put(temporalId, uuid);
    }

    public UUID get(String temporalId) {
        return temporalIdMap.get(temporalId);
    }

    public boolean hasTemporalId(String temporalId) {
        return temporalIdMap.containsKey(temporalId);
    }

    public void setSortedIndices(List<Integer> sortedIndices) {
        this.sortedIndices = sortedIndices;
    }

    public void clear() {
        temporalIdMap.clear();
        sortedIndices = null;
    }
}
