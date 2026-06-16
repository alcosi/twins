package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.domain.factory.FactoryMultiplierDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryMultiplierDuplicateService extends EntityDuplicateService<FactoryMultiplierDuplicate, TwinFactoryMultiplierEntity, TwinFactoryEntity> {

    @Lazy
    private final FactoryMultiplierService factoryMultiplierService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryMultiplierEntity> entityService() {
        return factoryMultiplierService;
    }

    @Override
    protected FactoryMultiplierDuplicate createNewDuplicate() {
        return new FactoryMultiplierDuplicate();
    }

    @Override
    protected Consumer<Collection<TwinFactoryEntity>> inParentLoader() {
        return factoryMultiplierService::loadFactoryMultipliers;
    }

    @Override
    protected Function<TwinFactoryEntity, Kit<TwinFactoryMultiplierEntity, UUID>> childExtractor() {
        return TwinFactoryEntity::getTwinFactoryMultiplierKit;
    }

    @Override
    protected Function<TwinFactoryEntity, UUID> destinationParentIdExtractor() {
        return TwinFactoryEntity::getId;
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
    protected TwinFactoryMultiplierEntity createNewEntity(FactoryMultiplierDuplicate duplicate) throws ServiceException {
        var src = duplicate.getOriginalEntity();
        return new TwinFactoryMultiplierEntity()
                .setTwinFactoryId(src.getTwinFactoryId())
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

    @Override
    protected void setNewParentEntityId(TwinFactoryMultiplierEntity newEntity, UUID duplicateParentEntityId) {
        newEntity.setTwinFactoryId(duplicateParentEntityId);
    }
}
