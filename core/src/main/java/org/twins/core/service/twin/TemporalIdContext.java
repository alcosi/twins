package org.twins.core.service.twin;

import lombok.Getter;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequestScope
@Getter
public class TemporalIdContext {
    private final Map<String, UUID> temporalIdMap = new HashMap<>();

    public void put(String temporalId, UUID uuid) {
        temporalIdMap.put(temporalId, uuid);
    }

    public UUID resolve(String temporalId) {
        if (temporalId == null)
            return null;
        return temporalIdMap.get(temporalId);
    }

    public UUID resolveOrUseId(String temporalId) throws ServiceException {
        if (temporalId == null)
            return null;
        if (temporalId.startsWith(TemporalIdContext.TEMPORAL_ID_PREFIX)) {
            String key = temporalId.substring(TemporalIdContext.TEMPORAL_ID_PREFIX.length());
            UUID resolvedId = temporalIdMap.get(key);
            if (resolvedId == null) {
                throw new ServiceException(ErrorCodeTwins.TEMPORAL_ID_NOT_FOUND,
                        "Temporal ID reference not found: " + key);
            }
            return resolvedId;
        } else {
            try {
                return UUID.fromString(temporalId);
            } catch (IllegalArgumentException e) {
                throw new ServiceException(ErrorCodeTwins.INVALID_TEMPORAL_REFERENCE,
                        "Invalid uui format: " + temporalId + ". Expected UUID or temporalId:XXX reference.");
            }
        }
    }

    public void clear() {
        temporalIdMap.clear();
    }

    public boolean contains(String temporalId) {
        return temporalIdMap.containsKey(temporalId);
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
