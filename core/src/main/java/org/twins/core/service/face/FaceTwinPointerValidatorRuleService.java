package org.twins.core.service.face;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.face.FaceTwinPointerValidatorRuleEntity;
import org.twins.core.dao.face.FaceTwinPointerValidatorRuleRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTwinPointerValidatorRuleService extends EntitySecureFindServiceImpl<FaceTwinPointerValidatorRuleEntity> {
    private final FaceTwinPointerValidatorRuleRepository faceTwinPointerValidatorRuleRepository;
    private final FaceTwinPointerService faceTwinPointerService;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceTwinPointerValidatorRuleEntity, UUID> entityRepository() {
        return faceTwinPointerValidatorRuleRepository;
    }

    @Override
    public Function<FaceTwinPointerValidatorRuleEntity, UUID> entityGetIdFunction() {
        return FaceTwinPointerValidatorRuleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTwinPointerValidatorRuleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) {
        return false;
    }

    @Override
    public boolean validateEntity(FaceTwinPointerValidatorRuleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) {
        if (entity.getId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty id");
        if (entity.getFaceTwinPointer() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty faceTwinPointer");
        if (entity.getTwinValidatorSetId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty twinValidatorSetId");
        return true;
    }

    public boolean isValid(UUID faceTwinPointerValidatorRuleId) throws ServiceException {
        RequestFacePointers requestFacePointers = faceService.getRequestFacePointers();
        if (requestFacePointers.isAlreadyValidated(faceTwinPointerValidatorRuleId)) {
            return requestFacePointers.getValidationResult(faceTwinPointerValidatorRuleId);
        }
        FaceTwinPointerValidatorRuleEntity faceTwinPointerValidatorRuleEntity = faceTwinPointerValidatorRuleRepository.findById(faceTwinPointerValidatorRuleId)
                .orElseThrow(() -> new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION));

        return false;
    }
}
