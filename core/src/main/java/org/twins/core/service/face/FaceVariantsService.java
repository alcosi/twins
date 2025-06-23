package org.twins.core.service.face;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.twins.core.dao.face.FaceVariant;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public abstract class FaceVariantsService<T extends FaceVariant> extends EntitySecureFindServiceImpl<T> {
    @Autowired
    private FaceTwinPointerValidatorRuleService faceTwinPointerValidatorRuleService;

    @Autowired
    private FaceService faceService;

    public T findSingleVariant(UUID faceId) throws ServiceException {
        List<T> variants = getVariants(faceId);
        if (variants.size() == 1 && variants.getFirst().getFaceTwinPointerValidatorRuleId() == null) {
            return variants.getFirst();
        }
        T ret = null;
        for (var variant : variants) {
            if (variant.getFaceTwinPointerValidatorRuleId() == null || faceTwinPointerValidatorRuleService.isValid(variant.getFaceTwinPointerValidatorRuleId())) {
                if (ret == null) {
                    ret = variant;
                } else {
                    throw new ServiceException(ErrorCodeTwins.FACE_CONFIG_IS_NOT_UNIQ);
                }
            }
        }
        if (ret == null) {
            throw new ServiceException(ErrorCodeTwins.FACE_NO_CONFIG_IS_SUITABLE);
        }
        return ret;
    }

    public T findSingleVariant(UUID faceId, UUID currentTwinId) throws ServiceException {
        faceService.getRequestFacePointers().setCurrentTwinId(currentTwinId);
        return findSingleVariant(faceId);
    }

    public List<T> filterVariants(UUID faceId) throws ServiceException {
        return filterVariants(getVariants(faceId));
    }

    public List<T> filterVariants(Kit<T, UUID> variants) throws ServiceException {
        return filterVariants(variants.getCollection());
    }

    public List<T> filterVariants(Collection<T> variants) throws ServiceException {
        List<T> filteredVariants = new ArrayList<>();
        for (T variant : variants) {
            if (variant.getFaceTwinPointerValidatorRuleId() == null ||
                    faceTwinPointerValidatorRuleService.isValid(variant.getFaceTwinPointerValidatorRuleId())) {
                filteredVariants.add(variant);
            }
        }
        return filteredVariants;
    }

    public List<T> filterVariants(UUID faceId, UUID currentTwinId) throws ServiceException {
        faceService.getRequestFacePointers().setCurrentTwinId(currentTwinId);
        return filterVariants(faceId);
    }

    public abstract List<T> getVariants(UUID of);
}
