package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.dto.rest.twin.TwinCreateRqDTOv2;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemporalIdResolver {

    private static final String TEMPORAL_ID_PREFIX = "temporalId:";

    /**
     * Checks if a value is a temporalId reference
     */
    public boolean isTemporalReference(String value) {
        return value != null && value.startsWith(TEMPORAL_ID_PREFIX);
    }

    /**
     * Extracts the key from a temporalId reference
     */
    public String extractTemporalKey(String value) {
        return value.substring(TEMPORAL_ID_PREFIX.length());
    }

    /**
     * Resolves a value - either a temporalId reference or a regular UUID
     */
    public UUID resolveUuid(String value, Map<String, UUID> temporalIdMap) throws ServiceException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        if (isTemporalReference(value)) {
            String key = extractTemporalKey(value);
            UUID resolved = temporalIdMap.get(key);
            if (resolved == null) {
                throw new ServiceException(ErrorCodeTwins.TEMPORAL_ID_NOT_FOUND,
                    "Temporal ID reference not found: " + key);
            }
            return resolved;
        }

        // Regular UUID - validate and return
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ServiceException(ErrorCodeTwins.INVALID_TEMPORAL_REFERENCE,
                "Invalid UUID or temporal reference: " + value);
        }
    }

    /**
     * Validates temporalId uniqueness in the request
     */
    public void validateTemporalIdUniqueness(List<TwinCreateRqDTOv2> twins) throws ServiceException {
        Set<String> seen = new HashSet<>();
        for (TwinCreateRqDTOv2 twin : twins) {
            if (twin.getTemporalId() != null) {
                if (!seen.add(twin.getTemporalId())) {
                    throw new ServiceException(ErrorCodeTwins.DUPLICATE_TEMPORAL_ID,
                        "Duplicate temporalId: " + twin.getTemporalId());
                }
            }
        }
    }

    /**
     * Detects cyclic dependencies in temporalId references
     */
    public void detectCycles(List<TwinCreate> twinCreates) throws ServiceException {
        // Build graph of temporalId -> headTwinId references
        Map<String, String> graph = new HashMap<>();
        for (TwinCreate tc : twinCreates) {
            if (tc.getTemporalId() != null && tc.getHeadTwinRef() != null) {
                if (isTemporalReference(tc.getHeadTwinRef())) {
                    graph.put(tc.getTemporalId(), extractTemporalKey(tc.getHeadTwinRef()));
                }
            }
        }

        // Detect cycles using DFS
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String node : graph.keySet()) {
            if (hasCycle(node, graph, visited, recursionStack)) {
                throw new ServiceException(ErrorCodeTwins.CYCLIC_DEPENDENCY,
                    "Cyclic dependency detected in temporal references starting from: " + node);
            }
        }
    }

    private boolean hasCycle(String node, Map<String, String> graph,
                            Set<String> visited, Set<String> recursionStack) {
        if (recursionStack.contains(node)) return true;
        if (visited.contains(node)) return false;

        visited.add(node);
        recursionStack.add(node);

        String neighbor = graph.get(node);
        if (neighbor != null && hasCycle(neighbor, graph, visited, recursionStack)) {
            return true;
        }

        recursionStack.remove(node);
        return false;
    }

    /**
     * Builds temporalId -> generated UUID mapping
     */
    public Map<String, UUID> buildTemporalIdMap(List<TwinCreate> twinCreates, Map<String, UUID> createdIds) {
        Map<String, UUID> temporalIdMap = new HashMap<>();
        for (int i = 0; i < twinCreates.size(); i++) {
            TwinCreate tc = twinCreates.get(i);
            if (tc.getTemporalId() != null) {
                // Get generated UUID by index
                UUID generatedId = createdIds.get("twin_" + i);
                if (generatedId != null) {
                    temporalIdMap.put(tc.getTemporalId(), generatedId);
                }
            }
        }
        return temporalIdMap;
    }
}
