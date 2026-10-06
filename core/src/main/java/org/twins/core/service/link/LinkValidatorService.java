package org.twins.core.service.link;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.KitUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkValidatorEntity;
import org.twins.core.dao.link.LinkValidatorRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.link.LinkValidatorCreate;
import org.twins.core.domain.link.LinkValidatorUpdate;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.linker.Linker;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class LinkValidatorService extends EntitySecureFindServiceImpl<LinkValidatorEntity> {
    @Getter
    private final LinkValidatorRepository repository;
    private final FeaturerService featurerService;
    private final LinkService linkService;
    private final TwinClassService twinClassService;
    @Lazy // TwinService -> TwinLinkService -> this service
    private final TwinService twinService;
    @Lazy
    private final TwinSearchService twinSearchService;

    @Override
    public CrudRepository<LinkValidatorEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<LinkValidatorEntity, UUID> entityGetIdFunction() {
        return LinkValidatorEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(LinkValidatorEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        // no domain_id on link_validator — isolation goes through the parent link
        LinkEntity link = linkService.findEntitySafe(entity.getLinkId());
        return checkDomainAccessDenied(link.getDomainId(), entity.logShort(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(LinkValidatorEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getLinkId() == null)
            return logErrorAndReturnFalse("linkId is required for " + entity.logShort());
        if (entity.getLinkerFeaturerId() == null)
            throw new ServiceException(ErrorCodeCommon.FEATURER_IS_NULL);
        if (entity.getOrder() == null)
            return logErrorAndReturnFalse("order is required for " + entity.logShort());
        return true;
    }

    /**
     * Load pattern (Variant D, one-to-many kit): validators are lazy-loaded into
     * {@link LinkEntity#getLinkValidators()} in one batch query — an empty kit also counts as
     * loaded, so repeated calls never hit the repository again.
     */
    public void loadLinkValidators(LinkEntity linkEntity) {
        loadLinkValidators(Collections.singletonList(linkEntity));
    }

    public void loadLinkValidators(Collection<LinkEntity> linkEntities) {
        loadKit(
                linkEntities,
                LinkEntity::getId,
                LinkEntity::getLinkValidators,
                LinkEntity::setLinkValidators,
                repository::findByLinkIdIn,
                LinkValidatorEntity::getId,
                LinkValidatorEntity::getLinkId,
                LinkValidatorEntity::setLink);
    }

    public void loadLink(LinkValidatorEntity src) throws ServiceException {
        loadLink(Collections.singletonList(src));
    }

    public void loadLink(Collection<LinkValidatorEntity> srcCollection) throws ServiceException {
        linkService.load(srcCollection,
                LinkValidatorEntity::getLinkId,
                LinkValidatorEntity::getLink,
                LinkValidatorEntity::setLink);
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<LinkValidatorEntity> createLinkValidators(List<LinkValidatorCreate> creates) throws ServiceException {
        if (creates == null || creates.isEmpty())
            return Collections.emptyList();
        Map<UUID, List<Integer>> claimedOrders = new HashMap<>(); // linkId -> orders claimed within this batch
        List<LinkValidatorEntity> entitiesToSave = new ArrayList<>(creates.size());
        for (LinkValidatorCreate create : creates) {
            LinkValidatorEntity entity = create.getLinkValidator();
            linkService.findEntitySafe(entity.getLinkId()); // existence + read permission on the parent link
            validateAndPrepareFeaturer(entity.getLinkerFeaturerId(), entity.getLinkerParams(), Linker.class);
            if (entity.getOrder() == null)
                entity.setOrder(nextFreeOrder(entity.getLinkId(), claimedOrders));
            else
                checkOrderFree(entity, claimedOrders);
            entitiesToSave.add(entity);
        }
        List<LinkValidatorEntity> saved = StreamSupport.stream(saveSafe(entitiesToSave).spliterator(), false).toList();
        invalidateParentLinkValidators(saved);
        return saved;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<LinkValidatorEntity> updateLinkValidators(List<LinkValidatorUpdate> updates) throws ServiceException {
        if (updates == null || updates.isEmpty())
            return Collections.emptyList();
        ChangesHelperMulti<LinkValidatorEntity> changes = new ChangesHelperMulti<>();
        Kit<LinkValidatorEntity, UUID> entitiesKit = findEntitiesSafe(updates.stream().map(LinkValidatorUpdate::getId).toList());
        Map<UUID, List<Integer>> claimedOrders = new HashMap<>();
        List<LinkValidatorEntity> allEntities = new ArrayList<>(updates.size());
        for (LinkValidatorUpdate update : updates) {
            LinkValidatorEntity entity = entitiesKit.get(update.getId());
            allEntities.add(entity);
            ChangesHelper changesHelper = new ChangesHelper();
            LinkValidatorEntity sourceEntity = update.getLinkValidator();
            if (sourceEntity.getLinkId() != null && !sourceEntity.getLinkId().equals(entity.getLinkId()))
                linkService.findEntitySafe(sourceEntity.getLinkId()); // existence + read permission on the new parent link
            updateEntityFieldByValueIfNotNull(sourceEntity.getLinkId(), entity,
                    LinkValidatorEntity::getLinkId, LinkValidatorEntity::setLinkId,
                    LinkValidatorEntity.Fields.linkId, changesHelper);
            updateEntityFeaturerField(entity, sourceEntity.getLinkerFeaturerId(), sourceEntity.getLinkerParams(),
                    LinkValidatorEntity::getLinkerFeaturerId, LinkValidatorEntity::setLinkerFeaturerId,
                    LinkValidatorEntity::getLinkerParams, LinkValidatorEntity::setLinkerParams,
                    LinkValidatorEntity.Fields.linkerFeaturerId, LinkValidatorEntity.Fields.linkerParams,
                    Linker.class, changesHelper);
            updateEntityFieldByValueIfNotNull(sourceEntity.getOrder(), entity,
                    LinkValidatorEntity::getOrder, LinkValidatorEntity::setOrder,
                    LinkValidatorEntity.Fields.order, changesHelper);
            if (changesHelper.hasChange(LinkValidatorEntity.Fields.linkId) || changesHelper.hasChange(LinkValidatorEntity.Fields.order))
                checkOrderFree(entity, claimedOrders);
            changes.add(entity, changesHelper);
        }
        updateSafe(changes);
        invalidateParentLinkValidators(allEntities);
        return allEntities;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void deleteLinkValidators(Set<UUID> ids) throws ServiceException {
        if (ids == null || ids.isEmpty())
            return;
        Collection<LinkValidatorEntity> entities = findEntitiesSafe(ids).getCollection();
        if (entities.isEmpty())
            return;
        deleteSafe(ids);
        invalidateParentLinkValidators(entities);
    }

    /**
     * Unique index {@code link_validator(link_id, order)}: the requested order must be free in db
     * and not claimed by a sibling from the same batch.
     */
    private void checkOrderFree(LinkValidatorEntity entity, Map<UUID, List<Integer>> claimedOrders) throws ServiceException {
        List<Integer> claimed = claimedOrders.computeIfAbsent(entity.getLinkId(), k -> new ArrayList<>());
        for (LinkValidatorEntity other : repository.findByLinkIdOrderByOrder(entity.getLinkId())) {
            if (entity.getId() != null && entity.getId().equals(other.getId()))
                continue; // own row — order change is handled by the db update
            if (other.getOrder().equals(entity.getOrder()))
                throw new ServiceException(ErrorCodeTwins.LINK_VALIDATOR_ORDER_CONFLICT,
                        entity.logShort() + " order[" + entity.getOrder() + "] is already taken by " + other.logShort() + " of link[" + entity.getLinkId() + "]");
        }
        if (claimed.contains(entity.getOrder()))
            throw new ServiceException(ErrorCodeTwins.LINK_VALIDATOR_ORDER_CONFLICT,
                    entity.logShort() + " order[" + entity.getOrder() + "] is claimed twice in the batch for link[" + entity.getLinkId() + "]");
        claimed.add(entity.getOrder());
    }

    /**
     * Appends after the highest order of the link (batch-aware via {@code claimedOrders}).
     */
    private int nextFreeOrder(UUID linkId, Map<UUID, List<Integer>> claimedOrders) {
        int max = 0;
        for (LinkValidatorEntity other : repository.findByLinkIdOrderByOrder(linkId))
            max = Math.max(max, other.getOrder());
        List<Integer> claimed = claimedOrders.computeIfAbsent(linkId, k -> new ArrayList<>());
        for (Integer order : claimed)
            max = Math.max(max, order);
        int next = max + 1;
        claimed.add(next);
        return next;
    }

    /**
     * Validators ride on the globally cached {@link LinkEntity} (see {@link #loadLinkValidators}) —
     * any mutation must drop the loaded list so the next read re-fetches.
     */
    private void invalidateParentLinkValidators(Collection<LinkValidatorEntity> entities) throws ServiceException {
        Set<UUID> linkIds = new HashSet<>();
        for (LinkValidatorEntity entity : entities)
            linkIds.add(entity.getLinkId());
        if (linkIds.isEmpty())
            return;
        for (LinkEntity link : linkService.findEntitiesSafe(linkIds).getCollection())
            link.setLinkValidators(null);
    }

    /**
     * Public cache-drop for sibling services (e.g. {@link LinkValidatorDuplicateService#afterCommit}).
     */
    public void evictLinkValidatorsCache(Collection<LinkValidatorEntity> entities) throws ServiceException {
        invalidateParentLinkValidators(entities);
    }

    public PaginationResult<TwinEntity> findValidDstTwins(UUID twinClassId, UUID linkId, UUID headTwinId, BasicSearch basicSearch, SimplePagination pagination) throws ServiceException {
        LinkEntity linkEntity = linkService.findEntitySafe(linkId);
        TwinClassEntity srcTwinClassEntity = twinClassService.findEntitySafe(twinClassId);
        TwinEntity headTwinEntity = null;
        if (headTwinId != null)
            headTwinEntity = twinService.findEntitySafe(headTwinId);
        addClassCheckToValidTwinsForLinkSearch(linkEntity, srcTwinClassEntity, basicSearch);
        loadLinkValidators(linkEntity);
        for (LinkValidatorEntity linkValidatorEntity : linkEntity.getLinkValidators()) {
            Linker linker = featurerService.getFeaturer(linkValidatorEntity.getLinkerFeaturerId(), Linker.class);
            linker.expandValidLinkedTwinSearch(linkValidatorEntity.getLinkerParams(), srcTwinClassEntity, headTwinEntity, basicSearch);
        }
        return twinSearchService.findTwins(basicSearch, pagination);
    }

    public PaginationResult<TwinEntity> findValidDstTwins(UUID twinId, UUID linkId, BasicSearch basicSearch, SimplePagination pagination) throws ServiceException {
        LinkEntity linkEntity = linkService.findEntitySafe(linkId);
        TwinEntity twinEntity = twinService.findEntitySafe(twinId);
        addClassCheckToValidTwinsForLinkSearch(linkEntity, twinEntity.getTwinClass(), basicSearch);
        loadLinkValidators(linkEntity);
        for (LinkValidatorEntity linkValidatorEntity : linkEntity.getLinkValidators()) {
            Linker linker = featurerService.getFeaturer(linkValidatorEntity.getLinkerFeaturerId(), Linker.class);
            linker.expandValidLinkedTwinSearch(linkValidatorEntity.getLinkerParams(), false, twinEntity, basicSearch);
        }
        return twinSearchService.findTwins(basicSearch, pagination);
    }

    public void validateLinkByLinkers(LinkEntity linkEntity, boolean forwardElseBackward, TwinEntity twinEntity, UUID candidateTwinId) throws ServiceException {
        loadLinkValidators(linkEntity);
        if (KitUtils.isEmpty(linkEntity.getLinkValidators()))
            return;
        // internal validation, not a user-facing search — the candidate must not be filtered out by the caller's view permission
        BasicSearch basicSearch = new BasicSearch()
                .setCheckViewPermission(false);
        basicSearch.addTwinId(candidateTwinId, false); // narrow the search to the candidate twin only
        for (LinkValidatorEntity linkValidatorEntity : linkEntity.getLinkValidators()) {
            Linker linker = featurerService.getFeaturer(linkValidatorEntity.getLinkerFeaturerId(), Linker.class);
            linker.validateLink(linkValidatorEntity.getLinkerParams(), forwardElseBackward, twinEntity, basicSearch);
        }
        if (basicSearch.isEmptyResult() || twinSearchService.count(basicSearch) == 0) // existence check only — no twin hydration
            throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT,
                    linkEntity.logNormal() + " twinId[" + candidateTwinId + "] is not a valid dst twin for twinId[" + twinEntity.getId() + "] (linker validation failed)");
    }

    private void addClassCheckToValidTwinsForLinkSearch(LinkEntity linkEntity, TwinClassEntity srcTwinClass, BasicSearch search) throws ServiceException {
        if (linkService.isForwardLink(linkEntity, srcTwinClass)) {// forward link
            twinClassService.loadExtendsHierarchyChildClasses(linkEntity.getDstTwinClass());
            search.addTwinClassId(linkEntity.getDstTwinClass().getExtendsHierarchyChildClassKit().getIdSet(), false);
        } else if (linkService.isBackwardLink(linkEntity, srcTwinClass)) {// backward link
            twinClassService.loadExtendsHierarchyChildClasses(srcTwinClass);
            search.addTwinClassId(srcTwinClass.getExtendsHierarchyChildClassKit().getIdSet(), false);
        } else
            throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, "unknown link type");
    }
}
