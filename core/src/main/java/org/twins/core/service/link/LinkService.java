package org.twins.core.service.link;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.MapUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.i18n.dao.I18nEntity;
import org.cambium.i18n.dao.I18nType;
import org.cambium.i18n.service.I18nService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkRepository;
import org.twins.core.dao.link.LinkStrength;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.EntityRelinkOperation;
import org.twins.core.domain.LinkUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
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
        if (null == entity.getSrcTwinClassId() || null == entity.getDstTwinClassId())
            return logErrorAndReturnFalse(ErrorCodeTwins.LINK_DIRECTION_CLASS_NULL.getMessage());
        switch (entityValidateMode) {
            case beforeSave:
                LinkEntity existsLink = linkRepository.findBySrcTwinClassIdAndDstTwinClassId(entity.getSrcTwinClassId(), entity.getDstTwinClassId());
                if (null != existsLink && (entity.getId() == null || !entity.getId().equals(existsLink.getId())))
                    return logErrorAndReturnFalse(ErrorCodeTwins.LINK_ALREADY_EXIST.getMessage());
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

    @Transactional(rollbackFor = Throwable.class)
    public LinkEntity createLink(LinkEntity linkEntity, I18nEntity forwardNameI18n, I18nEntity backwardNameI18n) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        linkEntity
                .setDomainId(apiUser.getDomainId())
                .setForwardNameI18NId(i18nService.createI18nAndTranslations(I18nType.LINK_FORWARD_NAME, forwardNameI18n).getId())
                .setBackwardNameI18NId(i18nService.createI18nAndTranslations(I18nType.LINK_BACKWARD_NAME, backwardNameI18n).getId())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(apiUser.getUserId());
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
        updateLinkForwardName(dbLinkEntity, forwardNameI18n, changesHelper);
        updateLinkBackwardName(dbLinkEntity, backwardNameI18n, changesHelper);
        updateLinkSrcTwinClassId(dbLinkEntity, linkUpdate.getSrcTwinClassUpdate(), changesHelper);
        updateLinkDstTwinClassId(dbLinkEntity, linkUpdate.getDstTwinClassUpdate(), changesHelper);
        updateLinkType(dbLinkEntity, linkUpdate.getType(), changesHelper);
        updateLinkStrength(dbLinkEntity, linkUpdate.getLinkStrengthId(), changesHelper);
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
        }
        return dbLinkEntity;
    }

    public void updateLinkForwardName(LinkEntity dbLinkEntity, I18nEntity forwardNameI18n, ChangesHelper changesHelper) throws ServiceException {
        if (forwardNameI18n == null)
            return;
        if (dbLinkEntity.getForwardNameI18NId() != null)
            forwardNameI18n.setId(dbLinkEntity.getForwardNameI18NId());
        i18nService.saveTranslations(I18nType.LINK_FORWARD_NAME, forwardNameI18n);
        if (changesHelper.isChanged(LinkEntity.Fields.forwardNameI18NId, dbLinkEntity.getForwardNameI18NId(), forwardNameI18n.getId()))
            dbLinkEntity.setForwardNameI18NId(forwardNameI18n.getId());
    }

    public void updateLinkBackwardName(LinkEntity dbLinkEntity, I18nEntity backwardNameI18n, ChangesHelper changesHelper) throws ServiceException {
        if (backwardNameI18n == null)
            return;
        if (dbLinkEntity.getBackwardNameI18NId() != null)
            backwardNameI18n.setId(dbLinkEntity.getBackwardNameI18NId());
        i18nService.saveTranslations(I18nType.LINK_FORWARD_NAME, backwardNameI18n);
        if (changesHelper.isChanged(LinkEntity.Fields.backwardNameI18NId, dbLinkEntity.getBackwardNameI18NId(), backwardNameI18n.getId()))
            dbLinkEntity.setBackwardNameI18NId(backwardNameI18n.getId());
    }

    public void updateLinkDstTwinClassId(LinkEntity dbLinkEntity, EntityRelinkOperation linkDstClassChangeOperation, ChangesHelper changesHelper) throws ServiceException {
//        if (dstTwinClassId == null || !changesHelper.isChanged(LinkEntity.Fields.dstTwinClassId, dbLinkEntity.getDstTwinClassId(), dstTwinClassId))
//            return;
//        dbLinkEntity.setDstTwinClassId(dstTwinClassId);
    }

    public void updateLinkSrcTwinClassId(LinkEntity dbLinkEntity, EntityRelinkOperation linkSrcClassChangeOperation, ChangesHelper changesHelper) throws ServiceException {
        if (linkSrcClassChangeOperation == null || !changesHelper.isChanged(LinkEntity.Fields.srcTwinClassId, dbLinkEntity.getSrcTwinClassId(), linkSrcClassChangeOperation.getNewId()))
            return;
        TwinClassEntity newSrcTwinClassEntity = UuidUtils.isNullifyMarker(linkSrcClassChangeOperation.getNewId()) ? null : twinClassService.findEntitySafe(linkSrcClassChangeOperation.getNewId());
        if (newSrcTwinClassEntity == null)
            throw new ServiceException(ErrorCodeTwins.LINK_DIRECTION_CLASS_NULL);
        Kit<TwinLinkEntity, UUID> existedTwinLinksKit = new Kit<>(twinLinkService.findTwinLinks(dbLinkEntity.getId(), linkSrcClassChangeOperation.getReplaceMap().keySet(), LinkDirection.forward), TwinLinkEntity::getId);
        if (!CollectionUtils.isEmpty(existedTwinLinksKit.getCollection())) {
            if (linkSrcClassChangeOperation.getStrategy() == EntityRelinkOperation.Strategy.restrict && MapUtils.isEmpty(linkSrcClassChangeOperation.getReplaceMap()))
                throw new ServiceException(ErrorCodeTwins.LINK_UPDATE_RESTRICTED, "please provide replaceMap for twin-links: " + StringUtils.join(existedTwinLinksKit.getIdSet()));
            Set<TwinLinkEntity> twinLinksForDeletion = new HashSet<>();
            Set<TwinLinkEntity> twinLinksReplaced = new HashSet<>();
            Kit<TwinEntity, UUID> newTwinEntitiesKit = new Kit<>(twinRepository.findAllByIdIn(linkSrcClassChangeOperation.getReplaceMap().values()), TwinEntity::getId);
            for (TwinLinkEntity twinLinkForReplace : existedTwinLinksKit.getCollection()) {
                UUID replacement = linkSrcClassChangeOperation.getReplaceMap().get(twinLinkForReplace.getId());
                if (replacement == null) {
                    if (linkSrcClassChangeOperation.getStrategy() == EntityRelinkOperation.Strategy.restrict)
                        throw new ServiceException(ErrorCodeTwins.LINK_UPDATE_RESTRICTED, "please provide replaceMap value for twink-links: " + twinLinkForReplace.getId());
                    else
                        twinLinksForDeletion.add(twinLinkForReplace);
                } else {
                    TwinEntity newTwinEntity = newTwinEntitiesKit.get(replacement);
                    if (newTwinEntity == null || !newTwinEntity.getTwinClassId().equals(newSrcTwinClassEntity.getId()))
                        throw new ServiceException(ErrorCodeTwins.LINK_UPDATE_RESTRICTED, "please provide correct replaceMap value for twin-link: " + twinLinkForReplace.getId());
                }
                if (UuidUtils.isNullifyMarker(replacement)) continue;
                twinLinksReplaced.add(twinLinkForReplace.setSrcTwinId(replacement));
            }
            if (CollectionUtils.isNotEmpty(twinLinksForDeletion)) {
                //todo support deletion
                throw new ServiceException(ErrorCodeTwins.LINK_UPDATE_RESTRICTED, "twin-link auto deletion is currently not implemented. please provide headReplaceMap value for twin-links: " + StringUtils.join(new Kit<>(twinLinksForDeletion, TwinLinkEntity::getId).getIdSet()));
            }
            if (CollectionUtils.isNotEmpty(twinLinksReplaced))
                twinLinkService.saveAll(twinLinksReplaced);
        }
        dbLinkEntity.setSrcTwinClassId(newSrcTwinClassEntity.getId());
        dbLinkEntity.setSrcTwinClass(newSrcTwinClassEntity);
    }

    private void updateLinkStrength(LinkEntity dbLinkEntity, LinkStrength linkStrengthId, ChangesHelper changesHelper) throws ServiceException {
        if (linkStrengthId == null || !changesHelper.isChanged(LinkEntity.Fields.linkStrengthId, dbLinkEntity.getLinkStrengthId(), linkStrengthId))
            return;
        dbLinkEntity.setLinkStrengthId(linkStrengthId);
    }

    private void updateLinkType(LinkEntity dbLinkEntity, LinkEntity.TwinlinkType type, ChangesHelper changesHelper) {
        if (type == null || !changesHelper.isChanged(LinkEntity.Fields.type, dbLinkEntity.getType(), type))
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
}
