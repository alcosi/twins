package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinTouchEntity;
import org.twins.core.dao.twin.TwinTouchRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinTouchService extends EntitySecureFindServiceImpl<TwinTouchEntity> {
    private final AuthService authService;
    private final TwinService twinService;
    private final TwinTouchRepository twinTouchRepository;

    public TwinTouchEntity addTouch(UUID twinId, String touchId) throws ServiceException {
        return addTouch(twinService.findEntitySafe(twinId), touchId);
    }

    private TwinTouchEntity addTouch(TwinEntity twin, String touchId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        TwinTouchEntity.Touch touch = TwinTouchEntity.Touch.valueOf(touchId.toUpperCase());
        TwinTouchEntity twinTouchEntityFromDB = twinTouchRepository.findByTwinIdAndTouchIdAndUserId(twin.getId(), touch, apiUser.getUserId());
        if (twinTouchEntityFromDB == null) {
            TwinTouchEntity twinTouchEntity = new TwinTouchEntity()
                    .setUserId(apiUser.getUserId())
                    .setTouchId(touch)
                    .setTwinId(twin.getId());
            twinTouchEntityFromDB = entitySmartService.save(twinTouchEntity, twinTouchRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        }
        return twinTouchEntityFromDB;
    }

    @Transactional
    public void deleteAllUsersTouch(UUID twinId, TwinTouchEntity.Touch touchId) throws ServiceException {
        twinTouchRepository.deleteByTwinIdAndTouchId(twinId, touchId);
        log.info("Touch[{}] on twin[{}] perhaps was deleted for all users", touchId, twinId);
    }

    @Transactional
    public void deleteCurrentUserTouch(UUID twinId, TwinTouchEntity.Touch touchId) throws ServiceException {
        deleteTouch(twinId, touchId, authService.getApiUser().getUserId());
    }

    @Transactional
    public void deleteTouch(UUID twinId, TwinTouchEntity.Touch touchId, UUID userId) throws ServiceException {
        twinTouchRepository.deleteByTwinIdAndTouchIdAndUserId(twinId, touchId, userId);
        log.info("Touch[{}] on twin[{}] perhaps was deleted for user[{}]", touchId, twinId, userId);
    }

    @Override
    public CrudRepository<TwinTouchEntity, UUID> entityRepository() {
        return null;
    }

    @Override
    public Function<TwinTouchEntity, UUID> entityGetIdFunction() {
        return TwinTouchEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinTouchEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return twinService.isEntityReadDenied(entity.getTwin(), readPermissionCheckMode);
    }

    @Override
    public boolean validateEntity(TwinTouchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
