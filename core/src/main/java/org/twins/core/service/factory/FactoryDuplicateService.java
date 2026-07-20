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
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryDuplicateService extends EntityDuplicateService<FactoryDuplicate, TwinFactoryEntity, Void> {
    @Lazy
    private final FactoryService factoryService;
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
        return factoryService;
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

    /**
     * A factory reached through {@code duplicateNextFactoryCascade} / {@code duplicateAfterCommitFactory}
     * is a cascaded duplicate (built via {@code lookupOrCollect}, not user-supplied). It is cloned
     * fully and keeps cascading, so the whole reachable factory graph is duplicated as deep as possible.
     */
    @Override
    protected void customizeCollectedDuplicate(FactoryDuplicate duplicate, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        duplicate
                .setDuplicateBranches(true)
                .setDuplicateMultipliers(true)
                .setDuplicatePipelines(true)
                .setDuplicateErasers(true)
                .setDuplicateTriggers(true)
                .setDuplicateConditionSets(true)
                .setDuplicateNextFactoryCascade(true)
                .setDuplicateAfterCommitFactory(true);
    }

    @Override
    protected TwinFactoryEntity createNewEntity(FactoryDuplicate duplicate, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        var original = duplicate.getOriginalEntity();
        var apiUser = authService.getApiUser();
        String newKey = resolveNewKey(duplicate);
        log.info("{} will be duplicated with new key[{}]", original.logShort(), newKey);
        return new TwinFactoryEntity()
                .setId(UuidUtils.generate())
                .setKey(newKey)
                .setDomainId(original.getDomainId())
                .setFactoryProcessorFeaturerId(original.getFactoryProcessorFeaturerId())
                .setFactoryProcessorParams(original.getFactoryProcessorParams() != null
                        ? new HashMap<>(original.getFactoryProcessorParams())
                        : null)
                .setCreatedByUserId(apiUser.getUser().getId())
                .setCreatedAt(Timestamp.from(Instant.now()));
    }

    /**
     * Resolves the key for the new factory. Uses the caller-supplied key as-is; for cascaded
     * factories (no key supplied) generates {@code <originalKey>_copy_<uuid>}. A full UUID suffix
     * gives 122 bits of entropy, so a key collision is astronomically unlikely — no pre-check (and
     * thus no check-then-insert race / extra round-trips) is needed. The DB unique index
     * {@code twin_factory_domain_id_key_uindex} remains the final integrity guard. The generated key
     * is cached back on the duplicate so logs and {@code validateKeyUniqueness} see it.
     */
    private String resolveNewKey(FactoryDuplicate duplicate) {
        String suppliedKey = duplicate.getNewKey();
        if (suppliedKey != null && !suppliedKey.isBlank()) {
            return suppliedKey;
        }
        var original = duplicate.getOriginalEntity();
        String base = (original.getKey() != null && !original.getKey().isBlank()) ? original.getKey() : "factory";
        String candidate = base + "_copy_" + UUID.randomUUID();
        duplicate.setNewKey(candidate);
        return candidate;
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
