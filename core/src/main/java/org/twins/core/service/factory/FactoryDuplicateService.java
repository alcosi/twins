package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.domain.EntityDuplicateCollector;
import org.twins.core.domain.factory.FactoryDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryDuplicateService extends EntityDuplicateService<FactoryDuplicate, TwinFactoryEntity, Void> {
    @Lazy
    private final FactoryExecutionService twinFactoryService;
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
    @Lazy
    private final FactoryConditionSetDuplicateService conditionSetDuplicateService;
    @Lazy
    private final AuthService authService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryEntity> entityService() {
        return twinFactoryService;
    }

    @Override
    protected EntitySecureFindServiceImpl<Void> entityParentService() {
        return null; // top-level entity
    }

    @Override
    protected Class<TwinFactoryEntity> getEntityClass() {
        return TwinFactoryEntity.class;
    }

    @Override
    protected Set<Class<?>> commitAfter() {
        return Set.of(); // top-level
    }

    @Override
    protected FactoryDuplicate createNewDuplicate() {
        return new FactoryDuplicate();
    }

    @Override
    protected void loadFor(Collection<Void> parents) {
        // top-level entity — never invoked
    }

    @Override
    protected Kit<TwinFactoryEntity, UUID> extractorChildren(Void parent) {
        return null; // top-level entity — never invoked
    }

    @Override
    protected UUID extractParentId(Void parent) {
        return null; // top-level entity — never invoked
    }

    @Override
    protected ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.FACTORY_KEY_ALREADY_IN_USE;
    }

    @Override
    protected TwinFactoryEntity createNewEntity(FactoryDuplicate duplicate, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        var original = duplicate.getOriginalEntity();
        var apiUser = authService.getApiUser();
        log.info("{} will be duplicated with new key[{}]", original.logShort(), duplicate.getNewKey());
        return new TwinFactoryEntity()
                .setId(UuidUtils.generate())
                .setKey(duplicate.getNewKey())
                .setCreatedByUserId(apiUser.getUser().getId())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setDomainId(original.getDomainId());
    }

    @Override
    protected List<I18nFieldDuplicate<TwinFactoryEntity>> i18nFields() {
        return List.of(
                I18nFieldDuplicate.of(TwinFactoryEntity::getNameI18NId,        TwinFactoryEntity::setNameI18NId),
                I18nFieldDuplicate.of(TwinFactoryEntity::getDescriptionI18NId, TwinFactoryEntity::setDescriptionI18NId)
        );
    }

    @Override
    protected void setNewParentEntity(TwinFactoryEntity newEntity, Void parentEntity) {
        // no parent
    }

    /**
     * Note: under policy B (scope-aware remapping), condition sets referenced by
     * branches/multipliers/pipelines/erasers/triggers are auto-duplicated via
     * {@code conditionSetDuplicateService.lookupOrCollect} inside each child's {@code createNewEntity}.
     * The explicit {@code duplicateConditionSets} flag still cascades ALL condition sets of the
     * source factory (including unreferenced ones) — useful when the user wants a full clone.
     */
    @Override
    protected List<ChildCascade<FactoryDuplicate, TwinFactoryEntity>> childCascades() {
        return List.of(
                new ChildCascade<>(FactoryDuplicate::isDuplicateConditionSets, conditionSetDuplicateService),
                new ChildCascade<>(FactoryDuplicate::isDuplicateBranches,      factoryBranchDuplicateService),
                new ChildCascade<>(FactoryDuplicate::isDuplicateMultipliers,   factoryMultiplierDuplicateService),
                new ChildCascade<>(FactoryDuplicate::isDuplicatePipelines,     factoryPipelineDuplicateService),
                new ChildCascade<>(FactoryDuplicate::isDuplicateErasers,       factoryEraserDuplicateService),
                new ChildCascade<>(FactoryDuplicate::isDuplicateTriggers,      factoryTriggerDuplicateService)
        );
    }
}
