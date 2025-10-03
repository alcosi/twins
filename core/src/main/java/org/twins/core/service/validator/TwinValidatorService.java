package org.twins.core.service.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
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

    public <T extends ContainsTwinValidatorSet> void loadValidators(T entity) throws ServiceException {
        loadValidators(List.of(entity));
    }

    public <T extends ContainsTwinValidatorSet> void loadValidators(Collection<T> entities) throws ServiceException {
        Kit<T, UUID> needLoad = new Kit<>(T::getTwinValidatorSetId);
        for (T entity : entities) {
            if (entity.getTwinValidatorKit() == null && entity.getTwinValidatorSetId() != null) {
                needLoad.add(entity);
            }
        }

        if (needLoad.isEmpty()) {
            return;
        }

        KitGrouped<TwinValidatorEntity, UUID, UUID> validatorKit = new KitGrouped<>(
                twinValidatorRepository.findByTwinValidatorSetIdIn(needLoad.getIdSet()),
                TwinValidatorEntity::getId,
                TwinValidatorEntity::getTwinValidatorSetId);

        for (Map.Entry<UUID, T> entry : needLoad.getMap().entrySet()) {
            entry.getValue().setTwinValidatorKit(new Kit<>(validatorKit.getGrouped(entry.getKey()), TwinValidatorEntity::getId));
        }
    }
}