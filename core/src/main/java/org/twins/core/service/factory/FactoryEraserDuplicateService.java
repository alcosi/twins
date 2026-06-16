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
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.domain.factory.FactoryEraserDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryEraserDuplicateService extends EntityDuplicateService<FactoryEraserDuplicate, TwinFactoryEraserEntity, TwinFactoryEntity> {

    @Lazy
    private final FactoryEraserService factoryEraserService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryEraserEntity> entityService() {
        return factoryEraserService;
    }

    @Override
    protected FactoryEraserDuplicate createNewDuplicate() {
        return new FactoryEraserDuplicate();
    }

    @Override
    protected Consumer<Collection<TwinFactoryEntity>> inParentLoader() {
        return factoryEraserService::loadFactoryErasers;
    }

    @Override
    protected Function<TwinFactoryEntity, Kit<TwinFactoryEraserEntity, UUID>> childExtractor() {
        return TwinFactoryEntity::getTwinFactoryEraserKit;
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
    protected void validateKeyUniqueness(Collection<FactoryEraserDuplicate> duplicates) throws ServiceException {
        // erasers have no key concept
    }

    @Override
    protected TwinFactoryEraserEntity createNewEntity(FactoryEraserDuplicate duplicate) throws ServiceException {
        var src = duplicate.getOriginalEntity();
        return new TwinFactoryEraserEntity()
                .setTwinFactoryId(src.getTwinFactoryId())
                .setInputTwinClassId(src.getInputTwinClassId())
                .setTwinFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                .setEraserAction(src.getEraserAction())
                .setDescription(src.getDescription())
                .setActive(src.getActive());
    }

    @Override
    protected void duplicateI18nFields(TwinFactoryEraserEntity src, TwinFactoryEraserEntity dst) throws ServiceException {
        // no i18n fields
    }

    @Override
    protected void setNewParentEntityId(TwinFactoryEraserEntity newEntity, UUID duplicateParentEntityId) {
        newEntity.setTwinFactoryId(duplicateParentEntityId);
    }
}
