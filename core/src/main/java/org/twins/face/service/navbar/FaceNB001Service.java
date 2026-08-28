package org.twins.face.service.navbar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceService;
import org.twins.face.dao.navbar.nb001.FaceNB001Entity;
import org.twins.face.dao.navbar.nb001.FaceNB001MenuItemEntity;
import org.twins.face.dao.navbar.nb001.FaceNB001MenuItemRepository;
import org.twins.face.dao.navbar.nb001.FaceNB001Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceNB001Service extends EntitySecureFindServiceImpl<FaceNB001Entity> {
    private final FaceNB001Repository faceNB001Repository;
    private final FaceNB001MenuItemRepository faceNB001MenuItemRepository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceNB001Entity, UUID> entityRepository() {
        return faceNB001Repository;
    }

    @Override
    public Function<FaceNB001Entity, UUID> entityGetIdFunction() {
        return FaceNB001Entity::getFaceId;
    }

    @Override
    public boolean isEntityReadDenied(FaceNB001Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceNB001Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadMenuItems(FaceNB001Entity src) {
        loadMenuItems(Collections.singletonList(src));
    }

    public void loadMenuItems(Collection<FaceNB001Entity> srcList) {
        loadKit(srcList,
                FaceNB001Entity::getFaceId,
                FaceNB001Entity::getMenuItems,
                FaceNB001Entity::setMenuItems,
                faceNB001MenuItemRepository::findByFaceIdInAndParentFaceMenuItemIdIsNull,
                FaceNB001MenuItemEntity::getId,
                FaceNB001MenuItemEntity::getFaceId,
                FaceNB001MenuItemEntity::setFaceNB001);
    }
}
