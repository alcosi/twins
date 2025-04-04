package org.twins.face.service.navbar;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.face.dao.navbar.nb001.FaceNB001MenuItemEntity;
import org.twins.face.dao.navbar.nb001.FaceNB001MenuItemRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceNB001MenuItemService extends EntitySecureFindServiceImpl<FaceNB001MenuItemEntity> {
    private final FaceNB001MenuItemRepository repository;

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
        return false;
    }
}
