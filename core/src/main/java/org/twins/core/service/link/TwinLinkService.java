package org.twins.core.service.link;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinLinkNoRelationsProjection;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.BasicSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class TwinLinkService extends EntitySecureFindServiceImpl<TwinLinkEntity> {
    final LinkService linkService;
    final TwinClassService twinClassService;
    final TwinLinkRepository twinLinkRepository;
    final TwinService twinService;
    final TwinSearchService twinSearchService;
    @Lazy
    final AuthService authService;
    final EntitySmartService entitySmartService;

    @Override
    public CrudRepository<TwinLinkEntity, UUID> entityRepository() {
        return twinLinkRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinLinkEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return true;
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
                if (!twinClassService.isInstanceOf(entity.getSrcTwin().getTwinClassId(), entity.getLink().getSrcTwinClassId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect srcTwinId");
                if (!twinClassService.isInstanceOf(entity.getDstTwin().getTwinClassId(), entity.getLink().getDstTwinClassId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incorrect dstTwinId");
        }
        return true;
    }

    public void prepareTwinLinks(TwinEntity srcTwinEntity, List<TwinLinkEntity> linksEntityList) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        for (TwinLinkEntity twinLinkEntity : linksEntityList) {
            if (twinLinkEntity.getLink() == null)
                twinLinkEntity.setLink(linkService.findEntity(twinLinkEntity.getLinkId(), EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows));
            if (twinLinkEntity.getDstTwin() == null)
                twinLinkEntity.setDstTwin(twinService.findEntity(twinLinkEntity.getDstTwinId(), EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows));
            Set<UUID> srcTwinExtendedClasses = twinClassService.loadExtendedClasses(srcTwinEntity.getTwinClass());
            Set<UUID> dstTwinExtendedClasses = twinClassService.loadExtendedClasses(twinLinkEntity.getDstTwin().getTwinClass());
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
        prepareTwinLinks(srcTwinEntity, linksEntityList);
        processAlreadyExisted(linksEntityList);
        entitySmartService.saveAllAndLog(linksEntityList, twinLinkRepository);
    }

    @Transactional
    public void updateTwinLinks(TwinEntity twinEntity, List<TwinLinkEntity> twinLinkEntityList) throws ServiceException {
        if (CollectionUtils.isEmpty(twinLinkEntityList))
            return;
        TwinLinkEntity dbTwinLinkEntity;
        List<TwinLinkEntity> updatedTwinLinkEntityList = new ArrayList<>();
        for (TwinLinkEntity updateTwinLinkEntity : twinLinkEntityList) {
            dbTwinLinkEntity = entitySmartService.findById(updateTwinLinkEntity.getId(), twinLinkRepository, EntitySmartService.FindMode.ifEmptyLogAndNull);
            if (dbTwinLinkEntity == null)
                continue;
            if (updateTwinLinkEntity.getSrcTwinId() != null && updateTwinLinkEntity.getDstTwinId() == null)
                updateTwinLinkEntity
                        .setDstTwinId(updateTwinLinkEntity.getSrcTwinId()) //shift
                        .setSrcTwinId(null);
            if (dbTwinLinkEntity.getSrcTwinId().equals(twinEntity.getId())) // forward link
                dbTwinLinkEntity
                        .setDstTwinId(updateTwinLinkEntity.getDstTwinId());
            else if (dbTwinLinkEntity.getDstTwinId().equals(twinEntity.getId())) { //backward link
                dbTwinLinkEntity
                        .setSrcTwinId(updateTwinLinkEntity.getDstTwinId());
            }
            if (validateEntityAndLog(dbTwinLinkEntity, EntitySmartService.EntityValidateMode.beforeSave))
                updatedTwinLinkEntityList.add(dbTwinLinkEntity);
        }
        entitySmartService.saveAllAndLog(updatedTwinLinkEntityList, twinLinkRepository);
    }

    public FindTwinLinksResult findTwinLinks(UUID twinId) throws ServiceException {
        List<TwinLinkEntity> twinLinkEntityList = twinLinkRepository.findBySrcTwinIdOrDstTwinId(twinId, twinId);
        FindTwinLinksResult linksResult = new FindTwinLinksResult();
        for (TwinLinkEntity twinLinkEntity : twinLinkEntityList) {
            if (twinLinkEntity.getSrcTwinId().equals(twinId)) {
                if (twinService.isEntityReadDenied(twinLinkEntity.getDstTwin(), EntitySmartService.ReadPermissionCheckMode.ifDeniedLog))
                    continue;
                linksResult.forwardLinks.put(twinLinkEntity.getId(), twinLinkEntity);
            } else if (twinLinkEntity.getDstTwinId().equals(twinId)) {
                if (twinService.isEntityReadDenied(twinLinkEntity.getSrcTwin(), EntitySmartService.ReadPermissionCheckMode.ifDeniedLog))
                    continue;
                linksResult.backwardLinks.put(twinLinkEntity.getId(), twinLinkEntity);
            } else
                log.warn(twinLinkEntity.logShort() + " is incorrect");
        }
        return linksResult;
    }

    public FindTwinLinksResult loadTwinLinks(TwinEntity twinEntity) throws ServiceException {
        if (twinEntity.getTwinLinks() != null)
            return twinEntity.getTwinLinks();
        twinEntity.setTwinLinks(findTwinLinks(twinEntity.getId()));
        return twinEntity.getTwinLinks();
    }

    public void loadTwinLinks(Collection<TwinEntity> twinEntityList) throws ServiceException {
        Map<UUID, TwinEntity> needLoad = new HashMap<>();
        for (TwinEntity twinEntity : twinEntityList)
            if (twinEntity.getTwinLinks() == null)
                needLoad.put(twinEntity.getId(), twinEntity);
        if (needLoad.size() == 0)
            return;
        List<TwinLinkEntity> twinLinkEntityList = twinLinkRepository.findBySrcTwinIdInOrDstTwinIdIn(needLoad.keySet(), needLoad.keySet());
        if (CollectionUtils.isEmpty(twinLinkEntityList))
            return;
        TwinEntity twinEntity = null;
        for (TwinLinkEntity twinLinkEntity : twinLinkEntityList) {
            if (needLoad.get(twinLinkEntity.getSrcTwinId()) != null) {
                if (twinService.isEntityReadDenied(twinLinkEntity.getDstTwin(), EntitySmartService.ReadPermissionCheckMode.ifDeniedLog))
                    continue;
                twinEntity = needLoad.get(twinLinkEntity.getSrcTwinId());
                if (twinEntity.getTwinLinks() == null)
                    twinEntity.setTwinLinks(new FindTwinLinksResult());
                twinEntity.getTwinLinks().forwardLinks.put(twinLinkEntity.getId(), twinLinkEntity);
            }
            if (needLoad.get(twinLinkEntity.getDstTwinId()) != null) {
                if (twinService.isEntityReadDenied(twinLinkEntity.getSrcTwin(), EntitySmartService.ReadPermissionCheckMode.ifDeniedLog))
                    continue;
                twinEntity = needLoad.get(twinLinkEntity.getDstTwinId());
                if (twinEntity.getTwinLinks() == null)
                    twinEntity.setTwinLinks(new FindTwinLinksResult());
                twinEntity.getTwinLinks().backwardLinks.put(twinLinkEntity.getId(), twinLinkEntity);
            }
        }
    }

    public List<TwinLinkEntity> findTwinForwardLinks(UUID twinId) throws ServiceException {
        List<TwinLinkEntity> twinLinkEntityList = twinLinkRepository.findBySrcTwinId(twinId, TwinLinkEntity.class);
        return filterDenied(twinLinkEntityList);
    }

    public List<TwinLinkEntity> findTwinForwardLinks(UUID twinId, Collection<UUID> linkIdCollection) throws ServiceException {
        List<TwinLinkEntity> twinLinkEntityList = twinLinkRepository.findBySrcTwinIdAndLinkIdIn(twinId, linkIdCollection, TwinLinkEntity.class);
        return filterDenied(twinLinkEntityList);
    }

    public List<TwinLinkEntity> findTwinBackwardLinks(UUID twinId) throws ServiceException {
        List<TwinLinkEntity> twinLinkEntityList = twinLinkRepository.findByDstTwinId(twinId, TwinLinkEntity.class);
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

    @Transactional
    public void deleteTwinLinks(UUID twinId, List<UUID> twinLinksDeleteUUIDList) throws ServiceException {
        if (CollectionUtils.isEmpty(twinLinksDeleteUUIDList))
            return;
        TwinLinkEntity twinLinkEntity;
        for (UUID twinLinkId : twinLinksDeleteUUIDList) {
            twinLinkEntity = findEntity(twinLinkId, EntitySmartService.FindMode.ifEmptyNull, EntitySmartService.ReadPermissionCheckMode.ifDeniedLog);
            if (twinLinkEntity == null)
                continue;
            if (!twinLinkEntity.getSrcTwinId().equals(twinId) && !twinLinkEntity.getDstTwinId().equals(twinId)) {
                log.error(twinLinkEntity.logShort() + " can not be delete because it's from other twin");
                continue;
            }
            if (twinLinkEntity.getLink().isMandatory()) {
                log.error(twinLinkEntity.logShort() + " can not be deleted because link is mandatory");
                continue;
            }
            entitySmartService.deleteAndLog(twinLinkId, twinLinkRepository);
        }
    }

    public List<TwinEntity> findValidDstTwins(LinkEntity linkEntity, TwinClassEntity srcTwinClass) throws ServiceException {
        if (linkService.isForwardLink(linkEntity, srcTwinClass)) {// forward link
            return twinSearchService.findTwins(new BasicSearch().addTwinClassId(twinClassService.loadChildClasses(linkEntity.getDstTwinClass())));
        } else if (linkService.isBackwardLink(linkEntity, srcTwinClass)) {// backward link
            return twinSearchService.findTwins(new BasicSearch().addTwinClassId(twinClassService.loadChildClasses(srcTwinClass)));
        } else
            return null;
    }

    public Long countValidDstTwins(LinkEntity linkEntity, TwinClassEntity srcTwinClass) throws ServiceException {
        if (linkService.isForwardLink(linkEntity, srcTwinClass)) {// forward link
            return twinSearchService.count(new BasicSearch().addTwinClassId(linkEntity.getDstTwinClassId()));
        } else if (linkService.isBackwardLink(linkEntity, srcTwinClass)) {// backward link
            return twinSearchService.count(new BasicSearch().addTwinClassId(linkEntity.getSrcTwinClassId()));
        } else
            return 0L;
    }

    public List<TwinLinkEntity> findTwinLinks(LinkEntity linkEntity, TwinEntity twinEntity, LinkService.LinkDirection linkDirection) throws ServiceException {
        if (linkDirection == null)
            linkDirection = linkService.detectLinkDirection(linkEntity, twinEntity.getTwinClass());
        switch (linkDirection) {
            case forward:
                List<TwinLinkEntity> twinLinkEntityList = twinLinkRepository.findBySrcTwinIdAndLinkId(twinEntity.getId(), linkEntity.getId(), TwinLinkEntity.class);
                return twinLinkEntityList;
            case backward:
                return twinLinkRepository.findByDstTwinIdAndLinkId(twinEntity.getId(), linkEntity.getId(), TwinLinkEntity.class);
            default:
                return null;
        }
    }

    @Data
    @Accessors(chain = true)
    public static class FindTwinLinksResult {
        UUID twinId;
        Map<UUID, TwinLinkEntity> forwardLinks = new LinkedHashMap<>();
        Map<UUID, TwinLinkEntity> backwardLinks = new LinkedHashMap<>();
    }
}
