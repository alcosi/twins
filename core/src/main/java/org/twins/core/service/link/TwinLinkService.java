package org.twins.core.service.link;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySecureFindServiceImpl;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;
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

    public void addLinks(TwinEntity srcTwinEntity, List<TwinLinkEntity> linksEntityList) throws ServiceException {
        for (TwinLinkEntity twinLinkEntity : linksEntityList) {
            LinkEntity linkEntity = linkService.findEntity(twinLinkEntity.getLinkId(), EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows);
            if (twinLinkEntity.getDstTwin() == null)
                twinLinkEntity.setDstTwin(twinService.findEntity(twinLinkEntity.getDstTwinId(), EntitySmartService.FindMode.ifEmptyThrows, EntitySmartService.ReadPermissionCheckMode.ifDeniedThrows));
            Set<UUID> srcTwinExtendedClasses = twinClassService.findExtendedClasses(srcTwinEntity.getTwinClass(), true);
            Set<UUID> dstTwinExtendedClasses = twinClassService.findExtendedClasses(twinLinkEntity.getDstTwin().getTwinClass(), true);
            if (srcTwinExtendedClasses.contains(linkEntity.getSrcTwinClassId())) { // forward link creation
                log.info("Forward link creation");
                twinLinkEntity
                        .setSrcTwin(srcTwinEntity)
                        .setSrcTwinId(srcTwinEntity.getId()); //dst is already filled
            } else if (srcTwinExtendedClasses.contains(linkEntity.getDstTwinClassId())) { // backward link creation, dst and src twins had to change places
                log.info("Backward link creation");
                twinLinkEntity
                        .setSrcTwin(twinLinkEntity.getDstTwin())
                        .setSrcTwinId(twinLinkEntity.getDstTwinId())
                        .setDstTwin(srcTwinEntity)
                        .setDstTwinId(srcTwinEntity.getId());
                Set<UUID> temp = srcTwinExtendedClasses;
                srcTwinExtendedClasses = dstTwinExtendedClasses;
                dstTwinExtendedClasses = temp;
            } else {
                throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT, linkEntity.easyLog(EasyLoggable.Level.NORMAL) + " can not be created for twinId[" + srcTwinEntity.getId() + "]");
            }
            if (!srcTwinExtendedClasses.contains(linkEntity.getSrcTwinClassId()))
                throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT, linkEntity.easyLog(EasyLoggable.Level.NORMAL) + " can not be created from twinId[" + twinLinkEntity.getSrcTwinId() + "] of twinClass[" + twinLinkEntity.getSrcTwin().getTwinClassId() + "]");
            if (!dstTwinExtendedClasses.contains(linkEntity.getDstTwinClassId()))
                throw new ServiceException(ErrorCodeTwins.TWIN_LINK_INCORRECT, linkEntity.easyLog(EasyLoggable.Level.NORMAL) + " can not be created to twinId[" + twinLinkEntity.getDstTwinId() + "] of twinClass[" + twinLinkEntity.getDstTwin().getTwinClassId() + "]");
            twinLinkEntity.setCreatedAt(Timestamp.from(Instant.now()));
        }
        entitySmartService.saveAllAndLog(linksEntityList, twinLinkRepository);
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
                linksResult.backwardLinks.put(twinLinkEntity.getId(),twinLinkEntity);
            } else
                log.warn(twinLinkEntity.easyLog(EasyLoggable.Level.NORMAL) + " is incorrect");
        }
        return linksResult;
    }

    @Data
    @Accessors(chain = true)
    public static class FindTwinLinksResult {
        UUID twinId;
        Map<UUID, TwinLinkEntity> forwardLinks = new LinkedHashMap<>();
        Map<UUID, TwinLinkEntity> backwardLinks = new LinkedHashMap<>();
    }
}
