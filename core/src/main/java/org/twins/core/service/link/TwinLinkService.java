package org.twins.core.service.link;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinLinkNoRelationsProjection;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.linker.Linker;
import org.twins.core.service.TwinChangesService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

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
            default:
                if (!twinClassService.isInstanceOf(entity.getSrcTwin().getTwinClass(), entity.getLink().getSrcTwinClassId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect srcTwinId");
                if (!twinClassService.isInstanceOf(entity.getDstTwin().getTwinClass(), entity.getLink().getDstTwinClassId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect dstTwinId");
        }
        return true;
    }

    public void prepareTwinLinks(TwinEntity srcTwinEntity, List<TwinLinkEntity> linksEntityList) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        for (TwinLinkEntity twinLinkEntity : linksEntityList) {
            if (twinLinkEntity.getLink() == null)
                twinLinkEntity.setLink(linkService.findLinkByIdCached(twinLinkEntity.getLinkId()));
            if (twinLinkEntity.getDstTwin() == null)
                twinLinkEntity.setDstTwin(twinService.findEntity(twinLinkEntity.getDstTwinId(), EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows));
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

    public void processAlreadyExisted(List<TwinLinkEntity> linksEntityList) throws ServiceException {
        Iterator<TwinLinkEntity> iterator = linksEntityList.listIterator();
        while (iterator.hasNext()) {
            TwinLinkEntity twinLinkEntity = iterator.next();
            if (twinLinkEntity.getLink().getType().isUniqForSrcTwin()) {
                List<TwinLinkNoRelationsProjection> dbTwinLinkList = twinLinkRepository.findBySrcTwinIdAndLinkId(twinLinkEntity.getSrcTwinId(), twinLinkEntity.getLinkId(), TwinLinkNoRelationsProjection.class);
                if (dbTwinLinkList != null && dbTwinLinkList.size() > 1)
                    throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT, "Multiple links not valid for type[" + twinLinkEntity.getLink().getType().name() + "]");
                else if (CollectionUtils.isNotEmpty(dbTwinLinkList) && twinLinkEntity.isUniqForSrcRelink()) {
                    TwinLinkNoRelationsProjection dbTwinLink = dbTwinLinkList.get(1);
                    log.warn(twinLinkEntity.getLink().logShort() + " is already exists for " + twinLinkEntity.getSrcTwin().logShort() + ". " + dbTwinLink.easyLog(EasyLoggable.Level.NORMAL) + " will be updated");
                    twinLinkEntity.setId(dbTwinLink.id());
                }
            } else {
                TwinLinkNoRelationsProjection dbTwinLink = twinLinkRepository.findBySrcTwinIdAndDstTwinIdAndLinkId(twinLinkEntity.getSrcTwinId(), twinLinkEntity.getDstTwinId(), twinLinkEntity.getLinkId(), TwinLinkNoRelationsProjection.class);
                if (dbTwinLink != null) {
                    log.warn(twinLinkEntity.getLink().logShort() + " is already exists for " + twinLinkEntity.getSrcTwin().logShort() + ".");
                    iterator.remove();
                }
            }
        }
    }

    public void addLinks(TwinEntity srcTwinEntity, List<TwinLinkEntity> linksEntityList) throws ServiceException {
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();
        addLinks(srcTwinEntity, linksEntityList, twinChangesCollector);
        twinChangesService.applyChanges(twinChangesCollector);
    }

    public void addLinks(TwinEntity srcTwinEntity, List<TwinLinkEntity> linksEntityList, TwinChangesCollector twinChangesCollector) throws ServiceException {
        prepareTwinLinks(srcTwinEntity, linksEntityList);
        processAlreadyExisted(linksEntityList);
        for (TwinLinkEntity twinLinkEntity : linksEntityList) {
            twinChangesCollector.getHistoryCollector().add(historyService.linkCreated(twinLinkEntity));
            twinChangesCollector.add(twinLinkEntity);
        }
    }

    public void updateTwinLinks(TwinEntity twinEntity, List<TwinLinkEntity> twinLinkEntityList, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (CollectionUtils.isEmpty(twinLinkEntityList))
            return;
        TwinLinkEntity dbTwinLinkEntity;
        TwinEntity unlinkedTwinEntity = null;
        boolean forward = true;
        List<TwinLinkEntity> updatedTwinLinkEntityList = new ArrayList<>();
        for (TwinLinkEntity updateTwinLinkEntity : twinLinkEntityList) {
            dbTwinLinkEntity = entitySmartService.findById(updateTwinLinkEntity.getId(), twinLinkRepository, EntitySmartService.FindMode.ifEmptyLogAndNull);
            if (dbTwinLinkEntity == null)
                continue;
            if (updateTwinLinkEntity.getSrcTwinId() != null && updateTwinLinkEntity.getDstTwinId() == null)
                updateTwinLinkEntity
                        .setDstTwinId(updateTwinLinkEntity.getSrcTwinId()) //shift
                        .setSrcTwinId(null);
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
            if (validateEntityAndLog(dbTwinLinkEntity, EntitySmartService.EntityValidateMode.beforeSave))
                updatedTwinLinkEntityList.add(dbTwinLinkEntity);
        }
        for (TwinLinkEntity twinLinkEntity : updatedTwinLinkEntityList) {
            twinChangesCollector.getHistoryCollector().add(historyService.linkUpdated(twinLinkEntity, unlinkedTwinEntity, forward));
            twinChangesCollector.add(twinLinkEntity);
        }
        entitySmartService.saveAllAndLog(updatedTwinLinkEntityList, twinLinkRepository);
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
        for (TwinLinkEntity twinLinkEntity : twinLinksDeleteList) {
            if (!twinLinkEntity.getSrcTwinId().equals(twinId) && !twinLinkEntity.getDstTwinId().equals(twinId)) {
                log.error(twinLinkEntity.logShort() + " can not be delete because it's from other twin");
                continue;
            }
            if (twinLinkEntity.getLink().getLinkStrengthId().equals(LinkStrength.MANDATORY)) {
                log.error(twinLinkEntity.logShort() + " can not be deleted because link is mandatory");
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

    public boolean isLinkDstTwinStatusIn(TwinEntity twin, UUID linkId, Set<UUID> statusIds) throws ServiceException {
        loadTwinLinks(twin);
        List<TwinLinkEntity> twinLinkEntityList = twin.getTwinLinks().getForwardLinks().getGrouped(linkId);

        if (twinLinkEntityList.size() != 1) {
            throw new ServiceException(ErrorCodeTwins.TWIN_VALIDATOR_INCORRECT, "this validator can't validate twin with more than 1 link with linkId[" + linkId + "]");
        } else {
            return statusIds.contains(twinLinkEntityList.getFirst().getDstTwin().getTwinStatusId());
        }
    }

    public void cudTwinLinks(TwinEntity twinEntity, EntityCUD<TwinLinkEntity> twinLinkCUD, TwinChangesCollector twinChangesCollector) throws ServiceException {
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
        if (twinLinkEntity.getDstTwin() == null && twinLinkEntity.getDstTwinId() != null) {
            twinLinkEntity.setDstTwin(twinService.findEntity(twinLinkEntity.getDstTwinId(), EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows));
        }
        return twinLinkEntity.getDstTwin();
    }
}
