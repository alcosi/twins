package org.twins.core.service.face;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.service.twin.TwinPointerValidatorRuleService;

import java.util.UUID;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class FaceTwinPointerValidatorRuleService {
    private final FaceService faceService;
    private final TwinPointerValidatorRuleService twinPointerValidatorRuleService;

    public boolean isValid(UUID faceTwinPointerValidatorRuleId) throws ServiceException {
        RequestTwinPointers requestFacePointers = faceService.getRequestFacePointers();
        if (requestFacePointers.isAlreadyValidated(faceTwinPointerValidatorRuleId)) {
            return requestFacePointers.getValidationResult(faceTwinPointerValidatorRuleId);
        }
        boolean result = twinPointerValidatorRuleService.isValid(requestFacePointers.getCurrentTwin(), faceTwinPointerValidatorRuleId);
        requestFacePointers.setValidationResult(faceTwinPointerValidatorRuleId, result);
        return result;
    }
}
