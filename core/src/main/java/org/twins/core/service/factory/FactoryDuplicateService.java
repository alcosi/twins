package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.domain.factory.FactoryDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryDuplicateService extends EntityDuplicateService<FactoryDuplicate, TwinFactoryEntity, Void> {
    @Lazy
    private final TwinFactoryService twinFactoryService;
    @Lazy
    private final FactoryBranchDuplicateService factoryBranchDuplicateService;
    @Lazy
    private final FactoryMultiplierDuplicateService factoryMultiplierDuplicateService;
    @Lazy
    private final FactoryPipelineDuplicateService factoryPipelineDuplicateService;
    @Lazy
    private final FactoryEraserDuplicateService factoryEraserDuplicateService;
    @Lazy
    private final FactoryTriggerDuplicateService factoryTriggerDuplicateService;
    private final I18nService i18nService;
    @Lazy
    private final AuthService authService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryEntity> entityService() {
        return twinFactoryService;
    }

    @Override
    protected FactoryDuplicate createNewDuplicate() {
        return new FactoryDuplicate();
    }

    @Override
    protected Consumer<Collection<Void>> inParentLoader() {
        return voids -> {};  // top-level entity — no parent, never invoked
    }

    @Override
    protected ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.FACTORY_KEY_ALREADY_IN_USE;
    }


    @Override
    protected TwinFactoryEntity createNewEntity(FactoryDuplicate duplicate) throws ServiceException {
        var original = duplicate.getOriginalEntity();
        var apiUser = authService.getApiUser();
        log.info("{} will be duplicated with new key[{}]", original.logShort(), duplicate.getNewKey());
        return new TwinFactoryEntity()
                .setKey(duplicate.getNewKey())
                .setCreatedByUserId(apiUser.getUser().getId())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setDomainId(original.getDomainId());
    }

    @Override
    protected void duplicateI18nFields(TwinFactoryEntity src, TwinFactoryEntity dst) throws ServiceException {
        if (src.getNameI18NId() != null) {
            I18nEntity i18nDuplicate = i18nService.duplicateI18n(src.getNameI18NId());
            dst.setNameI18NId(i18nDuplicate.getId());
        }
        if (src.getDescriptionI18NId() != null) {
            I18nEntity i18nDuplicate = i18nService.duplicateI18n(src.getDescriptionI18NId());
            dst.setDescriptionI18NId(i18nDuplicate.getId());
        }
    }

    @Override
    protected void afterSave(Collection<FactoryDuplicate> duplicates, Collection<TwinFactoryEntity> saved) throws ServiceException {
        Map<TwinFactoryEntity, TwinFactoryEntity> branchesMap = null;
        Map<TwinFactoryEntity, TwinFactoryEntity> multipliersMap = null;
        Map<TwinFactoryEntity, TwinFactoryEntity> pipelinesMap = null;
        Map<TwinFactoryEntity, TwinFactoryEntity> erasersMap = null;
        Map<TwinFactoryEntity, TwinFactoryEntity> triggersMap = null;
        for (var duplicate : duplicates) {
            TwinFactoryEntity src = duplicate.getOriginalEntity();
            TwinFactoryEntity dst = duplicate.getNewEntity();
            if (duplicate.isDuplicateBranches()) {
                if (branchesMap == null) branchesMap = new HashMap<>();
                branchesMap.put(src, dst);
            }
            if (duplicate.isDuplicateMultipliers()) {
                if (multipliersMap == null) multipliersMap = new HashMap<>();
                multipliersMap.put(src, dst);
            }
            if (duplicate.isDuplicatePipelines()) {
                if (pipelinesMap == null) pipelinesMap = new HashMap<>();
                pipelinesMap.put(src, dst);
            }
            if (duplicate.isDuplicateErasers()) {
                if (erasersMap == null) erasersMap = new HashMap<>();
                erasersMap.put(src, dst);
            }
            if (duplicate.isDuplicateTriggers()) {
                if (triggersMap == null) triggersMap = new HashMap<>();
                triggersMap.put(src, dst);
            }
        }
        if (branchesMap != null) {
            factoryBranchDuplicateService.duplicateFor(branchesMap, TwinFactoryEntity::getTwinFactoryBranchKit, TwinFactoryEntity::getId);
        }
        if (multipliersMap != null) {
            factoryMultiplierDuplicateService.duplicateFor(multipliersMap, TwinFactoryEntity::getTwinFactoryMultiplierKit, TwinFactoryEntity::getId);
        }
        if (pipelinesMap != null) {
            factoryPipelineDuplicateService.duplicateFor(pipelinesMap, TwinFactoryEntity::getTwinFactoryPipelineKit, TwinFactoryEntity::getId);
        }
        if (erasersMap != null) {
            factoryEraserDuplicateService.duplicateFor(erasersMap, TwinFactoryEntity::getTwinFactoryEraserKit, TwinFactoryEntity::getId);
        }
        if (triggersMap != null) {
            factoryTriggerDuplicateService.duplicateFor(triggersMap, TwinFactoryEntity::getTwinFactoryTriggerKit, TwinFactoryEntity::getId);
        }
    }

    @Override
    protected void setNewParentEntityId(TwinFactoryEntity newEntity, UUID duplicateParentEntityId) {
        //no parent
    }
}
