package org.twins.core.service.face;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@RequestScope
@RequiredArgsConstructor
public class RequestFacePointers {
    @Getter
    private UUID currentTwinId;

    public void setCurrentTwinId(UUID currentTwinId) throws ServiceException {
        if (this.currentTwinId != null) {
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "twin id already set");
        }
        this.currentTwinId = currentTwinId;
    }

    public boolean isAlreadyValidated(UUID faceTwinPointerValidatorRuleId) {
        return false;
    }

    public boolean getValidationResult(UUID faceTwinPointerValidatorRuleId) {
        return false;
    }
}
