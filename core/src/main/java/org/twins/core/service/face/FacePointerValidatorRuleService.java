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
import org.twins.core.dao.face.FacePointerValidatorRuleEntity;
import org.twins.core.dao.face.FacePointerValidatorRuleRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.twin.TwinValidatorSetService;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FacePointerValidatorRuleService extends EntitySecureFindServiceImpl<FacePointerValidatorRuleEntity> {
    private final FacePointerValidatorRuleRepository facePointerValidatorRuleRepository;
    private final FacePointerService facePointerService;
    private final FaceService faceService;
    private final TwinValidatorSetService twinValidatorSetService;

    @Override
    public CrudRepository<FacePointerValidatorRuleEntity, UUID> entityRepository() {
        return facePointerValidatorRuleRepository;
    }

    @Override
    public Function<FacePointerValidatorRuleEntity, UUID> entityGetIdFunction() {
        return FacePointerValidatorRuleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FacePointerValidatorRuleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) {
        return facePointerService.isEntityReadDenied(entity.getFacePointer());
    }

    @Override
    public boolean validateEntity(FacePointerValidatorRuleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) {
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
        FacePointerValidatorRuleEntity facePointerValidatorRuleEntity = facePointerValidatorRuleRepository.findById(faceTwinPointerValidatorRuleId)
                .orElseThrow(() -> new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION));
        twinValidatorSetService.loadTwinValidatorSet(facePointerValidatorRuleEntity);
        TwinEntity pointedTwin = facePointerService.getPointer(facePointerValidatorRuleEntity.getFaceTwinPointer());
        boolean result = twinValidatorSetService.isValid(pointedTwin, facePointerValidatorRuleEntity, facePointerValidatorRuleEntity.getTwinValidators());
        requestFacePointers.setValidationResult(faceTwinPointerValidatorRuleId, result);
        return result;
    }
}
