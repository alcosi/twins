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
        Map<UUID, Integer> dbMaxOrders = prefetchMaxOrders(creates); // one group query for the whole batch
        Map<UUID, List<Integer>> claimedOrders = new HashMap<>(); // linkId -> orders claimed within this batch
        checkParentLinksAccessible(creates); // one batch call instead of one find per create
        Set<UUID> explicitOrderLinkIds = new HashSet<>();
        for (LinkValidatorCreate create : creates)
            if (create.getLinkValidator().getOrder() != null)
                explicitOrderLinkIds.add(create.getLinkValidator().getLinkId());
        Map<UUID, Map<UUID, Integer>> dbSiblings = loadOccupiedOrders(explicitOrderLinkIds); // one projection query for the batch
        List<LinkValidatorEntity> entitiesToSave = new ArrayList<>(creates.size());
        for (LinkValidatorCreate create : creates) {
            LinkValidatorEntity entity = create.getLinkValidator();
            validateAndPrepareFeaturer(entity.getLinkerFeaturerId(), entity.getLinkerParams(), Linker.class);
            if (entity.getOrder() == null)
                entity.setOrder(nextFreeOrder(entity.getLinkId(), dbMaxOrders, claimedOrders));
            else
                checkOrderFree(entity, dbSiblings, claimedOrders);
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
        // existence + read permission on the requested parent links — one batch call instead of one find per moved row
        Set<UUID> requestedLinkIds = new HashSet<>();
        for (LinkValidatorUpdate update : updates)
            if (update.getLinkValidator().getLinkId() != null)
                requestedLinkIds.add(update.getLinkValidator().getLinkId());
        linkService.findEntitiesSafe(requestedLinkIds);
        // order-conflict snapshot: target links = requested (moves) + current of all updated validators
        Set<UUID> orderCheckLinkIds = new HashSet<>(requestedLinkIds);
        for (LinkValidatorEntity entity : entitiesKit.getCollection())
            orderCheckLinkIds.add(entity.getLinkId());
        Map<UUID, Map<UUID, Integer>> dbSiblings = loadOccupiedOrders(orderCheckLinkIds);
        Map<UUID, List<Integer>> claimedOrders = new HashMap<>();
        List<LinkValidatorEntity> allEntities = new ArrayList<>(updates.size());
        for (LinkValidatorUpdate update : updates) {
            LinkValidatorEntity entity = entitiesKit.get(update.getId());
            allEntities.add(entity);
            ChangesHelper changesHelper = new ChangesHelper();
            LinkValidatorEntity sourceEntity = update.getLinkValidator();
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
                checkOrderFree(entity, dbSiblings, claimedOrders);
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
     * Sibling (validatorId -> order) per link for the whole batch in one projection query — no
     * entity hydration, and {@link #checkOrderFree} never hits the db per entity (N+1 on bulk
     * create/update with explicit orders). Snapshot of uncommitted state: both batch methods save
     * after the loop, so this equals what the old per-row reads saw.
     */
    private Map<UUID, Map<UUID, Integer>> loadOccupiedOrders(Collection<UUID> linkIds) {
        Map<UUID, Map<UUID, Integer>> result = new HashMap<>();
        if (linkIds.isEmpty())
            return result;
        for (Object[] row : repository.findSiblingsOrderByLinkIdIn(linkIds))
            result.computeIfAbsent((UUID) row[1], k -> new HashMap<>()).put((UUID) row[0], (Integer) row[2]);
        return result;
    }

    /**
     * Unique index {@code link_validator(link_id, order)}: the requested order must be free in db
     * (snapshot from {@link #loadOccupiedOrders}) and not claimed by a sibling from the same batch.
     */
    private void checkOrderFree(LinkValidatorEntity entity, Map<UUID, Map<UUID, Integer>> dbSiblings, Map<UUID, List<Integer>> claimedOrders) throws ServiceException {
        List<Integer> claimed = claimedOrders.computeIfAbsent(entity.getLinkId(), k -> new ArrayList<>());
        for (Map.Entry<UUID, Integer> sibling : dbSiblings.getOrDefault(entity.getLinkId(), Map.of()).entrySet()) {
            if (entity.getId() != null && entity.getId().equals(sibling.getKey()))
                continue; // own row — order change is handled by the db update
            if (sibling.getValue().equals(entity.getOrder()))
                throw new ServiceException(ErrorCodeTwins.LINK_VALIDATOR_ORDER_CONFLICT,
                        entity.logShort() + " order[" + entity.getOrder() + "] is already taken by linkValidator[" + sibling.getKey() + "] of link[" + entity.getLinkId() + "]");
        }
        if (claimed.contains(entity.getOrder()))
            throw new ServiceException(ErrorCodeTwins.LINK_VALIDATOR_ORDER_CONFLICT,
                    entity.logShort() + " order[" + entity.getOrder() + "] is claimed twice in the batch for link[" + entity.getLinkId() + "]");
        claimed.add(entity.getOrder());
    }

    /**
     * Parent-link existence + read permission for the whole batch in one
     * {@link EntitySecureFindServiceImpl#findEntitiesSafe} call (ifMissedThrows + ifDeniedThrows) —
     * replaces the per-create {@code findEntitySafe} (N+1 on bulk creates into one link).
     */
    private void checkParentLinksAccessible(List<LinkValidatorCreate> creates) throws ServiceException {
        Set<UUID> linkIds = new HashSet<>();
        for (LinkValidatorCreate create : creates) {
            UUID linkId = create.getLinkValidator().getLinkId();
            if (linkId == null)
                throw new ServiceException(ErrorCodeTwins.UUID_IS_NULL, "no Link can be found by null id");
            linkIds.add(linkId);
        }
        linkService.findEntitiesSafe(linkIds);
    }

    /**
     * Db max order per link for the whole batch in one group query — {@link #nextFreeOrder} never
     * hits the db per entity (N+1 on bulk creates).
     */
    private Map<UUID, Integer> prefetchMaxOrders(List<LinkValidatorCreate> creates) {
        Set<UUID> linkIds = new HashSet<>();
        for (LinkValidatorCreate create : creates)
            if (create.getLinkValidator().getOrder() == null)
                linkIds.add(create.getLinkValidator().getLinkId());
        Map<UUID, Integer> result = new HashMap<>();
        if (!linkIds.isEmpty())
            for (Object[] row : repository.findMaxOrderByLinkIdIn(linkIds))
                result.put((UUID) row[0], row[1] == null ? 0 : ((Number) row[1]).intValue());
        return result;
    }

    /**
     * Hands out the first order above the db max not claimed by this batch (batch-aware via
     * {@code claimedOrders}, db max prefetched by {@link #prefetchMaxOrders}).
     */
    private int nextFreeOrder(UUID linkId, Map<UUID, Integer> dbMaxOrders, Map<UUID, List<Integer>> claimedOrders) {
        List<Integer> claimed = claimedOrders.computeIfAbsent(linkId, k -> new ArrayList<>());
        int next = dbMaxOrders.getOrDefault(linkId, 0) + 1;
        while (claimed.contains(next))
            next++; // skip orders claimed by explicit values or earlier auto-appends in this batch
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
