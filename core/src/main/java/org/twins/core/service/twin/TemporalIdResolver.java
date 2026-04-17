package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.springframework.stereotype.Service;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.dto.rest.twin.TwinCreateRqDTOv2;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Collection;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemporalIdResolver {

    public static final String TEMPORAL_ID_PREFIX = "temporalId:";

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
     * Validates temporalId uniqueness in DTO list
     */
    public void validateTemporalIdUniquenessDTO(Collection<TwinCreateRqDTOv2> dtoList) throws ServiceException {
        Set<String> seen = new HashSet<>();
        for (TwinCreateRqDTOv2 dto : dtoList) {
            if (dto.getTemporalId() != null) {
                if (!seen.add(dto.getTemporalId())) {
                    throw new ServiceException(ErrorCodeTwins.DUPLICATE_TEMPORAL_ID,
                        "Duplicate temporalId: " + dto.getTemporalId());
                }
            }
        }
    }

    /**
     * Generates UUID for all temporalId references in the list.
     * @return map of temporalId -> generated UUID
     */
    public Map<String, UUID> generateTemporalIds(List<TwinCreate> twinCreates) {
        Map<String, UUID> temporalIdMap = new HashMap<>();
        for (TwinCreate tc : twinCreates) {
            if (tc.getTemporalId() != null) {
                UUID uuid = UuidUtils.generate();
                temporalIdMap.put(tc.getTemporalId(), uuid);
                tc.getTwinEntity().setId(uuid); // Pre-set ID in entity
            }
        }
        return temporalIdMap;
    }

    /**
     * Detects cyclic dependencies and returns sorted indices by headTwinId dependencies.
     * Uses Kahn's algorithm for topological sort considering only headTwinRef dependencies.
     * @return list of indices in order they should be created
     * @throws ServiceException if cyclic dependency detected in headTwinId references
     */
    public List<Integer> detectCycles(List<TwinCreate> twinCreates) throws ServiceException {
        int n = twinCreates.size();

        // Build temporalId -> index mapping
        Map<String, Integer> temporalIdToIndex = new HashMap<>();
        for (int i = 0; i < n; i++) {
            if (twinCreates.get(i).getTemporalId() != null) {
                temporalIdToIndex.put(twinCreates.get(i).getTemporalId(), i);
            }
        }

        // Build graph: index -> Set of dependencies (indices that this node depends on)
        Map<Integer, Set<Integer>> graph = new HashMap<>();
        int[] inDegree = new int[n];

        for (int i = 0; i < n; i++) {
            TwinCreate tc = twinCreates.get(i);
            Set<Integer> dependencies = new HashSet<>();

            // Check headTwinRef dependency (only headTwinRef matters for creation order)
            if (tc.getHeadTwinRef() != null && isTemporalReference(tc.getHeadTwinRef())) {
                String targetKey = extractTemporalKey(tc.getHeadTwinRef());
                Integer targetIndex = temporalIdToIndex.get(targetKey);
                if (targetIndex != null) {
                    dependencies.add(targetIndex);
                }
            }

            graph.put(i, dependencies);
            inDegree[i] = dependencies.size();
        }

        // Kahn's algorithm for topological sort
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.add(i);
            }
        }

        List<Integer> sortedIndices = new ArrayList<>();
        while (!queue.isEmpty()) {
            int current = queue.poll();
            sortedIndices.add(current);

            // Reduce in-degree for all nodes that depend on current
            for (int i = 0; i < n; i++) {
                if (graph.get(i).contains(current)) {
                    inDegree[i]--;
                    if (inDegree[i] == 0) {
                        queue.add(i);
                    }
                }
            }
        }

        // Check for cycle - if not all nodes processed, there's a cycle
        if (sortedIndices.size() != n) {
            throw new ServiceException(ErrorCodeTwins.CYCLIC_DEPENDENCY,
                "Cyclic dependency detected in headTwinId references. Please check the twin hierarchy.");
        }

        return sortedIndices;
    }

    /**
     * Builds temporalId -> generated UUID mapping from already mapped entities.
     * @return map of temporalId -> generated UUID
     */
    public Map<String, UUID> buildTemporalIdMap(List<TwinCreate> twinCreates) {
        Map<String, UUID> temporalIdMap = new HashMap<>();
        for (TwinCreate tc : twinCreates) {
            if (tc.getTemporalId() != null && tc.getTwinEntity().getId() != null) {
                temporalIdMap.put(tc.getTemporalId(), tc.getTwinEntity().getId());
            }
        }
        return temporalIdMap;
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
     * Validates that all temporalId references point to existing temporalIds in DTO list
     */
    public void validateTemporalIdReferencesExistDTO(Collection<TwinCreateRqDTOv2> dtoList) throws ServiceException {
        // Collect all existing temporalIds
        Set<String> existingTemporalIds = new HashSet<>();
        for (TwinCreateRqDTOv2 dto : dtoList) {
            if (dto.getTemporalId() != null) {
                existingTemporalIds.add(dto.getTemporalId());
            }
        }

        // Check all references point to existing temporalIds
        for (TwinCreateRqDTOv2 dto : dtoList) {
            if (dto.getTemporalId() == null) continue;

            // Check headTwinId
            if (dto.getHeadTwinId() != null && isTemporalReference(dto.getHeadTwinId())) {
                String target = extractTemporalKey(dto.getHeadTwinId());
                if (!existingTemporalIds.contains(target)) {
                    throw new ServiceException(ErrorCodeTwins.TEMPORAL_ID_NOT_FOUND,
                        "Temporal reference '" + target + "' not found (referenced from " + dto.getTemporalId() + ".headTwinId)");
                }
            }

            // Check fields
            if (dto.getFields() != null) {
                for (Map.Entry<String, String> entry : dto.getFields().entrySet()) {
                    if (entry.getValue() != null && isTemporalReference(entry.getValue())) {
                        String target = extractTemporalKey(entry.getValue());
                        if (!existingTemporalIds.contains(target)) {
                            throw new ServiceException(ErrorCodeTwins.TEMPORAL_ID_NOT_FOUND,
                                "Temporal reference '" + target + "' not found (referenced from " + dto.getTemporalId() + ".field." + entry.getKey() + ")");
                        }
                    }
                }
            }

            // Check links
            if (dto.getLinks() != null) {
                for (var link : dto.getLinks()) {
                    if (link.getDstTwinId() != null && isTemporalReference(link.getDstTwinId())) {
                        String target = extractTemporalKey(link.getDstTwinId());
                        if (!existingTemporalIds.contains(target)) {
                            throw new ServiceException(ErrorCodeTwins.TEMPORAL_ID_NOT_FOUND,
                                "Temporal reference '" + target + "' not found (referenced from " + dto.getTemporalId() + ".link)");
                        }
                    }
                }
            }
        }
    }
}
