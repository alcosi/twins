package org.twins.face.service.navbar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.service.face.FaceService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.resource.ResourceService;
import org.twins.core.service.twin.TwinSearchServiceV3;
import org.twins.face.dao.navbar.nb001.FaceNB001MenuItemEntity;
import org.twins.face.dao.navbar.nb001.FaceNB001MenuItemRepository;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceNB001MenuItemService extends EntitySecureFindServiceImpl<FaceNB001MenuItemEntity> {
    private final FaceNB001MenuItemRepository repository;
    private final FaceService faceService;
    private final I18nService i18nService;
    private final PermissionService permissionService;
    private final ResourceService resourceService;
    private final TwinSearchServiceV3 twinSearchServiceV3;

    @Override
    public CrudRepository<FaceNB001MenuItemEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<FaceNB001MenuItemEntity, UUID> entityGetIdFunction() {
        return FaceNB001MenuItemEntity::getFaceId;
    }

    @Override
    public boolean isEntityReadDenied(FaceNB001MenuItemEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceNB001MenuItemEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getFaceId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty faceId");
        if (entity.getKey() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty key");
        if (entity.getLabelI18nId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty labelI18nId");

        switch (entityValidateMode) {
            case beforeSave:
                faceService.findEntitySafe(entity.getFaceId());
                i18nService.findEntitySafe(entity.getLabelI18nId());
                if (entity.getDescriptionI18nId() != null)
                    i18nService.findEntitySafe(entity.getDescriptionI18nId());
                if (entity.getTargetPageFaceId() != null && (entity.getTargetPageFace() == null || !entity.getTargetPageFace().getId().equals(entity.getTargetPageFaceId())))
                    entity.setTargetPageFace(faceService.findEntitySafe(entity.getTargetPageFaceId()));
                if (entity.getPermissionId() != null && (entity.getPermission() == null || !entity.getPermission().getId().equals(entity.getPermissionId())))
                    entity.setPermission(permissionService.findEntitySafe(entity.getPermissionId()));
                if (entity.getCounterTwinSearchId() != null && !SystemIds.TwinSearch.UNLIMITED.equals(entity.getCounterTwinSearchId()))
                    twinSearchServiceV3.findEntitySafe(entity.getCounterTwinSearchId());
        }
        return true;
    }

    public void loadChilds(FaceNB001MenuItemEntity src) {
        loadChilds(Collections.singletonList(src));
    }

    public List<FaceNB001MenuItemEntity> sorted(Iterable<FaceNB001MenuItemEntity> items) {
        List<FaceNB001MenuItemEntity> list = new ArrayList<>();
        items.forEach(list::add);
        list.sort(Comparator
                .comparing(FaceNB001MenuItemEntity::getOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(FaceNB001MenuItemEntity::getKey, Comparator.nullsLast(String::compareTo)));
        return list;
    }

    public void loadChilds(Collection<FaceNB001MenuItemEntity> srcList) {
        loadKit(srcList,
                FaceNB001MenuItemEntity::getId,
                FaceNB001MenuItemEntity::getChilds,
                FaceNB001MenuItemEntity::setChilds,
                repository::findByParentFaceMenuItemIdInOrderByOrderAscKeyAsc,
                FaceNB001MenuItemEntity::getId,
                FaceNB001MenuItemEntity::getParentFaceMenuItemId,
                FaceNB001MenuItemEntity::setParentFaceMenuItem);
    }

    public void loadIconResources(FaceNB001MenuItemEntity src) throws ServiceException {
        loadIconResources(Collections.singletonList(src));
    }

    public void loadIconResources(Collection<FaceNB001MenuItemEntity> srcList) throws ServiceException {
        resourceService.load(srcList,
                FaceNB001MenuItemEntity::getIconResourceId,
                FaceNB001MenuItemEntity::getIconResource,
                FaceNB001MenuItemEntity::setIconResource);
    }

    public void loadTargetPageFaces(FaceNB001MenuItemEntity src) throws ServiceException {
        loadTargetPageFaces(Collections.singletonList(src));
    }

    public void loadTargetPageFaces(Collection<FaceNB001MenuItemEntity> srcList) throws ServiceException {
        faceService.load(srcList,
                FaceNB001MenuItemEntity::getTargetPageFaceId,
                FaceNB001MenuItemEntity::getTargetPageFace,
                FaceNB001MenuItemEntity::setTargetPageFace);
    }

    public void loadPermissions(FaceNB001MenuItemEntity src) throws ServiceException {
        loadPermissions(Collections.singletonList(src));
    }

    public void loadPermissions(Collection<FaceNB001MenuItemEntity> srcList) throws ServiceException {
        permissionService.load(srcList,
                FaceNB001MenuItemEntity::getPermissionId,
                FaceNB001MenuItemEntity::getPermission,
                FaceNB001MenuItemEntity::setPermission);
    }

    public void loadCounters(FaceNB001MenuItemEntity src) throws ServiceException {
        loadCounters(Collections.singletonList(src));
    }

    /**
     * Fills the transient counter badge for every item with counterTwinSearchId set. Each distinct
     * search is counted once through the configured pipeline (the header is resolved inside, a
     * misconfigured id fails the request); items without a configured search keep counter null.
     */
    public void loadCounters(Collection<FaceNB001MenuItemEntity> srcList) throws ServiceException {
        Map<UUID, Long> counts = new HashMap<>();
        for (FaceNB001MenuItemEntity item : srcList) {
            UUID searchId = item.getCounterTwinSearchId();
            item.setCounter(null);
            if (searchId == null)
                continue;
            if (!counts.containsKey(searchId))
                counts.put(searchId, twinSearchServiceV3.countConfigured(searchId, null, null));
            item.setCounter(counts.get(searchId));
        }
    }

}
