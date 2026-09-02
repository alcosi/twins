package org.twins.core.service.twinlink;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twinlink.TwinLinkCUD;
import org.twins.core.domain.twinlink.TwinLinkCreate;
import org.twins.core.domain.twinlink.TwinLinkUpdate;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinCreateStage;
import org.twins.core.domain.twinoperation.TwinOperation;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.twin.TwinCreateStrategy;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.linker.Linker;
import org.twins.core.service.TwinChangesService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static org.twins.core.dao.specifications.link.TwinLinkSpecification.checkStrength;
import static org.twins.core.dao.specifications.link.TwinLinkSpecification.checkUuidIn;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class TwinLinkService extends EntitySecureFindServiceImpl<TwinLinkEntity> {
    private final LinkService linkService;
    private final TwinClassService twinClassService;
    private final TwinLinkRepository twinLinkRepository;
    private final TwinService twinService;
    private final TwinSearchService twinSearchService;
    @Lazy
    private final AuthService authService;
    private final EntitySmartService entitySmartService;
    private final HistoryService historyService;
    private final TwinChangesService twinChangesService;
    private final FeaturerService featurerService;
    private final UserService userService;

    @Override
    public CrudRepository<TwinLinkEntity, UUID> entityRepository() {
        return twinLinkRepository;
    }

    @Override
    public Function<TwinLinkEntity, UUID> entityGetIdFunction() {
        return TwinLinkEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinLinkEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        //todo check src and dst twins belong to the domain
        return false;
    }

    @Override
    public boolean validateEntity(TwinLinkEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getSrcTwinId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty srcTwinId");
        if (entity.getDstTwinId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty dstTwinId");
        if (entity.getLinkId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty linkId");
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getLink() == null)
                    entity.setLink(linkService.findEntitySafe(entity.getLinkId()));
                if (entity.getDstTwin() == null)
                    entity.setDstTwin(twinService.findEntitySafe(entity.getDstTwinId()));
                if (entity.getSrcTwin() == null)
                    entity.setSrcTwin(twinService.findEntitySafe(entity.getSrcTwinId()));
                if (entity.getDstTwinId() != entity.getDstTwin().getId())
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect dstTwin object");
                if (entity.getSrcTwinId() != entity.getSrcTwin().getId())
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect srcTwin object");
                if (entity.getLinkId() != entity.getLink().getId())
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect link object");
// todo
//            default:
//                if (!twinClassService.isInstanceOf(entity.getSrcTwin().getTwinClass(), entity.getLink().getSrcTwinClassId()))
//                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect srcTwinId");
//                if (!twinClassService.isInstanceOf(entity.getDstTwin().getTwinClass(), entity.getLink().getDstTwinClassId()))
//                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect dstTwinId");
        }
        return true;
    }

    public void prepareTwinLinks(TwinEntity srcTwinEntity, List<TwinLinkCreate> linksCreateList) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        List<TwinLinkEntity> linksEntityList = twinLinkEntities(linksCreateList);
        loadDstTwin(linksEntityList);
        loadLink(linksEntityList);
        for (TwinLinkEntity twinLinkEntity : linksEntityList) {
            if (twinLinkEntity.getId() == null)
                twinLinkEntity.setCreateElseUpdate(true); // fresh link entering the create flow (relink flips it back)
            Set<UUID> srcTwinExtendedClasses = srcTwinEntity.getTwinClass().getExtendedClassIdSet();
            Set<UUID> dstTwinExtendedClasses = twinLinkEntity.getDstTwin().getTwinClass().getExtendedClassIdSet();
            if (srcTwinExtendedClasses.contains(twinLinkEntity.getLink().getSrcTwinClassId())) { // forward link creation
                log.info("Forward link creation");
                twinLinkEntity
                        .setSrcTwin(srcTwinEntity)
                        .setSrcTwinId(srcTwinEntity.getId()); //dst is already filled
            } else if (srcTwinExtendedClasses.contains(twinLinkEntity.getLink().getDstTwinClassId())) { // backward link creation, dst and src twins had to change places
                log.info("Backward link creation");
                twinLinkEntity
                        .setSrcTwin(twinLinkEntity.getDstTwin())
                        .setDstTwin(srcTwinEntity)
                        .setSrcTwinId(twinLinkEntity.getDstTwinId())
                        .setDstTwinId(srcTwinEntity.getId());
                Set<UUID> temp = srcTwinExtendedClasses;
                srcTwinExtendedClasses = dstTwinExtendedClasses;
                dstTwinExtendedClasses = temp;
            } else {
                throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT, twinLinkEntity.getLink().logNormal() + " can not be created for twinId[" + srcTwinEntity.getId() + "]");
            }
            if (!srcTwinExtendedClasses.contains(twinLinkEntity.getLink().getSrcTwinClassId()))
                throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT, twinLinkEntity.getLink().logNormal() + " can not be created from twinId[" + twinLinkEntity.getSrcTwinId() + "] of twinClass[" + twinLinkEntity.getSrcTwin().getTwinClassId() + "]");
            if (!dstTwinExtendedClasses.contains(twinLinkEntity.getLink().getDstTwinClassId()))
                throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT, twinLinkEntity.getLink().logNormal() + " can not be created to twinId[" + twinLinkEntity.getDstTwinId() + "] of twinClass[" + twinLinkEntity.getDstTwin().getTwinClassId() + "]");
            twinLinkEntity.setCreatedAt(Timestamp.from(Instant.now()));
            if (twinLinkEntity.getCreatedByUserId() == null)
                twinLinkEntity
                        .setCreatedByUserId(apiUser.getUser().getId())
                        .setCreatedByUser(apiUser.getUser());
        }
    }

    public void processAlreadyExisted(List<TwinLinkCreate> linksCreateList) throws ServiceException {
        Iterator<TwinLinkCreate> iterator = linksCreateList.listIterator();
        loadLink(twinLinkEntities(linksCreateList));
        while (iterator.hasNext()) {
            TwinLinkEntity twinLinkEntity = iterator.next().getTwinLink();
            if (twinLinkEntity.getLink().getType().isUniqForSrcTwin()) {
                List<TwinLinkEntity> dbTwinLinkList = twinLinkRepository.findBySrcTwinIdAndLinkId(twinLinkEntity.getSrcTwinId(), twinLinkEntity.getLinkId(), TwinLinkEntity.class);
                if (dbTwinLinkList != null && dbTwinLinkList.size() > 1)
                    throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT, "Multiple links not valid for type[" + twinLinkEntity.getLink().getType().name() + "]");
                else if (CollectionUtils.isNotEmpty(dbTwinLinkList) && twinLinkEntity.isUniqForSrcRelink()) {
                    TwinLinkEntity dbTwinLink = dbTwinLinkList.getFirst();
                    log.warn("Link[{}] is already exists for twin[{}]. TwinLink[{}] will be updated.", twinLinkEntity.getLinkId(), twinLinkEntity.getSrcTwinId(), dbTwinLink.getId());
                    twinLinkEntity.setId(dbTwinLink.getId());
                    twinLinkEntity.setRelationTwinId(dbTwinLink.getRelationTwinId());
                    twinLinkEntity.setCreateElseUpdate(false); // relink: this is an UPDATE of the existing twin_link — its relation twin must be REUSED, not re-created
                }
            } else {
                TwinLinkEntity dbTwinLink = twinLinkRepository.findBySrcTwinIdAndDstTwinIdAndLinkId(twinLinkEntity.getSrcTwinId(), twinLinkEntity.getDstTwinId(), twinLinkEntity.getLinkId(), TwinLinkEntity.class);
                if (dbTwinLink != null) {
                    log.warn("Link[{}] is already exists for twin[{}].", twinLinkEntity.getLinkId(), twinLinkEntity.getSrcTwinId());
                    iterator.remove();
                }
            }
        }
    }

    @Transactional(rollbackFor = Throwable.class)
    public void addLinks(TwinEntity srcTwinEntity, List<TwinLinkCreate> linksCreateList) throws ServiceException {
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        addLinks(srcTwinEntity, linksCreateList, twinChangesCollector);
        twinChangesService.applyChanges(twinChangesCollector);
    }

    public void addLinks(TwinEntity srcTwinEntity, List<TwinLinkCreate> linksCreateList, TwinChangesCollector twinChangesCollector) throws ServiceException {
        prepareTwinLinks(srcTwinEntity, linksCreateList);
        processAlreadyExisted(linksCreateList);
        createRelationTwins(linksCreateList, twinChangesCollector);
        for (TwinLinkCreate linkCreate : linksCreateList) {
            TwinLinkEntity twinLinkEntity = linkCreate.getTwinLink();
            twinChangesCollector.getHistoryCollector().add(historyService.linkCreated(twinLinkEntity));
            twinChangesCollector.add(twinLinkEntity);
        }
    }

    /**
     * Shadow "relation twin" creation (mirrors the job_twin pattern in TwinTriggerService):
     * for every new twin_link whose link has relation_twin_class_id set, auto-create a twin of that class
     * with id == twin_link.id (ID equality) carrying the relation's extra attributes. Initial attribute
     * values arrive pre-converted on TwinLinkCreate (reverse-mapper layer). Batched into ONE createTwins /
     * ONE updateTwin call, accumulated into the parent collector so a single applyChanges persists
     * everything — twins are flushed before twin_links (TwinChangesService), so the twin_link.relation_twin_id
     * FK holds. Deletion is DB-level: AFTER DELETE trigger on twin_link.
     */
    private void createRelationTwins(List<TwinLinkCreate> linksCreateList, TwinChangesCollector twinChangesCollector) throws ServiceException {
        List<TwinLinkCreate> needRelationTwinCreation = new ArrayList<>();
        for (TwinLinkCreate linkCreate : linksCreateList) {
            TwinLinkEntity twinLinkEntity = linkCreate.getTwinLink();
            if (twinLinkEntity.getLink().getRelationTwinClassId() == null) // link is loaded in prepareTwinLinks
                continue;
            if (twinLinkEntity.getId() == null)
                twinLinkEntity.setId(UuidUtils.generate()); // assign now so the relation twin can share it
            needRelationTwinCreation.add(linkCreate);
        }
        if (needRelationTwinCreation.isEmpty())
            return;
        // several creates over the same twin_link (e.g. a unique link relinked twice in one request) share
        // the id adopted in processAlreadyExisted — ONE relation twin per twin_link id, first entry wins
        needRelationTwinCreation = CollectionUtils.distinctBy(needRelationTwinCreation, linkCreate -> linkCreate.getTwinLink().getId());
        // relink REUSE vs create — driven by the createElseUpdate flag (set in prepareTwinLinks for fresh
        // links, flipped by processAlreadyExisted on relink): the relation twin belongs to the link INSTANCE
        // and SURVIVES the relink — reuse it, never re-run the create pipeline over the living twin
        // (repository save with a preset id MERGES, silently clobbering name/status — not an exception)
        List<TwinLinkCreate> toCreate = new ArrayList<>(needRelationTwinCreation.size());
        List<TwinLinkCreate> relinked = new ArrayList<>(needRelationTwinCreation.size());
        for (TwinLinkCreate linkCreate : needRelationTwinCreation)
            (linkCreate.getTwinLink().isCreateElseUpdate() ? toCreate : relinked).add(linkCreate);
        List<TwinUpdate> relationTwinUpdates = relationTwinUpdates(relinked);
        if (toCreate.isEmpty() && relationTwinUpdates.isEmpty())
            return;
        if (!toCreate.isEmpty())
            twinService.createTwins(TwinCreateStage.of(relationTwinCreates(toCreate)), twinChangesCollector);
        if (!relationTwinUpdates.isEmpty())
            twinService.updateTwin(relationTwinUpdates, twinChangesCollector, false);
    }

    /**
     * Relink REUSE: apply the provided fields as field-only TwinUpdates to the surviving relation twins
     * (the relation_twin_id pointer is adopted from the DB row in processAlreadyExisted) for the caller
     * to batch into ONE updateTwin call. Throws if fields are provided but the twin_link has no relation
     * twin (the link got relation_twin_class_id configured after the twin_link was created) — fail fast.
     */
    private List<TwinUpdate> relationTwinUpdates(List<TwinLinkCreate> relinked) throws ServiceException {
        if (relinked.isEmpty())
            return Collections.emptyList();
        var updateEntityKit = new Kit<>(relinked.stream().map(TwinLinkCreate::getTwinLink).toList(), TwinLinkEntity::getId);
        loadTwin(updateEntityKit.getCollection()); // populates the surviving relation twins (throws on a broken FK reference)
        List<TwinUpdate> relationTwinUpdates = new ArrayList<>();
        for (TwinLinkCreate linkCreate : relinked) {
            var twinLinkEntity = linkCreate.getTwinLink();
            var survivingRelationTwin = twinLinkEntity.getRelationTwin();
            if (CollectionUtils.isEmpty(linkCreate.getRelationTwinFields()))
                continue;
            if (survivingRelationTwin == null) // late-enabled relation class: the twin_link row predates it, no relation twin exists
                throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT,
                        "relationTwinFields provided but twin_link[" + twinLinkEntity.getId() + "] has no relation twin");
            TwinUpdate relationTwinUpdate = new TwinUpdate();
            relationTwinUpdate.setDbTwinEntity(survivingRelationTwin);
            relationTwinUpdate.setTwinEntity(survivingRelationTwin.clone()); // field-only update: no basic changes
            relationTwinUpdate.setFields(linkCreate.getRelationTwinFields());
            relationTwinUpdate.setCanTriggerAfterOperationFactory(false); // recursion guard
            relationTwinUpdate.setLauncher(TwinOperation.Launcher.link);
            relationTwinUpdates.add(relationTwinUpdate);
        }
        return relationTwinUpdates;
    }

    /** Builds the fresh relation twin batch: one TwinEntity per twin_link (ID equality) + its TwinCreate. */
    private List<TwinCreate> relationTwinCreates(List<TwinLinkCreate> toCreate) throws ServiceException {
        linkService.loadTwinClasses(toCreate.stream().map(linkCreate -> linkCreate.getTwinLink().getLink()).toList());
        List<TwinCreate> twinCreates = new ArrayList<>(toCreate.size());
        for (TwinLinkCreate linkCreate : toCreate) {
            TwinLinkEntity twinLinkEntity = linkCreate.getTwinLink();
            LinkEntity link = twinLinkEntity.getLink();
            TwinEntity relationTwin = new TwinEntity()
                    .setId(twinLinkEntity.getId()) // ID equality: relation_twin.id == twin_link.id (same as job_twin)
                    .setTwinClassId(link.getRelationTwinClassId())
                    .setTwinClass(link.getRelationTwinClass())
                    .setName("relation twin");
            // redundant (== id) but hosts the FK; safe to set now — rollback is shared via the collector tx
            twinLinkEntity
                    .setRelationTwinId(twinLinkEntity.getId())
                    .setRelationTwin(relationTwin);
            TwinCreate twinCreate = new TwinCreate();
            twinCreate.setTwinEntity(relationTwin);
            twinCreate.setCanTriggerAfterOperationFactory(false); // recursion guard (same as job_twin)
            twinCreate.setLauncher(TwinOperation.Launcher.link);
            twinCreate.setCreateStrategy(TwinCreateStrategy.AUTO); // sketch iff required relation attributes are missing
            twinCreate.setFields(linkCreate.getRelationTwinFields()); // null-safe; converted in the reverse mapper
            twinCreates.add(twinCreate);
        }
        return twinCreates;
    }

    /** Entity view over the composition list — same instances, for the entity-typed bulk loaders. */
    private static List<TwinLinkEntity> twinLinkEntities(Collection<TwinLinkCreate> linksCreateList) {
        return linksCreateList.stream().map(TwinLinkCreate::getTwinLink).toList();
    }

    public void updateTwinLinks(TwinEntity twinEntity, List<TwinLinkUpdate> twinLinkUpdateList, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (CollectionUtils.isEmpty(twinLinkUpdateList))
            return;
        var updateEntityKit = new Kit<>(twinLinkUpdateList.stream().map(TwinLinkUpdate::getTwinLink).toList(), TwinLinkEntity::getId);
        var dbEntityKit = findEntitiesSafe(updateEntityKit.getIdSet());
        List<TwinLinkEntity> updatedTwinLinkEntityList = new ArrayList<>();
        loadTwin(dbEntityKit.getCollection());
        loadLink(dbEntityKit.getCollection());
        List<TwinUpdate> relationTwinUpdates = new ArrayList<>();
        for (TwinLinkUpdate twinLinkUpdate : twinLinkUpdateList) {
            var updateTwinLinkEntity = twinLinkUpdate.getTwinLink();
            var dbTwinLinkEntity = dbEntityKit.get(updateTwinLinkEntity.getId());
            if (twinLinkUpdate.getRelationTwinFields() != null && !twinLinkUpdate.getRelationTwinFields().isEmpty()) {
                TwinEntity relationTwin = dbTwinLinkEntity.getRelationTwin();
                if (relationTwin == null)
                    throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT,
                            "relationTwinFields provided but twin_link[" + updateTwinLinkEntity.getId() + "] has no relation twin");
                var relationTwinUpdate = new TwinUpdate();
                relationTwinUpdate.setDbTwinEntity(relationTwin);
                relationTwinUpdate.setTwinEntity(relationTwin.clone()); // field-only update: no basic changes
                relationTwinUpdate.setFields(twinLinkUpdate.getRelationTwinFields());
                relationTwinUpdate.setCanTriggerAfterOperationFactory(false); // recursion guard (same as createRelationTwins)
                relationTwinUpdate.setLauncher(TwinOperation.Launcher.link);
                relationTwinUpdates.add(relationTwinUpdate);
            }
            if (updateTwinLinkEntity.getSrcTwinId() != null && updateTwinLinkEntity.getDstTwinId() == null)
                updateTwinLinkEntity
                        .setDstTwinId(updateTwinLinkEntity.getSrcTwinId()) //shift
                        .setSrcTwinId(null);
            // per-link: the unlinked twin and the direction must pair with THEIR link in the history
            TwinEntity unlinkedTwinEntity = null;
            boolean forward = true;
            if (dbTwinLinkEntity.getSrcTwinId().equals(twinEntity.getId())) {// forward link
                unlinkedTwinEntity = dbTwinLinkEntity.getDstTwin();
                forward = true;
                dbTwinLinkEntity
                        .setDstTwinId(updateTwinLinkEntity.getDstTwinId());
            } else if (dbTwinLinkEntity.getDstTwinId().equals(twinEntity.getId())) { //backward link
                unlinkedTwinEntity = dbTwinLinkEntity.getSrcTwin();
                forward = false;
                dbTwinLinkEntity
                        .setSrcTwinId(updateTwinLinkEntity.getDstTwinId());
            }
            if (validateEntityAndLog(dbTwinLinkEntity, EntitySmartService.EntityValidateMode.beforeSave)) {
                updatedTwinLinkEntityList.add(dbTwinLinkEntity);
                twinChangesCollector.getHistoryCollector().add(historyService.linkUpdated(dbTwinLinkEntity, unlinkedTwinEntity, forward));
                twinChangesCollector.add(dbTwinLinkEntity);
            }
        }
        entitySmartService.saveAllAndLog(updatedTwinLinkEntityList, twinLinkRepository);
        if (!relationTwinUpdates.isEmpty())
            twinService.updateTwin(relationTwinUpdates, twinChangesCollector, false);
    }

    public void loadTwinLinks(TwinEntity twinEntity) throws ServiceException {
        loadTwinLinks(Collections.singletonList(twinEntity));
    }

    public void loadTwinLinks(Collection<TwinEntity> twinEntityList) throws ServiceException {
        Kit<TwinEntity, UUID> needLoad = new Kit<>(TwinEntity::getId);
        for (TwinEntity twinEntity : twinEntityList)
            if (twinEntity.getTwinLinks() == null) {
                // it's important to create it here, because this will indicate in future that links are already loaded
                // (even if there are no links in db, we should not try to load them no more time)
                twinEntity.setTwinLinks(new FindTwinLinksResult());
                needLoad.add(twinEntity);
            }
        if (needLoad.isEmpty())
            return;
        List<TwinLinkEntity> twinLinkEntityList = twinLinkRepository
//                .findBySrcTwinIdInOrDstTwinIdIn(needLoad.keySet(), needLoad.keySet()); //backward links loading is disabled because of huge data
                .findBySrcTwinIdIn(needLoad.getIdSet());
        if (CollectionUtils.isEmpty(twinLinkEntityList))
            return;
        for (var loadedLink : twinLinkEntityList) {
            loadedLink.setSrcTwin(needLoad.get(loadedLink.getSrcTwinId()));
        }
        loadDstTwin(twinLinkEntityList);
        loadLink(twinLinkEntityList);
        TwinEntity twinEntity = null;
        for (TwinLinkEntity twinLinkEntity : twinLinkEntityList) {
            if (needLoad.get(twinLinkEntity.getSrcTwinId()) != null) {
                if (twinService.isEntityReadDenied(twinLinkEntity.getDstTwin(), EntitySmartService.ReadPermissionCheckMode.ifDeniedLog))
                    continue;
                twinEntity = needLoad.get(twinLinkEntity.getSrcTwinId());
                twinEntity.getTwinLinks().forwardLinks.add(twinLinkEntity);
            }
//            if (needLoad.get(twinLinkEntity.getDstTwinId()) != null) {
//                if (twinService.isEntityReadDenied(twinLinkEntity.getSrcTwin(), EntitySmartService.ReadPermissionCheckMode.ifDeniedLog))
//                    continue;
//                twinEntity = needLoad.get(twinLinkEntity.getDstTwinId());
//                twinEntity.getTwinLinks().backwardLinks.add(twinLinkEntity);
//            }
        }
    }

    public KitGrouped<TwinLinkEntity, UUID, UUID> findTwinForwardLinks(TwinEntity twinEntity) throws ServiceException {
        loadTwinLinks(twinEntity);
        return twinEntity.getTwinLinks().getForwardLinks();
    }

    public List<TwinLinkEntity> findTwinForwardLinks(TwinEntity twinEntity, Collection<UUID> linkIdCollection) throws ServiceException {
        List<TwinLinkEntity> twinLinkEntityList = twinLinkRepository.findBySrcTwinIdAndLinkIdIn(twinEntity.getId(), linkIdCollection, TwinLinkEntity.class);
        return filterDenied(twinLinkEntityList);
    }

    public List<TwinLinkEntity> findTwinBackwardLinks(UUID twinId) throws ServiceException {
        List<TwinLinkEntity> twinLinkEntityList = twinLinkRepository.findByDstTwinId(twinId, TwinLinkEntity.class);
        return filterDenied(twinLinkEntityList);
    }

    public List<TwinLinkEntity> findTwinBackwardLinksAndLinkStrengthIds(Collection<UUID> twinIds, List<LinkStrength> strengthIds) throws ServiceException {
        List<TwinLinkEntity> twinLinkEntityList = twinLinkRepository.findAll(
                checkStrength(strengthIds)
                        .and(checkUuidIn(twinIds, false, false, TwinLinkEntity.Fields.dstTwinId))
        );
        return filterDenied(twinLinkEntityList);
    }

    protected List<TwinLinkEntity> filterDenied(List<TwinLinkEntity> twinLinkEntityList) throws ServiceException {
        loadTwin(twinLinkEntityList);
        loadLink(twinLinkEntityList);
        ListIterator<TwinLinkEntity> iterator = twinLinkEntityList.listIterator();
        TwinLinkEntity twinLinkEntity;
        while (iterator.hasNext()) {
            twinLinkEntity = iterator.next();
            if (twinService.isEntityReadDenied(twinLinkEntity.getSrcTwin(), EntitySmartService.ReadPermissionCheckMode.ifDeniedLog))
                iterator.remove();
        }
        return twinLinkEntityList;
    }

    public void deleteTwinLinks(UUID twinId, List<TwinLinkEntity> twinLinksDeleteList, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (CollectionUtils.isEmpty(twinLinksDeleteList))
            return;
        loadTwin(twinLinksDeleteList);
        loadLink(twinLinksDeleteList);
        for (TwinLinkEntity twinLinkEntity : twinLinksDeleteList) {
            if (!twinLinkEntity.getSrcTwinId().equals(twinId) && !twinLinkEntity.getDstTwinId().equals(twinId)) {
                log.error("{} can not be delete because it's from other twin", twinLinkEntity.logShort());
                continue;
            }
            if (twinLinkEntity.getLink().getLinkStrengthId().equals(LinkStrength.MANDATORY)) {
                log.error("{} can not be deleted because link is mandatory", twinLinkEntity.logShort());
                continue;
            }
            twinChangesCollector.getHistoryCollector().add(historyService.linkDeleted(twinLinkEntity));
            twinChangesCollector.delete(twinLinkEntity);
        }
    }

    public PaginationResult<TwinEntity> findValidDstTwins(UUID twinClassId, UUID linkId, UUID headTwinId, BasicSearch basicSearch, SimplePagination pagination) throws ServiceException {
        LinkEntity linkEntity = linkService.findEntitySafe(linkId);
        TwinClassEntity srcTwinClassEntity = twinClassService.findEntitySafe(twinClassId);
        TwinEntity headTwinEntity = null;
        if (headTwinId != null)
            headTwinEntity = twinService.findEntitySafe(headTwinId);
        addClassCheckToValidTwinsForLinkSearch(linkEntity, srcTwinClassEntity, basicSearch);
        if (linkEntity.getLinkerFeaturerId() != null) {
            Linker linker = featurerService.getFeaturer(linkEntity.getLinkerFeaturerId(), Linker.class);
            linker.expandValidLinkedTwinSearch(linkEntity.getLinkerParams(), srcTwinClassEntity, headTwinEntity, basicSearch);
        }
        return twinSearchService.findTwins(basicSearch, pagination);
    }

    public PaginationResult<TwinEntity> findValidDstTwins(UUID twinId, UUID linkId, BasicSearch basicSearch, SimplePagination pagination) throws ServiceException {
        LinkEntity linkEntity = linkService.findEntitySafe(linkId);
        TwinEntity twinEntity = twinService.findEntitySafe(twinId);
        addClassCheckToValidTwinsForLinkSearch(linkEntity, twinEntity.getTwinClass(), basicSearch);
        if (linkEntity.getLinkerFeaturerId() != null) {
            Linker linker = featurerService.getFeaturer(linkEntity.getLinkerFeaturerId(), Linker.class);
            linker.expandValidLinkedTwinSearch(linkEntity.getLinkerParams(), twinEntity, basicSearch);
        }
        return twinSearchService.findTwins(basicSearch, pagination);
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

    public Collection<TwinLinkEntity> findTwinLinks(LinkEntity linkEntity, TwinEntity twinEntity, LinkService.LinkDirection linkDirection) throws ServiceException {
        if (linkDirection == null)
            linkDirection = linkService.detectLinkDirection(linkEntity, twinEntity.getTwinClass());
        switch (linkDirection) {
            case forward:
                if (twinEntity.getTwinLinks() != null)
                    return twinEntity.getTwinLinks().forwardLinks.getCollection();
                return twinLinkRepository.findBySrcTwinIdAndLinkId(twinEntity.getId(), linkEntity.getId(), TwinLinkEntity.class);
            case backward:
                if (twinEntity.getTwinLinks() != null)
                    return twinEntity.getTwinLinks().backwardLinks.getCollection();
                return twinLinkRepository.findByDstTwinIdAndLinkId(twinEntity.getId(), linkEntity.getId(), TwinLinkEntity.class);
            default:
                return null;
        }
    }

    public Set<UUID> findSrcTwinIdsByLinkId(@NonNull UUID linkId) {
        return twinLinkRepository.findSrcTwinIdsByLinkId(linkId);
    }

    public Set<UUID> findDstTwinIdsByLinkId(@NonNull UUID linkId) {
        return twinLinkRepository.findDstTwinIdsByLinkId(linkId);
    }

    public Map<UUID, Integer> countBackwardLinks(Collection<UUID> dstTwinIdList, UUID linkId) {
        if (linkId == null || dstTwinIdList == null || dstTwinIdList.isEmpty())
            return Collections.emptyMap();
        List<Object[]> rows = twinLinkRepository.countBackwardLinks(dstTwinIdList, linkId);
        if (rows == null || rows.isEmpty())
            return Collections.emptyMap();
        int expectedSize = rows.size();
        Map<UUID, Integer> result = new HashMap<>( expectedSize);
        for (Object[] row : rows) {
            if (row == null || row.length < 2)
                continue;
            Object idObj = row[0];
            Object cntObj = row[1];
            if (idObj == null || cntObj == null)
                continue;
            UUID id = (UUID) idObj;
            int cnt = (cntObj instanceof Number n) ? n.intValue() : Integer.parseInt(cntObj.toString());
            result.put(id, cnt);
        }
        return result.isEmpty() ? Collections.emptyMap() : result;
    }

    @Data
    @Accessors(chain = true)
    public static class FindTwinLinksResult {
        public static final FindTwinLinksResult EMPTY = new FindTwinLinksResult();
        UUID twinId;
        KitGrouped<TwinLinkEntity, UUID, UUID> forwardLinks = new KitGrouped<>(TwinLinkEntity::getId, TwinLinkEntity::getLinkId);
        @Deprecated //backwardLinks should be taken from API with pagination support
        KitGrouped<TwinLinkEntity, UUID, UUID> backwardLinks = new KitGrouped<>(TwinLinkEntity::getId, TwinLinkEntity::getLinkId); //todo delete me, I can cause large memory usage
    }

    public static boolean equalsInSrcTwinIdAndDstTwinId(TwinLinkEntity one, TwinLinkEntity two) {
        return one.getSrcTwinId().equals(two.getSrcTwinId()) && one.getDstTwinId().equals(two.getDstTwinId());
    }

    public boolean hasLink(TwinEntity twinEntity, UUID linkId) {
        if (twinEntity.getTwinLinks() != null && twinEntity.getTwinLinks().getForwardLinks() != null)
            return twinEntity.getTwinLinks().getForwardLinks().containsGroupedKey(linkId);
        return twinLinkRepository.existsBySrcTwinIdAndLinkId(twinEntity.getId(), linkId);
    }

    public boolean hasBackwardLink(TwinEntity twinEntity, UUID linkId) {
        if (twinEntity.getTwinLinks() != null && twinEntity.getTwinLinks().getBackwardLinks() != null)
            return twinEntity.getTwinLinks().getBackwardLinks().containsGroupedKey(linkId);
        return twinLinkRepository.existsByDstTwinIdAndLinkId(twinEntity.getId(), linkId);
    }

    public List<TwinLinkEntity> findTwinLinksBySrcTwinAndLinkId(TwinEntity twinEntity, UUID linkId) {
        if (twinEntity.getTwinLinks() != null && twinEntity.getTwinLinks().getBackwardLinks() != null)
            return twinEntity.getTwinLinks().getForwardLinks().getGrouped(linkId);
        return twinLinkRepository.findBySrcTwinIdAndLinkId(twinEntity.getId(), linkId, TwinLinkEntity.class);
    }

    public boolean isLinkDstTwinStatusIn(TwinEntity twin, UUID linkId, Set<UUID> statusIds) throws ServiceException {
        loadTwinLinks(twin);
        List<TwinLinkEntity> twinLinkEntityList = twin.getTwinLinks().getForwardLinks().getGrouped(linkId);

        if (twinLinkEntityList.size() != 1) {
            throw new ServiceException(ErrorCodeTwins.TWIN_VALIDATOR_INCORRECT, "this validator can't validate twin with more than 1 link with linkId[" + linkId + "]");
        } else {
            loadDstTwin(twinLinkEntityList);
            return statusIds.contains(twinLinkEntityList.getFirst().getDstTwin().getTwinStatusId());
        }
    }

    public void cudTwinLinks(TwinEntity twinEntity, TwinLinkCUD twinLinkCUD, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinLinkCUD == null)
            return;
        if (CollectionUtils.isNotEmpty(twinLinkCUD.getCreateList())) {
            addLinks(twinEntity, twinLinkCUD.getCreateList(), twinChangesCollector);
        }
        if (CollectionUtils.isNotEmpty(twinLinkCUD.getUpdateList())) {
            updateTwinLinks(twinEntity, twinLinkCUD.getUpdateList(), twinChangesCollector);
        }
        if (CollectionUtils.isNotEmpty(twinLinkCUD.getDeleteList())) {
            deleteTwinLinks(twinEntity.getId(), twinLinkCUD.getDeleteList(), twinChangesCollector);
        }
    }

    public TwinEntity getDstTwinSafe(TwinLinkEntity twinLinkEntity) throws ServiceException {
        loadDstTwin(twinLinkEntity);
        return twinLinkEntity.getDstTwin();
    }

    public Set<TwinLinkEntity> findAllWithinHierarchies(Collection<UUID> hierarchies) {
        return twinLinkRepository.findAllWithinHierarchies(hierarchies);
    }

    public Set<TwinLinkEntity> findAllWithinHierarchiesAndLinkIdIn(Collection<UUID> hierarchies, Collection<UUID> linkIds) {
        return twinLinkRepository.findAllWithinHierarchiesAndLinkIdIn(hierarchies, linkIds);
    }

    public Set<TwinLinkEntity> findAllWithinHierarchiesAndTwinsInStatusIds(Collection<UUID> hierarchies, Collection<UUID> twinStatusIds) {
        return twinLinkRepository.findAllWithinHierarchiesAndTwinsInStatusIds(hierarchies, twinStatusIds);
    }

    public Set<TwinLinkEntity> findAllWithinHierarchiesAndLinkIdInAndTwinsInStatusIds(Collection<UUID> hierarchies, Collection<UUID> linkIds, Collection<UUID> twinStatusIds) {
        return twinLinkRepository.findAllWithinHierarchiesAndLinkIdInAndTwinsInStatusIds(hierarchies, linkIds, twinStatusIds);
    }

    /** Links with both src and dst in {@code twinIds} (no hierarchy-tree filter). */
    public Set<TwinLinkEntity> findAllBetweenTwinsIn(Collection<UUID> twinIds) {
        if (CollectionUtils.isEmpty(twinIds)) {
            return Collections.emptySet();
        }
        return twinLinkRepository.findAllBetweenTwinsIn(twinIds);
    }

    public Set<TwinLinkEntity> findAllBetweenTwinsInAndTwinsInStatusIds(Collection<UUID> twinIds, Collection<UUID> twinStatusIds) {
        if (CollectionUtils.isEmpty(twinIds) || CollectionUtils.isEmpty(twinStatusIds)) {
            return Collections.emptySet();
        }
        return twinLinkRepository.findAllBetweenTwinsInAndTwinsInStatusIds(twinIds, twinStatusIds);
    }

    public Set<TwinLinkEntity> findAllBetweenTwinsInAndLinkIdIn(Collection<UUID> twinIds, Collection<UUID> linkIds) {
        if (CollectionUtils.isEmpty(twinIds) || CollectionUtils.isEmpty(linkIds)) {
            return Collections.emptySet();
        }
        return twinLinkRepository.findAllBetweenTwinsInAndLinkIdIn(twinIds, linkIds);
    }

    public Set<TwinLinkEntity> findAllBetweenTwinsInAndLinkIdInAndTwinsInStatusIds(Collection<UUID> twinIds, Collection<UUID> linkIds, Collection<UUID> twinStatusIds) {
        if (CollectionUtils.isEmpty(twinIds) || CollectionUtils.isEmpty(linkIds) || CollectionUtils.isEmpty(twinStatusIds)) {
            return Collections.emptySet();
        }
        return twinLinkRepository.findAllBetweenTwinsInAndLinkIdInAndTwinsInStatusIds(twinIds, linkIds, twinStatusIds);
    }

    public Set<TwinLinkEntity> findAllByLinkIdInAndSrcTwinIdInOrDstTwinIdIn(Collection<UUID> linkIds, Collection<UUID> twinIds) {
        if (CollectionUtils.isEmpty(linkIds) || CollectionUtils.isEmpty(twinIds)) {
            return Collections.emptySet();
        }
        return twinLinkRepository.findAllByLinkIdInAndSrcTwinIdInOrDstTwinIdIn(linkIds, twinIds);
    }

    /**
     * Like {@link #findAllBetweenTwinsInAndLinkIdInAndTwinsInStatusIds}, but factory input twins skip the status filter
     * on their endpoint (so links to/from roots passed into the factory are still loaded).
     */
    public Set<TwinLinkEntity> findAllBetweenTwinsInAndLinkIdInAndTwinsInStatusIdsOrInputTwins(Collection<UUID> twinIds, Collection<UUID> linkIds, Collection<UUID> twinStatusIds, Collection<UUID> inputTwinIds) {
        if (CollectionUtils.isEmpty(twinIds) || CollectionUtils.isEmpty(linkIds) || CollectionUtils.isEmpty(twinStatusIds)) {
            return Collections.emptySet();
        }
        if (CollectionUtils.isEmpty(inputTwinIds)) {
            return twinLinkRepository.findAllBetweenTwinsInAndLinkIdInAndTwinsInStatusIds(twinIds, linkIds, twinStatusIds);
        }
        return twinLinkRepository.findAllBetweenTwinsInAndLinkIdInAndTwinsInStatusIdsOrInputTwins(twinIds, linkIds, twinStatusIds, inputTwinIds);
    }

    public void loadCreatedByUser(TwinLinkEntity entity) throws ServiceException {
        loadCreatedByUser(Collections.singletonList(entity));
    }

    public void loadCreatedByUser(Collection<TwinLinkEntity> entities) throws ServiceException {
        userService.load(entities,
                TwinLinkEntity::getCreatedByUserId,
                TwinLinkEntity::getCreatedByUser,
                TwinLinkEntity::setCreatedByUser);
    }

    public void loadSrcTwin(TwinLinkEntity entity) throws ServiceException {
        loadSrcTwin(Collections.singletonList(entity));
    }

    public void loadSrcTwin(Collection<TwinLinkEntity> entities) throws ServiceException {
        twinService.load(entities,
                TwinLinkEntity::getSrcTwinId,
                TwinLinkEntity::getSrcTwin,
                TwinLinkEntity::setSrcTwin);
    }

    public void loadDstTwin(TwinLinkEntity entity) throws ServiceException {
        loadDstTwin(Collections.singletonList(entity));
    }

    public void loadDstTwin(Collection<TwinLinkEntity> entities) throws ServiceException {
        twinService.load(entities,
                TwinLinkEntity::getDstTwinId,
                TwinLinkEntity::getDstTwin,
                TwinLinkEntity::setDstTwin);
    }

    public void loadTwin(TwinLinkEntity entity) throws ServiceException {
        loadTwin(Collections.singletonList(entity));
    }

    public void loadTwin(Collection<TwinLinkEntity> entities) throws ServiceException {
        twinService.load(entities,
                new LoadedField<>(
                        TwinLinkEntity::getDstTwinId,
                        TwinLinkEntity::getDstTwin,
                        TwinLinkEntity::setDstTwin),
                new LoadedField<>(
                        TwinLinkEntity::getSrcTwinId,
                        TwinLinkEntity::getSrcTwin,
                        TwinLinkEntity::setSrcTwin),
                new LoadedField<>(
                        TwinLinkEntity::getRelationTwinId,
                        TwinLinkEntity::getRelationTwin,
                        TwinLinkEntity::setRelationTwin)
                );
    }

    public void loadLink(TwinLinkEntity entity) throws ServiceException {
        loadLink(Collections.singletonList(entity));
    }

    public void loadLink(Collection<TwinLinkEntity> entities) throws ServiceException {
        linkService.load(entities,
                TwinLinkEntity::getLinkId,
                TwinLinkEntity::getLink,
                TwinLinkEntity::setLink);
    }

    public void loadRelationTwin(TwinLinkEntity entity) throws ServiceException {
        loadRelationTwin(Collections.singletonList(entity));
    }

    public void loadRelationTwin(Collection<TwinLinkEntity> entities) throws ServiceException {
        twinService.load(entities,
                TwinLinkEntity::getRelationTwinId,
                TwinLinkEntity::getRelationTwin,
                TwinLinkEntity::setRelationTwin);
    }
}
