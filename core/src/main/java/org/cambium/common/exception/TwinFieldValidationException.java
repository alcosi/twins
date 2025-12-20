package org.cambium.common.exception;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class TwinFieldValidationException extends ServiceException {
    private final UUID twinId;
    private final Map<UUID, String> invalidFields;

    public TwinFieldValidationException(ErrorCode serviceError, UUID twinId, Map<UUID, String> invalidFields) {
        super(serviceError);
        this.twinId = twinId;
        this.invalidFields = invalidFields;
    }
}
