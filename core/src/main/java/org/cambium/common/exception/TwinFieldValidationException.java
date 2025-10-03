package org.cambium.common.exception;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
public class TwinFieldValidationException extends ServiceException {
    private Map<UUID, String> invalidFieldIds;

    public TwinFieldValidationException(ErrorCode serviceError, Map<UUID, String> invalidFieldIds) {
        super(serviceError);
        this.invalidFieldIds = invalidFieldIds;
    }


}
