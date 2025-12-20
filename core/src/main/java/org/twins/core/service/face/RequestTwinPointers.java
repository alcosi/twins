package org.twins.core.service.face;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.twin.TwinService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequestScope
@RequiredArgsConstructor
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
public class RequestTwinPointers {
    private final TwinService twinService;
    @Getter
    private UUID currentTwinId;
    @Getter
    private TwinEntity currentTwin;
    private final Map<UUID, Boolean> validationResults = new HashMap<>();
    private final Map<UUID, TwinEntity> pointers = new HashMap<>();

    public void setCurrentTwinId(UUID currentTwinId) throws ServiceException {
        if (this.currentTwinId != null) {
            throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION, "twin id already set");
        }
        if (currentTwinId == null) {
            this.currentTwinId = UuidUtils.NULLIFY_MARKER;
        } else {
            this.currentTwinId = currentTwinId;
            this.currentTwin = twinService.findEntitySafe(currentTwinId);
        }
    }

    public boolean isAlreadyValidated(UUID faceTwinPointerValidatorRuleId) {
        return validationResults.containsKey(faceTwinPointerValidatorRuleId);
    }

    public boolean getValidationResult(UUID faceTwinPointerValidatorRuleId) {
        return validationResults.get(faceTwinPointerValidatorRuleId);
    }

    public void setValidationResult(UUID faceTwinPointerValidatorRuleId, boolean result) {
        validationResults.put(faceTwinPointerValidatorRuleId, result);
    }

    public boolean hasPointer(UUID faceTwinPointer) {
        return pointers.containsKey(faceTwinPointer);
    }

    public TwinEntity getPointedTwin(UUID faceTwinPointer) {
        return pointers.get(faceTwinPointer);
    }

    public void addPointer(UUID faceTwinPointerId, TwinEntity targetTwin) {
        pointers.put(faceTwinPointerId, targetTwin);
    }
}
