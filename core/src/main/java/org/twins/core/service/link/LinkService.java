package org.twins.core.service.link;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.dao.I18nType;
import org.cambium.i18n.service.I18nService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkRepository;
import org.twins.core.dao.link.LinkStrength;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.cambium.common.util.CacheUtils.evictCache;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class LinkService extends EntitySecureFindServiceImpl<LinkEntity> {
    private final LinkRepository linkRepository;
    private final TwinClassService twinClassService;
    @Lazy
    private final AuthService authService;
    private final EntitySmartService entitySmartService;
    private final I18nService i18nService;

    @Autowired
    private CacheManager cacheManager;

    @Override
    public CrudRepository<LinkEntity, UUID> entityRepository() {
        return linkRepository;
    }

    @Override
    public boolean isEntityReadDenied(LinkEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + apiUser.getDomain().easyLog(EasyLoggable.Level.NORMAL));
            return true;
        }
        //todo check permission schema
        return false;
    }

    @Override
    public boolean validateEntity(LinkEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional
    public LinkEntity createLink(LinkEntity linkEntity, I18nEntity forwardNameI18n, I18nEntity backwardNameI18n) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        //TODO any additional checks for classes?
        if(null == linkEntity.getSrcTwinClassId() || null == linkEntity.getDstTwinClassId())
            throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT);
        linkEntity
                .setDomainId(apiUser.getDomainId())
                .setForwardNameI18NId(i18nService.createI18nAndTranslations(I18nType.LINK_FORWARD_NAME, forwardNameI18n).getId())
                .setBackwardNameI18NId(i18nService.createI18nAndTranslations(I18nType.LINK_BACKWARD_NAME, backwardNameI18n).getId())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(apiUser.getUserId());
        //TODO in all services(create... update...) dont update objects(classes & user) in link.
        return entitySmartService.save(linkEntity, linkRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
    }

    @Transactional
    public LinkEntity updateLink(LinkEntity linkEntity, I18nEntity forwardNameI18n, I18nEntity backwardNameI18n) throws ServiceException {
        LinkEntity dbLinkEntity = findEntitySafe(linkEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateLinkForwardName(dbLinkEntity, forwardNameI18n, changesHelper);
        updateLinkBackwardName(dbLinkEntity, backwardNameI18n, changesHelper);
        //TODO any additional checks for classes?
        updateLinkSrcTwinClassId(dbLinkEntity, linkEntity.getSrcTwinClassId(), changesHelper);
        updateLinkDstTwinClassId(dbLinkEntity, linkEntity.getDstTwinClassId(), changesHelper);
        updateLinkType(dbLinkEntity, linkEntity.getType(), changesHelper);
        updateLinkStrength(dbLinkEntity, linkEntity.getLinkStrengthId(), changesHelper);
        return entitySmartService.saveAndLogChanges(dbLinkEntity, linkRepository, changesHelper);
    }

    @Transactional
    public void updateLinkForwardName(LinkEntity dbLinkEntity, I18nEntity forwardNameI18n, ChangesHelper changesHelper) throws ServiceException {
        if (forwardNameI18n == null)
            return;
        if (dbLinkEntity.getForwardNameI18NId() != null)
            forwardNameI18n.setId(dbLinkEntity.getForwardNameI18NId());
        i18nService.saveTranslations(I18nType.LINK_FORWARD_NAME, forwardNameI18n);
        dbLinkEntity.setForwardNameI18NId(forwardNameI18n.getId()); //TODO in all services(TwinClassService) it will not update if another changes absent. Not collected to changesHelper
    }

    @Transactional
    public void updateLinkBackwardName(LinkEntity dbLinkEntity, I18nEntity backwardNameI18n, ChangesHelper changesHelper) throws ServiceException {
        if (backwardNameI18n == null)
            return;
        if (dbLinkEntity.getBackwardNameI18NId() != null)
            backwardNameI18n.setId(dbLinkEntity.getBackwardNameI18NId());
        i18nService.saveTranslations(I18nType.LINK_FORWARD_NAME, backwardNameI18n);
        dbLinkEntity.setBackwardNameI18NId(backwardNameI18n.getId()); //TODO in all service it will not update if another changes absent.
    }

    @Transactional
    public void updateLinkDstTwinClassId(LinkEntity dbLinkEntity, UUID dstTwinClassId, ChangesHelper changesHelper) throws ServiceException {
        if (dstTwinClassId == null || !changesHelper.isChanged("dstTwinClassId", dbLinkEntity.getDstTwinClassId(), dstTwinClassId))
            return;
        evictCache(cacheManager, TwinClassRepository.CACHE_TWIN_CLASS_BY_ID, dbLinkEntity.getDstTwinClassId());
        dbLinkEntity.setDstTwinClassId(dstTwinClassId);
    }

    @Transactional
    public void updateLinkSrcTwinClassId(LinkEntity dbLinkEntity, UUID srcTwinClassId, ChangesHelper changesHelper) throws ServiceException {
        if (srcTwinClassId == null || !changesHelper.isChanged("srcTwinClassId", dbLinkEntity.getSrcTwinClassId(), srcTwinClassId))
            return;
        evictCache(cacheManager, TwinClassRepository.CACHE_TWIN_CLASS_BY_ID, dbLinkEntity.getSrcTwinClassId());
        dbLinkEntity.setSrcTwinClassId(srcTwinClassId);
    }

    private void updateLinkStrength(LinkEntity dbLinkEntity, LinkStrength linkStrengthId, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("linkStrengthId", dbLinkEntity.getLinkStrengthId(), linkStrengthId))
            return;
        dbLinkEntity.setLinkStrengthId(linkStrengthId);
    }

    private void updateLinkType(LinkEntity dbLinkEntity, LinkEntity.TwinlinkType type, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged("type", dbLinkEntity.getType(), type))
            return;
        dbLinkEntity.setType(type);
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

    public Kit<LinkEntity, UUID> loadLinks(TwinClassEntity twinClassEntity){
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
}
