package org.twins.core.service.link;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.*;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkRepository;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.EntityRelinkOperation;
import org.twins.core.domain.LinkUpdate;
import org.twins.core.enums.EntityRelinkOperationStrategy;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.link.LinkType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.linker.Linker;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.user.UserService;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class LinkService extends EntitySecureFindServiceImpl<LinkEntity> {
    private final LinkRepository linkRepository;
    private final TwinClassService twinClassService;
    private final UserService userService;
    private final EntitySmartService entitySmartService;
    private final I18nService i18nService;

    @Lazy
    private final TwinLinkService twinLinkService;
    @Lazy
    private final TwinRepository twinRepository;
    @Lazy
    private final AuthService authService;
    private final FeaturerService featurerService;
    private final TwinLinkRepository twinLinkRepository;
    private final CacheManager cacheManager;

    @Override
    public CrudRepository<LinkEntity, UUID> entityRepository() {
        return linkRepository;
    }

    @Override
    public Function<LinkEntity, UUID> entityGetIdFunction() {
        return LinkEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(LinkEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in " + apiUser.getDomain().logNormal());
            return true;
        }
        //todo check permission schema
        return false;
    }

    @Override
    public boolean validateEntity(LinkEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (null == entity.getSrcTwinClassId() || null == entity.getDstTwinClassId())
            return logErrorAndReturnFalse(ErrorCodeTwins.LINK_DIRECTION_CLASS_NULL.getMessage());
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getSrcTwinClass() == null || !entity.getSrcTwinClass().getId().equals(entity.getSrcTwinClassId()))
                    entity.setSrcTwinClass(twinClassService.findEntitySafe(entity.getSrcTwinClassId()));
                if (entity.getDstTwinClass() == null || !entity.getDstTwinClass().getId().equals(entity.getDstTwinClassId()))
                    entity.setDstTwinClass(twinClassService.findEntitySafe(entity.getDstTwinClassId()));
                if (entity.getCreatedByUser() == null)
                    entity.setCreatedByUser(userService.findEntitySafe(entity.getCreatedByUserId()));
            default:
                if (!entity.getDstTwinClass().getDomainId().equals(entity.getDomainId()) || !entity.getSrcTwinClass().getDomainId().equals(entity.getDomainId()))
                    return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " incompatible source/destination class [" + entity.getSrcTwinClass().easyLog(EasyLoggable.Level.DETAILED) + " > " + entity.getDstTwinClass().easyLog(EasyLoggable.Level.DETAILED) + "]");
        }
        return true;
    }


    public static final String CACHE_LINK = "LinkService.findLinkByIdCached";
    @Cacheable(value = CACHE_LINK, key = "#linkId")
    public LinkEntity findLinkByIdCached(UUID linkId) throws ServiceException {
        return findEntity(linkId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
    }

    @Transactional(rollbackFor = Throwable.class)
    public LinkEntity createLink(LinkEntity linkEntity, I18nEntity forwardNameI18n, I18nEntity backwardNameI18n) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        linkEntity
                .setDomainId(apiUser.getDomainId())
                .setForwardNameI18NId(i18nService.createI18nAndTranslations(I18nType.LINK_FORWARD_NAME, forwardNameI18n).getId())
                .setBackwardNameI18NId(i18nService.createI18nAndTranslations(I18nType.LINK_BACKWARD_NAME, backwardNameI18n).getId())
                .setCreatedByUserId(apiUser.getUserId());
        if (linkEntity.getLinkerFeaturerId() == null) {
            linkEntity
                    .setLinkerFeaturerId(FeaturerTwins.ID_3001)
                    .setLinkerParams(null);
        }
        //todo validate linker params
        validateEntityAndThrow(linkEntity, EntitySmartService.EntityValidateMode.beforeSave);
        linkEntity = entitySmartService.save(linkEntity, linkRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        linkEntity.getDstTwinClass().setLinksKit(null);
        linkEntity.getSrcTwinClass().setLinksKit(null);
        return linkEntity;
    }

    @Transactional(rollbackFor = Throwable.class)
    public LinkEntity updateLink(LinkUpdate linkUpdate, I18nEntity forwardNameI18n, I18nEntity backwardNameI18n) throws ServiceException {
        LinkEntity dbLinkEntity = findEntitySafe(linkUpdate.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        //for future old classes kit nullify
        linkUpdate.setDstTwinClass(dbLinkEntity.getDstTwinClass());
        linkUpdate.setSrcTwinClass(dbLinkEntity.getSrcTwinClass());
        i18nService.updateI18nFieldForEntity(forwardNameI18n, I18nType.LINK_FORWARD_NAME, dbLinkEntity, LinkEntity::getForwardNameI18NId, LinkEntity::setForwardNameI18NId, LinkEntity.Fields.forwardNameI18NId, changesHelper);
        i18nService.updateI18nFieldForEntity(backwardNameI18n, I18nType.LINK_BACKWARD_NAME, dbLinkEntity, LinkEntity::getBackwardNameI18NId, LinkEntity::setBackwardNameI18NId, LinkEntity.Fields.backwardNameI18NId, changesHelper);
        updateLinkSrcTwinClassId(dbLinkEntity, linkUpdate.getSrcTwinClassUpdate(), changesHelper);
        updateLinkDstTwinClassId(dbLinkEntity, linkUpdate.getDstTwinClassUpdate(), changesHelper);
        updateLinkType(dbLinkEntity, linkUpdate.getType(), changesHelper);
        updateLinkStrength(dbLinkEntity, linkUpdate.getLinkStrengthId(), changesHelper);
        updateLinkerFeaturer(dbLinkEntity, linkUpdate.getLinkerFeaturerId(), linkUpdate.getLinkerParams(), changesHelper);
        validateEntity(dbLinkEntity, EntitySmartService.EntityValidateMode.beforeSave);
        if (changesHelper.hasChanges()) {
            dbLinkEntity = entitySmartService.saveAndLogChanges(dbLinkEntity, linkRepository, changesHelper);
            if (changesHelper.hasChange(LinkEntity.Fields.dstTwinClassId)) {
                dbLinkEntity.getDstTwinClass().setLinksKit(null);
                linkUpdate.getDstTwinClass().setLinksKit(null);
            }
            if (changesHelper.hasChange(LinkEntity.Fields.srcTwinClassId)) {
                dbLinkEntity.getSrcTwinClass().setLinksKit(null);
                linkUpdate.getSrcTwinClass().setLinksKit(null);
            }
            CacheUtils.evictCache(cacheManager,CACHE_LINK, dbLinkEntity.getId());
        }
        return dbLinkEntity;
    }

    public void updateLinkDstTwinClassId(LinkEntity dbLinkEntity, EntityRelinkOperation linkDstClassChangeOperation, ChangesHelper changesHelper) throws ServiceException {
        if (linkDstClassChangeOperation == null || !changesHelper.isChanged(LinkEntity.Fields.dstTwinClassId, dbLinkEntity.getDstTwinClassId(), linkDstClassChangeOperation.getNewId()))
            return;
        TwinClassEntity newDstTwinClassEntity = UuidUtils.isNullifyMarker(linkDstClassChangeOperation.getNewId()) ? null : twinClassService.findEntitySafe(linkDstClassChangeOperation.getNewId());
        if (newDstTwinClassEntity == null)
            throw new ServiceException(ErrorCodeTwins.LINK_DIRECTION_CLASS_NULL);
        Set<UUID> existedDstTwinIds = twinLinkService.findDstTwinIdsByLinkId(dbLinkEntity.getId());
        if (!CollectionUtils.isEmpty(existedDstTwinIds)) {
            if (linkDstClassChangeOperation.getStrategy() == EntityRelinkOperationStrategy.restrict && MapUtils.isEmpty(linkDstClassChangeOperation.getReplaceMap()))
                throw new ServiceException(ErrorCodeTwins.LINK_UPDATE_RESTRICTED, "please provide replaceMap for twin-links: " + StringUtils.join(existedDstTwinIds));
            Set<UUID> newValidTwinIds = twinRepository.findIdByTwinClassIdAndIdIn(newDstTwinClassEntity.getId(), linkDstClassChangeOperation.getReplaceMap().values());
            Set<UUID> dstTwinIdsTwinLinksForDeletion = new HashSet<>();
            for (UUID dstTwinIdForReplace : existedDstTwinIds) {
                UUID replacement = linkDstClassChangeOperation.getReplaceMap().get(dstTwinIdForReplace);
                if (replacement == null) {
                    if (linkDstClassChangeOperation.getStrategy() == EntityRelinkOperationStrategy.restrict)
                        throw new ServiceException(ErrorCodeTwins.LINK_UPDATE_RESTRICTED, "please provide replaceMap value for twink-links: " + dstTwinIdForReplace);
                    else
                        replacement = UuidUtils.NULLIFY_MARKER;
                }
                if (UuidUtils.isNullifyMarker(replacement)) {
                    dstTwinIdsTwinLinksForDeletion.add(dstTwinIdForReplace);
                    continue;
                }
                if (!newValidTwinIds.contains(replacement))
                    throw new ServiceException(ErrorCodeTwins.LINK_UPDATE_RESTRICTED, "please provide correct headReplaceMap value for twink-link: " + dstTwinIdForReplace);
                twinLinkRepository.replaceDstTwinIdForTwinLinkByLinkId(dbLinkEntity.getId(), dstTwinIdForReplace, replacement);
            }
            if (CollectionUtils.isNotEmpty(dstTwinIdsTwinLinksForDeletion)) {
                //todo support deletion
                throw new ServiceException(ErrorCodeTwins.LINK_UPDATE_RESTRICTED, "twin-link auto deletion is currently not implemented. please provide headReplaceMap value for twin-links: " + StringUtils.join(dstTwinIdsTwinLinksForDeletion));
            }
        }
        dbLinkEntity.setDstTwinClassId(newDstTwinClassEntity.getId());
        dbLinkEntity.setDstTwinClass(newDstTwinClassEntity);

    }

    public void updateLinkSrcTwinClassId(LinkEntity dbLinkEntity, EntityRelinkOperation linkSrcClassChangeOperation, ChangesHelper changesHelper) throws ServiceException {
        if (linkSrcClassChangeOperation == null || !changesHelper.isChanged(LinkEntity.Fields.srcTwinClassId, dbLinkEntity.getSrcTwinClassId(), linkSrcClassChangeOperation.getNewId()))
            return;
        TwinClassEntity newSrcTwinClassEntity = UuidUtils.isNullifyMarker(linkSrcClassChangeOperation.getNewId()) ? null : twinClassService.findEntitySafe(linkSrcClassChangeOperation.getNewId());
        if (newSrcTwinClassEntity == null)
            throw new ServiceException(ErrorCodeTwins.LINK_DIRECTION_CLASS_NULL);
        Set<UUID> existedSrcTwinIds = twinLinkService.findSrcTwinIdsByLinkId(dbLinkEntity.getId());
        if (!CollectionUtils.isEmpty(existedSrcTwinIds)) {
            if (linkSrcClassChangeOperation.getStrategy() == EntityRelinkOperationStrategy.restrict && MapUtils.isEmpty(linkSrcClassChangeOperation.getReplaceMap()))
                throw new ServiceException(ErrorCodeTwins.LINK_UPDATE_RESTRICTED, "please provide replaceMap for twin-links: " + StringUtils.join(existedSrcTwinIds));
            Set<UUID> newValidTwinIds = MapUtils.isEmpty(linkSrcClassChangeOperation.getReplaceMap()) ?
                    Collections.emptySet() : twinRepository.findIdByTwinClassIdAndIdIn(newSrcTwinClassEntity.getId(), linkSrcClassChangeOperation.getReplaceMap().values());
            Set<UUID> srcTwinIdsTwinLinksForDeletion = new HashSet<>();
            for (UUID srcTwinIdForReplace : existedSrcTwinIds) {
                UUID replacement = linkSrcClassChangeOperation.getReplaceMap().get(srcTwinIdForReplace);
                if (replacement == null) {
                    if (linkSrcClassChangeOperation.getStrategy() == EntityRelinkOperationStrategy.restrict)
                        throw new ServiceException(ErrorCodeTwins.LINK_UPDATE_RESTRICTED, "please provide replaceMap value for twink-links: " + srcTwinIdForReplace);
                    else
                        replacement = UuidUtils.NULLIFY_MARKER;
                }
                if (UuidUtils.isNullifyMarker(replacement)) {
                    srcTwinIdsTwinLinksForDeletion.add(srcTwinIdForReplace);
                    continue;
                }
                if (!newValidTwinIds.contains(replacement))
                    throw new ServiceException(ErrorCodeTwins.LINK_UPDATE_RESTRICTED, "please provide correct headReplaceMap value for twink-link: " + srcTwinIdForReplace);
                twinLinkRepository.replaceSrcTwinIdForTwinLinkByLinkId(dbLinkEntity.getId(), srcTwinIdForReplace, replacement);
            }
            if (CollectionUtils.isNotEmpty(srcTwinIdsTwinLinksForDeletion)) {
                //todo support deletion
                throw new ServiceException(ErrorCodeTwins.LINK_UPDATE_RESTRICTED, "twin-link auto deletion is currently not implemented. please provide headReplaceMap value for twin-links: " + StringUtils.join(srcTwinIdsTwinLinksForDeletion));
            }
        }
        dbLinkEntity.setSrcTwinClassId(newSrcTwinClassEntity.getId());
        dbLinkEntity.setSrcTwinClass(newSrcTwinClassEntity);
    }

    private void updateLinkStrength(LinkEntity dbLinkEntity, LinkStrength linkStrengthId, ChangesHelper changesHelper) throws ServiceException {
        if (linkStrengthId == null || !changesHelper.isChanged(LinkEntity.Fields.linkStrengthId, dbLinkEntity.getLinkStrengthId(), linkStrengthId))
            return;
        dbLinkEntity.setLinkStrengthId(linkStrengthId);
    }

    private void updateLinkType(LinkEntity dbLinkEntity, LinkType type, ChangesHelper changesHelper) {
        if (type == null || !changesHelper.isChanged(LinkEntity.Fields.type, dbLinkEntity.getType(), type))
            return;
        dbLinkEntity.setType(type);
    }

    public void updateLinkerFeaturer(LinkEntity dbLinkEntity, Integer newHeadhunterFeaturerId, HashMap<String, String> linkerParams, ChangesHelper changesHelper) throws ServiceException {
        if (changesHelper.isChanged(LinkEntity.Fields.linkerFeaturerId, dbLinkEntity.getLinkerFeaturerId(), newHeadhunterFeaturerId)) {
            FeaturerEntity newLinkerFeaturer = featurerService.checkValid(newHeadhunterFeaturerId, linkerParams, Linker.class);
            dbLinkEntity
                    .setLinkerFeaturerId(newLinkerFeaturer.getId())
                    .setLinkerFeaturer(newLinkerFeaturer);
        }
        featurerService.prepareForStore(newHeadhunterFeaturerId, linkerParams);
        if (!MapUtils.areEqual(dbLinkEntity.getLinkerParams(), linkerParams)) {
            changesHelper.add(TwinClassEntity.Fields.headHunterParams, dbLinkEntity.getLinkerParams(), linkerParams);
            dbLinkEntity
                    .setLinkerParams(linkerParams);
        }
    }

    public FindTwinClassLinksResult findLinks(UUID twinClassId) throws ServiceException {
        TwinClassEntity twinClassEntity = twinClassService.findEntity(twinClassId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
        return findLinks(twinClassEntity);
    }

    public FindTwinClassLinksResult findLinks(TwinClassEntity twinClassEntity) throws ServiceException {
        loadLinks(twinClassEntity);
        Set<UUID> extendedTwinClasses = twinClassEntity.getExtendedClassIdSet();
        FindTwinClassLinksResult linksResult = new FindTwinClassLinksResult();
        for (LinkEntity linkEntity : twinClassEntity.getLinksKit().getCollection()) {
            if (extendedTwinClasses.contains(linkEntity.getSrcTwinClassId())) {
                if (twinClassService.isEntityReadDenied(linkEntity.getDstTwinClass(), EntitySmartService.ReadPermissionCheckMode.ifDeniedLog))
                    continue;
                linksResult.forwardLinks.put(linkEntity.getId(), linkEntity);
            } else if (extendedTwinClasses.contains(linkEntity.getDstTwinClassId())) {
                if (twinClassService.isEntityReadDenied(linkEntity.getSrcTwinClass(), EntitySmartService.ReadPermissionCheckMode.ifDeniedLog))
                    continue;
                linksResult.backwardLinks.put(linkEntity.getId(), linkEntity);
            } else
                log.warn(linkEntity.easyLog(EasyLoggable.Level.NORMAL) + " is incorrect");
        }
        return linksResult;
    }

    public void loadLinksForTwinClasses(List<TwinClassEntity> twinClassEntities) {
        for (TwinClassEntity twinClass : twinClassEntities)
            loadLinks(twinClass);
    }

    public Kit<LinkEntity, UUID> loadLinks(TwinClassEntity twinClassEntity) {
        if (twinClassEntity.getLinksKit() != null)
            return twinClassEntity.getLinksKit();
        Set<UUID> extendedTwinClasses = twinClassEntity.getExtendedClassIdSet();
        twinClassEntity.setLinksKit(new Kit<>(linkRepository.findBySrcTwinClassIdInOrDstTwinClassIdIn(extendedTwinClasses, extendedTwinClasses), LinkEntity::getId));
        return twinClassEntity.getLinksKit();
    }

    public boolean isForwardLink(LinkEntity linkEntity, TwinClassEntity twinClassEntity) throws ServiceException {
        return twinClassService.isInstanceOf(twinClassEntity, linkEntity.getSrcTwinClassId());
    }

    public boolean isBackwardLink(LinkEntity linkEntity, TwinClassEntity twinClassEntity) throws ServiceException {
        return twinClassService.isInstanceOf(twinClassEntity, linkEntity.getDstTwinClassId());
    }

    public LinkDirection detectLinkDirection(LinkEntity linkEntity, TwinClassEntity twinClassEntity) throws ServiceException {
        if (isForwardLink(linkEntity, twinClassEntity))
            return LinkDirection.forward;
        else if (isBackwardLink(linkEntity, twinClassEntity))
            return LinkDirection.backward;
        else
            return LinkDirection.invalid;
    }

    public List<LinkEntity> findLinks(TwinClassEntity srcTwinClass, TwinClassEntity dstTwinClass) {
        return linkRepository.findBySrcTwinClassIdInOrDstTwinClassIdIn(srcTwinClass.getExtendedClassIdSet(), dstTwinClass.getExtendedClassIdSet());
    }

    public enum LinkDirection {
        forward,
        backward,
        invalid,
    }

    @Data
    @Accessors(chain = true)
    public static class FindTwinClassLinksResult {
        UUID twinClassId;
        Map<UUID, LinkEntity> forwardLinks = new LinkedHashMap<>();
        Map<UUID, LinkEntity> backwardLinks = new LinkedHashMap<>();
    }

    public Collection<LinkEntity> findAllByIdIn(Collection<UUID> ids) {
        return linkRepository.findAllByIdIn(ids);
    }
}
