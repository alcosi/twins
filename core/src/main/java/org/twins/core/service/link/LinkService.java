package org.twins.core.service.link;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkService extends EntitySecureFindServiceImpl<LinkEntity> {
    final LinkRepository linkRepository;
    final TwinClassService twinClassService;
    final TwinLinkRepository twinLinkRepository;
    @Lazy
    final AuthService authService;

    @Override
    public String entityName() {
        return "link";
    }

    @Override
    public CrudRepository<LinkEntity, UUID> entityRepository() {
        return linkRepository;
    }

    @Override
    public boolean isEntityReadDenied(LinkEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allowed in domain[" + apiUser.getDomain().logShort());
            return true;
        }
        //todo check permission schema
        return false;
    }

    public FindTwinClassLinksResult findLinks(UUID twinClassId) throws ServiceException {
        TwinClassEntity twinClassEntity = twinClassService.findEntity(twinClassId, EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
        return findLinks(twinClassEntity);
    }

    public FindTwinClassLinksResult findLinks(TwinClassEntity twinClassEntity) throws ServiceException {
        List<UUID> extendedTwinClasses = twinClassService.findExtendedClasses(twinClassEntity, true);
        List<LinkEntity> linksEntityList = linkRepository.findBySrcTwinClassIdInOrDstTwinClassIdIn(extendedTwinClasses, extendedTwinClasses);
        FindTwinClassLinksResult linksResult = new FindTwinClassLinksResult();
        for (LinkEntity linkEntity : linksEntityList) {
            if (linkEntity.getSrcTwinClassId().equals(twinClassEntity.getId())) {
                if (twinClassService.isEntityReadDenied(linkEntity.getDstTwinClass(), EntitySmartService.ReadPermissionCheckMode.ifDeniedLog))
                    continue;
                linksResult.forwardLinks.put(linkEntity.getId() ,linkEntity);
            } else if (linkEntity.getDstTwinClassId().equals(twinClassEntity.getId())) {
                if (twinClassService.isEntityReadDenied(linkEntity.getSrcTwinClass(), EntitySmartService.ReadPermissionCheckMode.ifDeniedLog))
                    continue;
                linksResult.backwardLinks.put(linkEntity.getId() ,linkEntity);
            } else
                log.warn(linkEntity.logShort() + " is incorrect");
        }
        return linksResult;
    }

    public void addLinks(TwinEntity srcTwinEntity, List<TwinLinkEntity> linksEntityList) throws ServiceException {
        FindTwinClassLinksResult findTwinClassLinksResult = findLinks(srcTwinEntity.getTwinClass());
        TwinEntity dstTwinEntity;
        for (TwinLinkEntity twinLinkEntity : linksEntityList) {
            LinkEntity linkEntity = findEntity(twinLinkEntity.getLinkId(), EntitySmartService.FindMode.ifEmptyNull, EntitySmartService.ReadPermissionCheckMode.ifDeniedLog);
            if (linkEntity == null)
                continue;
            if (linkEntity.getSrcTwinClassId().equals(srcTwinEntity.getTwinClassId())) {

            }
        }
    }



    @Data
    @Accessors(chain = true)
    public static class FindTwinClassLinksResult {
        UUID twinClassId;
        Map<UUID, LinkEntity> forwardLinks = new LinkedHashMap<>();
        Map<UUID, LinkEntity> backwardLinks = new LinkedHashMap<>();
    }
}
