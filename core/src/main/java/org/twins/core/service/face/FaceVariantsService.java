package org.twins.core.service.face;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FaceVariantEntity;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.*;
import java.util.function.ToIntFunction;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
public abstract class FaceVariantsService<T extends FaceVariantEntity> extends EntitySecureFindServiceImpl<T> {
    @Autowired
    private FaceTwinPointerValidatorRuleService faceTwinPointerValidatorRuleService;

    @Autowired
    private FaceService faceService;

    public T findSingleVariant(UUID faceId) throws ServiceException {
        FaceEntity face = faceService.findEntitySafe(faceId);
        List<T> variants = getVariants(faceId);
        if (variants.size() == 1 && variants.getFirst().getTwinPointerValidatorRuleId() == null) {
            return variants.getFirst();
        }
        T ret = null;
        for (var variant : variants) {
            if (variant.getTwinPointerValidatorRuleId() == null || faceTwinPointerValidatorRuleService.isValid(variant.getTwinPointerValidatorRuleId())) {
                if (ret == null) {
                    ret = variant;
                } else {
                    throw new ServiceException(ErrorCodeTwins.FACE_CONFIG_IS_NOT_UNIQ, "to many suitable configs variant were found for " + face.logNormal());
                }
            }
        }
        if (ret == null) {
            throw new ServiceException(ErrorCodeTwins.FACE_NO_CONFIG_IS_SUITABLE, "not suitable config variant was found for " + face.logNormal());
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

    public List<T> filterVariants(Kit<T, UUID> variants, ToIntFunction<? super T> getOrderFunction) throws ServiceException {
        return filterVariants(variants.getCollection()).stream()
                .sorted(Comparator.comparingInt(getOrderFunction))
                .toList();
    }

    public List<T> filterVariants(Collection<T> variants) throws ServiceException {
        List<T> filteredVariants = new ArrayList<>();
        for (T variant : variants) {
            if (variant.getTwinPointerValidatorRuleId() == null ||
                    faceTwinPointerValidatorRuleService.isValid(variant.getTwinPointerValidatorRuleId())) {
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
