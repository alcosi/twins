package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.domain.factory.FactoryEraserDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryEraserDuplicateService extends EntityDuplicateService<FactoryEraserDuplicate, TwinFactoryEraserEntity> {

    @Lazy
    private final FactoryEraserService factoryEraserService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryEraserEntity> entityService() {
        return factoryEraserService;
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
    protected void prepareDuplicates(Collection<FactoryEraserDuplicate> duplicates) throws ServiceException {
        for (var duplicate : duplicates) {
            if (duplicate.getNewTwinFactoryId() == null) {
                duplicate.setNewTwinFactoryId(duplicate.getOriginalEntity().getTwinFactoryId());
            }
        }
    }

    @Override
    protected TwinFactoryEraserEntity createNewEntity(FactoryEraserDuplicate duplicate) throws ServiceException {
        var src = duplicate.getOriginalEntity();
        return new TwinFactoryEraserEntity()
                .setTwinFactoryId(duplicate.getNewTwinFactoryId())
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

    public void duplicateForFactory(TwinFactoryEntity fromFactory, TwinFactoryEntity toFactory) throws ServiceException {
        List<TwinFactoryEraserEntity> erasers = fromFactory.getTwinFactoryEraserKit().getList();
        if (erasers == null || erasers.isEmpty()) {
            return;
        }
        var entitiesForSave = new ArrayList<TwinFactoryEraserEntity>();
        for (TwinFactoryEraserEntity originalEraser : erasers) {
            TwinFactoryEraserEntity duplicateEraser = new TwinFactoryEraserEntity()
                    .setTwinFactoryId(toFactory.getId())
                    .setInputTwinClassId(originalEraser.getInputTwinClassId())
                    .setTwinFactoryConditionSetId(originalEraser.getTwinFactoryConditionSetId())
                    .setTwinFactoryConditionInvert(originalEraser.getTwinFactoryConditionInvert())
                    .setEraserAction(originalEraser.getEraserAction())
                    .setDescription(originalEraser.getDescription())
                    .setActive(originalEraser.getActive());
            entitiesForSave.add(duplicateEraser);
        }
        factoryEraserService.saveSafe(entitiesForSave);
    }
}
