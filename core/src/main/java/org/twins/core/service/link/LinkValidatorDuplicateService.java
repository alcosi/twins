package org.twins.core.service.link;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkValidatorEntity;
import org.twins.core.domain.EntityDuplicateCollector;
import org.twins.core.domain.link.LinkValidatorDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkValidatorDuplicateService extends EntityDuplicateService<LinkValidatorDuplicate, LinkValidatorEntity, LinkEntity> {

    @Lazy
    private final LinkValidatorService linkValidatorService;
    @Lazy
    private final LinkService linkService;

    @Override
    protected EntitySecureFindServiceImpl<LinkValidatorEntity> entityService() {
        return linkValidatorService;
    }

    @Override
    protected EntitySecureFindServiceImpl<LinkEntity> entityParentService() {
        return linkService;
    }

    @Override
    protected Class<LinkValidatorEntity> getEntityClass() {
        return LinkValidatorEntity.class;
    }

    @Override
    protected Set<Class<?>> commitAfter() {
        return Set.of(LinkEntity.class);
    }

    @Override
    protected LinkValidatorDuplicate createNewDuplicate() {
        return new LinkValidatorDuplicate();
    }

    @Override
    protected void loadFor(Collection<LinkEntity> parents) {
        linkValidatorService.loadLinkValidators(parents);
    }

    /**
     * Enables "duplicate in place": a validator duplicated without an explicit target link
     * ({@code newParentEntityId == null}) defaults to its own link.
     */
    @Override
    protected UUID extractOriginalParentId(LinkValidatorEntity original) {
        return original.getLinkId();
    }

    @Override
    protected Kit<LinkValidatorEntity, UUID> extractorChildren(LinkEntity parent) {
        // loadFor has already run; the kit may still be null on a data-less path — treat as empty
        Kit<LinkValidatorEntity, UUID> validators = parent.getLinkValidators();
        return validators == null ? Kit.emptyKit() : validators;
    }

    @Override
    protected UUID extractParentId(LinkEntity parent) {
        return parent.getId();
    }

    @Override
    protected ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.LINK_VALIDATOR_INCORRECT;
    }

    @Override
    protected void validateKeyUniqueness(Collection<LinkValidatorDuplicate> duplicates) throws ServiceException {
        // validators have no key concept
    }

    @Override
    protected LinkValidatorEntity createNewEntity(LinkValidatorDuplicate duplicate, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        var src = duplicate.getOriginalEntity();
        UUID targetLinkId = duplicate.getNewParentEntityId() != null ? duplicate.getNewParentEntityId() : src.getLinkId();
        return new LinkValidatorEntity()
                .setId(UuidUtils.generate())
                .setLinkId(targetLinkId)
                .setLinkerFeaturerId(src.getLinkerFeaturerId())
                .setLinkerParams(src.getLinkerParams() != null ? new HashMap<>(src.getLinkerParams()) : null)
                // unique (link_id, order): always append after everything known so far — db rows of the
                // target link plus validator clones already reserved in this duplicate operation
                .setOrder(nextFreeOrder(targetLinkId, duplicateCollector));
    }

    @Override
    protected void setNewParentEntity(LinkValidatorEntity newEntity, LinkEntity parentEntity) {
        newEntity
                .setLinkId(parentEntity.getId())
                .setLink(parentEntity);
    }

    @Override
    protected void afterCommit(Collection<LinkValidatorEntity> saved) throws ServiceException {
        // LinkEntity instances are globally cached together with their loaded validator list — drop it
        linkValidatorService.evictLinkValidatorsCache(saved);
    }

    private int nextFreeOrder(UUID linkId, EntityDuplicateCollector duplicateCollector) {
        Map<UUID, Integer> maxOrderCache = duplicateCollector.getMaxOrderCache(LinkValidatorEntity.class);
        int next = maxOrderCache.computeIfAbsent(linkId, this::findDbMaxOrder) + 1;
        maxOrderCache.put(linkId, next); // claim for the next clone of the same target link
        return next;
    }

    /**
     * Scalar db max — the per-operation cache in {@link EntityDuplicateCollector} keeps this to one
     * query per distinct target link instead of one per cloned validator (N+1 on bulk duplicates).
     */
    private int findDbMaxOrder(UUID linkId) {
        List<Object[]> rows = linkValidatorService.getRepository().findMaxOrderByLinkIdIn(List.of(linkId));
        if (rows.isEmpty() || rows.get(0)[1] == null)
            return 0;
        return ((Number) rows.get(0)[1]).intValue();
    }
}
