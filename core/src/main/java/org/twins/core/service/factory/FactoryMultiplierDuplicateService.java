package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.domain.factory.FactoryMultiplierDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryMultiplierDuplicateService extends EntityDuplicateService<FactoryMultiplierDuplicate, TwinFactoryMultiplierEntity> {

    @Lazy
    private final FactoryMultiplierService factoryMultiplierService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryMultiplierEntity> entityService() {
        return factoryMultiplierService;
    }

    @Override
    protected ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.FACTORY_KEY_ALREADY_IN_USE;
    }

    @Override
    protected void validateKeyUniqueness(Collection<FactoryMultiplierDuplicate> duplicates) throws ServiceException {
        // multipliers have no key concept
    }

    @Override
    protected void prepareDuplicates(Collection<FactoryMultiplierDuplicate> duplicates) throws ServiceException {
        for (var duplicate : duplicates) {
            if (duplicate.getNewTwinFactoryId() == null) {
                duplicate.setNewTwinFactoryId(duplicate.getOriginalEntity().getTwinFactoryId());
            }
        }
    }

    @Override
    protected TwinFactoryMultiplierEntity createNewEntity(FactoryMultiplierDuplicate duplicate) throws ServiceException {
        var src = duplicate.getOriginalEntity();
        return new TwinFactoryMultiplierEntity()
                .setTwinFactoryId(duplicate.getNewTwinFactoryId())
                .setInputTwinClassId(src.getInputTwinClassId())
                .setMultiplierFeaturerId(src.getMultiplierFeaturerId())
                .setMultiplierParams(src.getMultiplierParams())
                .setDescription(src.getDescription())
                .setActive(src.getActive());
    }

    @Override
    protected void duplicateI18nFields(TwinFactoryMultiplierEntity src, TwinFactoryMultiplierEntity dst) throws ServiceException {
        // no i18n fields
    }

    public void duplicateForFactory(TwinFactoryEntity fromFactory, TwinFactoryEntity toFactory) throws ServiceException {
        List<TwinFactoryMultiplierEntity> multipliers = fromFactory.getTwinFactoryMultiplierKit().getList();
        if (multipliers == null || multipliers.isEmpty()) {
            return;
        }
        var entitiesForSave = new ArrayList<TwinFactoryMultiplierEntity>();
        for (TwinFactoryMultiplierEntity originalMultiplier : multipliers) {
            TwinFactoryMultiplierEntity duplicateMultiplier = new TwinFactoryMultiplierEntity()
                    .setTwinFactoryId(toFactory.getId())
                    .setInputTwinClassId(originalMultiplier.getInputTwinClassId())
                    .setMultiplierFeaturerId(originalMultiplier.getMultiplierFeaturerId())
                    .setMultiplierParams(originalMultiplier.getMultiplierParams())
                    .setDescription(originalMultiplier.getDescription())
                    .setActive(originalMultiplier.getActive());
            entitiesForSave.add(duplicateMultiplier);
        }
        factoryMultiplierService.saveSafe(entitiesForSave);
    }
}
