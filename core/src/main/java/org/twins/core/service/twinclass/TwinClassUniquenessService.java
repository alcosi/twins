package org.twins.core.service.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassUniquenessEntity;
import org.twins.core.dao.twinclass.TwinClassUniquenessFieldRepository;
import org.twins.core.dao.twinclass.TwinClassUniquenessRepository;
import org.twins.core.service.TwinsEntitySecureFindService;

import java.util.Collection;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class TwinClassUniquenessService extends TwinsEntitySecureFindService<TwinClassUniquenessEntity> {

    private final TwinClassUniquenessRepository twinClassUniquenessRepository;
    private final TwinClassUniquenessFieldRepository fieldRepository;
    private final TwinClassUniquenessConfigService configService;

    @Override
    public CrudRepository<TwinClassUniquenessEntity, UUID> entityRepository() {
        return twinClassUniquenessRepository;
    }

    @Override
    public Function<TwinClassUniquenessEntity, UUID> entityGetIdFunction() {
        return TwinClassUniquenessEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassUniquenessEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassUniquenessEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadUniqueness(TwinClassEntity twinClassEntity) {
        loadUniqueness(Set.of(twinClassEntity));
    }

    public void loadUniqueness(Collection<TwinClassEntity> twinClassEntities) {
        loadKit(
                twinClassEntities,
                TwinClassEntity::getId,
                TwinClassEntity::getUniquenessKit,
                TwinClassEntity::setUniquenessKit,
                twinClassUniquenessRepository::findAllByTwinClassIdIn,
                TwinClassUniquenessEntity::getId,
                TwinClassUniquenessEntity::getTwinClassId
        );

        for (TwinClassEntity twinClass : twinClassEntities) {
            if (twinClass.getUniquenessKit() != null && !twinClass.getUniquenessKit().isEmpty()) {
                configService.loadUniquenessFields(twinClass.getUniquenessKit().getCollection());
            }
        }
    }
}
