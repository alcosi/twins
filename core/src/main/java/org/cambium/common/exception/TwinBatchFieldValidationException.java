package org.cambium.common.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class TwinBatchFieldValidationException extends ServiceException {
    private Map<UUID, Map<UUID, String>> invalidFields = new HashMap<>();

    public TwinBatchFieldValidationException(ErrorCode serviceError) {
        super(serviceError);
    }

    public TwinBatchFieldValidationException addInvalidFields(UUID twinId, Map<UUID, String> twinInvalidFields) {
        invalidFields.put(twinId, twinInvalidFields);
        return this;
    }
}
