package org.twins.core.service.twin;

import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequestScope
public class TemporalIdContext {
    /**
     * Registry of the batch twins: temporalId -> the entity the current request is about to persist.
     * {@link #put(String, UUID)} creates the entity (id set, the rest filled later);
     * TwinCreateRqRestDTOReverseMapper.map() populates THAT SAME instance — so a reference resolved at any
     * point (before or during mapping) carries the right id, and from its map() on — the real entity.
     * Creation order is handled by TwinService.extractDependencies.
     */
    private final Map<String, TwinEntity> temporalTwinMap = new HashMap<>();

    public void put(String temporalId, UUID uuid) {
        temporalTwinMap.put(temporalId, new TwinEntity().setId(uuid));
    }

    /** The batch twin registered under its temporalId (id-only until map() populates it). */
    public TwinEntity resolveTwinByTemporalId(String temporalId) {
        return temporalId == null ? null : temporalTwinMap.get(temporalId);
    }

    public UUID resolve(String temporalId) {
        if (temporalId == null)
            return null;
        TwinEntity twin = temporalTwinMap.get(temporalId);
        return twin == null ? null : twin.getId();
    }

    /**
     * The batch twin by its generated id — the entity the request will persist (id-only until its dto
     * is mapped). Null if the id is not from this batch.
     */
    public TwinEntity resolveTwin(UUID twinId) {
        if (twinId == null)
            return null;
        for (TwinEntity twin : temporalTwinMap.values())
            if (twinId.equals(twin.getId()))
                return twin;
        return null;
    }

    /** Batch twins keyed by their generated id (defensive copy — mutating the result does not affect the context). */
    public Map<UUID, TwinEntity> getBatchTwinsById() {
        Map<UUID, TwinEntity> byId = new HashMap<>();
        for (TwinEntity twin : temporalTwinMap.values())
            byId.putIfAbsent(twin.getId(), twin);
        return byId;
    }

    /** temporalId -> generated twin id, as echoed back to the client in the create response. */
    public Map<String, UUID> getTemporalIdMap() {
        Map<String, UUID> ids = new HashMap<>();
        temporalTwinMap.forEach((temporalId, twin) -> ids.put(temporalId, twin.getId()));
        return ids;
    }

    public void clear() {
        temporalTwinMap.clear();
    }

    public boolean contains(String temporalId) {
        return temporalTwinMap.containsKey(temporalId);
    }

    public static final String TEMPORAL_ID_PREFIX = "temporalId:";

    /**
     * Checks if a value is a temporalId reference
     */
    public static boolean isTemporalReference(String value) {
        return value != null && value.startsWith(TEMPORAL_ID_PREFIX);
    }

    /**
     * Extracts the key from a temporalId reference
     *
     * @throws ServiceException if the reference format is invalid
     */
    public static String extractTemporalKey(String value) throws ServiceException {
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
}
