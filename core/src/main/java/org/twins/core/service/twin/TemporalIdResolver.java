package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.domain.twinoperation.TwinCreate;
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
     * Checks if any twin in the list has a temporalId set
     */
    public boolean hasAnyTemporalId(List<TwinCreate> twinCreates) {
        return twinCreates.stream().anyMatch(tc -> tc.getTemporalId() != null);
    }

    /**
     * Extracts the key from a temporalId reference
     * @throws ServiceException if the reference format is invalid
     */
    public String extractTemporalKey(String value) throws ServiceException {
        if (!value.startsWith(TEMPORAL_ID_PREFIX)) {
            throw new ServiceException(ErrorCodeTwins.INVALID_TEMPORAL_REFERENCE,
                "Invalid temporal reference format: " + value);
        }
        String key = value.substring(TEMPORAL_ID_PREFIX.length());
        if (key.isEmpty()) {
            throw new ServiceException(ErrorCodeTwins.INVALID_TEMPORAL_REFERENCE,
                "Temporal reference key cannot be empty");
        }
        // Validate key contains only valid characters (alphanumeric, hyphen, underscore)
        if (!key.matches("^[a-zA-Z0-9_-]+$")) {
            throw new ServiceException(ErrorCodeTwins.INVALID_TEMPORAL_REFERENCE,
                "Temporal reference key contains invalid characters: " + key);
        }
        return key;
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
     * Validates temporalId uniqueness in the list
     */
    public void validateTemporalIdUniqueness(List<TwinCreate> twinCreates) throws ServiceException {
        Set<String> seen = new HashSet<>();
        for (TwinCreate twinCreate : twinCreates) {
            if (twinCreate.getTemporalId() != null) {
                if (!seen.add(twinCreate.getTemporalId())) {
                    throw new ServiceException(ErrorCodeTwins.DUPLICATE_TEMPORAL_ID,
                        "Duplicate temporalId: " + twinCreate.getTemporalId());
                }
            }
        }
    }

    /**
     * Detects cyclic dependencies in temporalId references
     * Checks headTwinRef, fieldRefs, and linksRefList for cycles
     */
    public void detectCycles(List<TwinCreate> twinCreates) throws ServiceException {
        // Build multigraph: temporalId -> Set of target temporalIds
        Map<String, Set<String>> graph = new HashMap<>();

        for (TwinCreate tc : twinCreates) {
            if (tc.getTemporalId() == null) continue;

            Set<String> targets = graph.computeIfAbsent(tc.getTemporalId(), k -> new HashSet<>());

            // Add headTwinRef if present
            if (tc.getHeadTwinRef() != null && isTemporalReference(tc.getHeadTwinRef())) {
                targets.add(extractTemporalKey(tc.getHeadTwinRef()));
            }

            // Add fieldRefs
            if (tc.getFieldRefs() != null) {
                for (String fieldRef : tc.getFieldRefs().values()) {
                    if (fieldRef != null && isTemporalReference(fieldRef)) {
                        targets.add(extractTemporalKey(fieldRef));
                    }
                }
            }

            // Add linksRefList
            if (tc.getLinksRefList() != null) {
                for (TwinCreate.LinkRef linkRef : tc.getLinksRefList()) {
                    if (linkRef != null && linkRef.getDstTwinIdRef() != null && isTemporalReference(linkRef.getDstTwinIdRef())) {
                        targets.add(extractTemporalKey(linkRef.getDstTwinIdRef()));
                    }
                }
            }
        }

        // Detect cycles using DFS
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();

        for (String node : graph.keySet()) {
            if (hasCycleInMultigraph(node, graph, visited, recursionStack)) {
                throw new ServiceException(ErrorCodeTwins.CYCLIC_DEPENDENCY,
                    "Cyclic dependency detected in temporal references starting from: " + node);
            }
        }
    }

    private boolean hasCycleInMultigraph(String node, Map<String, Set<String>> graph,
                                          Set<String> visited, Set<String> recursionStack) {
        if (recursionStack.contains(node)) return true;
        if (visited.contains(node)) return false;

        visited.add(node);
        recursionStack.add(node);

        Set<String> neighbors = graph.get(node);
        if (neighbors != null) {
            for (String neighbor : neighbors) {
                if (hasCycleInMultigraph(neighbor, graph, visited, recursionStack)) {
                    return true;
                }
            }
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

    /**
     * Extracts field references that contain temporalId: values from input fields map
     * @param fields the input fields map from DTO
     * @return map of fieldKey -> temporalId reference (only for fields with temporalId:)
     */
    public Map<String, String> extractFieldRefsFromMap(Map<String, String> fields) {
        Map<String, String> fieldRefs = new HashMap<>();
        if (fields != null) {
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                if (entry.getValue() != null && isTemporalReference(entry.getValue())) {
                    fieldRefs.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return fieldRefs.isEmpty() ? null : fieldRefs;
    }

    /**
     * Validates that all temporalId references point to existing temporalIds
     */
    public void validateTemporalIdReferencesExist(List<TwinCreate> twinCreates) throws ServiceException {
        // Collect all existing temporalIds
        Set<String> existingTemporalIds = new HashSet<>();
        for (TwinCreate tc : twinCreates) {
            if (tc.getTemporalId() != null) {
                existingTemporalIds.add(tc.getTemporalId());
            }
        }

        // Check all references point to existing temporalIds
        for (TwinCreate tc : twinCreates) {
            if (tc.getTemporalId() == null) continue;

            // Check headTwinRef
            if (tc.getHeadTwinRef() != null && isTemporalReference(tc.getHeadTwinRef())) {
                String target = extractTemporalKey(tc.getHeadTwinRef());
                if (!existingTemporalIds.contains(target)) {
                    throw new ServiceException(ErrorCodeTwins.TEMPORAL_ID_NOT_FOUND,
                        "Temporal reference '" + target + "' not found (referenced from " + tc.getTemporalId() + ".headTwinId)");
                }
            }

            // Check fieldRefs
            if (tc.getFieldRefs() != null) {
                for (Map.Entry<String, String> entry : tc.getFieldRefs().entrySet()) {
                    if (entry.getValue() != null && isTemporalReference(entry.getValue())) {
                        String target = extractTemporalKey(entry.getValue());
                        if (!existingTemporalIds.contains(target)) {
                            throw new ServiceException(ErrorCodeTwins.TEMPORAL_ID_NOT_FOUND,
                                "Temporal reference '" + target + "' not found (referenced from " + tc.getTemporalId() + ".field." + entry.getKey() + ")");
                        }
                    }
                }
            }

            // Check linksRefList
            if (tc.getLinksRefList() != null) {
                for (TwinCreate.LinkRef linkRef : tc.getLinksRefList()) {
                    if (linkRef != null && linkRef.getDstTwinIdRef() != null && isTemporalReference(linkRef.getDstTwinIdRef())) {
                        String target = extractTemporalKey(linkRef.getDstTwinIdRef());
                        if (!existingTemporalIds.contains(target)) {
                            throw new ServiceException(ErrorCodeTwins.TEMPORAL_ID_NOT_FOUND,
                                "Temporal reference '" + target + "' not found (referenced from " + tc.getTemporalId() + ".link)");
                        }
                    }
                }
            }
        }
    }
}
