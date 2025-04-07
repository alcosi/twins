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
import org.twins.core.service.face.FaceService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.permission.PermissionService;
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
    private final FaceService faceService;
    private final I18nService i18nService;
    private final PermissionService permissionService;

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
        if (entity.getTargetPageFaceId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty targetPageFaceId");

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getFace() == null || !entity.getFace().getId().equals(entity.getFaceId()))
                    entity.setFace(faceService.findEntitySafe(entity.getFaceId()));
                if (entity.getLabelI18n() == null || !entity.getLabelI18n().getId().equals(entity.getLabelI18nId()))
                    entity.setLabelI18n(i18nService.findEntitySafe(entity.getLabelI18nId()));
                if (entity.getDescriptionI18nId() != null && (entity.getDescriptionI18n() == null || !entity.getDescriptionI18n().getId().equals(entity.getDescriptionI18nId())))
                    entity.setDescriptionI18n(i18nService.findEntitySafe(entity.getDescriptionI18nId()));
                if (entity.getTargetPageFace() == null || !entity.getTargetPageFace().getId().equals(entity.getTargetPageFaceId()))
                    entity.setTargetPageFace(faceService.findEntitySafe(entity.getTargetPageFaceId()));
                if (entity.getPermissionId() != null && (entity.getPermission() == null || !entity.getPermission().getId().equals(entity.getPermissionId())))
                    entity.setPermission(permissionService.findEntitySafe(entity.getPermissionId()));
        }
        return true;
    }

}
