package org.twins.face.service.navbar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.face.dao.navbar.FaceNB001Entity;
import org.twins.face.dao.navbar.FaceNB001MenuItemEntity;
import org.twins.face.dao.navbar.FaceNB001MenuItemRepository;
import org.twins.face.dao.navbar.FaceNB001Repository;

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
    @Lazy
    private final AuthService authService;


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
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getFace().getDomainId().equals(authService.getApiUser().getDomainId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allows in domain[" + apiUser.getDomainId() + "]");
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(FaceNB001Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadMenuItems(FaceNB001Entity src) {
        loadMenuItems(Collections.singletonList(src));
    }

    public void loadMenuItems(Collection<FaceNB001Entity> srcList) {
        if (CollectionUtils.isEmpty(srcList))
            return;
        Kit<FaceNB001Entity, UUID> needLoad = new Kit<>(FaceNB001Entity::getFaceId);
        for (var faceNB001Entity : srcList)
            if (faceNB001Entity.getMenuItems() == null) {
                faceNB001Entity.setMenuItems(new Kit<>(FaceNB001MenuItemEntity::getId));
                needLoad.add(faceNB001Entity);
            }
        if (needLoad.isEmpty())
            return;
        KitGrouped<FaceNB001MenuItemEntity, UUID, UUID> loadedKit = new KitGrouped<>(
                faceNB001MenuItemRepository.findByFaceIdIn(needLoad.getIdSet()), FaceNB001MenuItemEntity::getId, FaceNB001MenuItemEntity::getFaceId);
        for (var entry : loadedKit.getGroupedMap().entrySet()) {
            needLoad.get(entry.getKey()).getMenuItems().addAll(entry.getValue());
        }
    }
}
