package org.twins.core.service.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.validator.ContainsTwinValidatorSet;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorRepository;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class TwinValidatorService extends EntitySecureFindServiceImpl<TwinValidatorEntity> {
    
    private final TwinValidatorRepository twinValidatorRepository;

    @Override
    public CrudRepository<TwinValidatorEntity, UUID> entityRepository() {
        return twinValidatorRepository;
    }

    @Override
    public Function<TwinValidatorEntity, UUID> entityGetIdFunction() {
        return TwinValidatorEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinValidatorEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinValidatorEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public <T extends ContainsTwinValidatorSet> void initializeValidatorCollections(Collection<T> entities) throws ServiceException {
        if (entities == null || entities.isEmpty()) {
            return;
        }

        Set<UUID> validatorSetIds = entities.stream()
                .map(ContainsTwinValidatorSet::getTwinValidatorSetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (validatorSetIds.isEmpty()) {
            return;
        }

        Map<UUID, List<TwinValidatorEntity>> validatorsBySetId = twinValidatorRepository
                .findByTwinValidatorSetIdIn(validatorSetIds)
                .stream()
                .collect(Collectors.groupingBy(TwinValidatorEntity::getTwinValidatorSetId));

        for (T entity : entities) {
            if (entity.getTwinValidatorSetId() != null) {
                List<TwinValidatorEntity> validators = validatorsBySetId.get(entity.getTwinValidatorSetId());
                if (validators != null) {
                    try {
                        entity.setTwinValidators(new HashSet<>(validators));
                    } catch (Exception e) {
                        throw new ServiceException(ErrorCodeTwins.TWIN_VALIDATOR_ERROR, "Failed to set collection twinValidators for entity " + entity.getClass().getSimpleName());
                    }
                }
            }
        }
    }
}
