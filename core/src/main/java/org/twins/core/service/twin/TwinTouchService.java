package org.twins.core.service.twin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.Touch;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinTouchEntity;
import org.twins.core.dao.twin.TwinTouchRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinTouchService extends EntitySecureFindServiceImpl<TwinTouchEntity> {
    private final AuthService authService;
    private final TwinService twinService;
    private final TwinTouchRepository twinTouchRepository;

    public TwinTouchEntity addTouch(UUID twinId, String touchId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Touch touch = Touch.valueOf(touchId.toUpperCase());
        TwinTouchEntity twinTouchEntityFromDB = twinTouchRepository.findByTwinIdAndTouchIdAndUserId(twinId, touch, apiUser.getUserId());
        if (twinTouchEntityFromDB == null) {
            TwinTouchEntity twinTouchEntity = new TwinTouchEntity()
                    .setUserId(apiUser.getUserId())
                    .setTouchId(touch)
                    .setTwinId(twinId);
            twinTouchEntityFromDB = entitySmartService.save(twinTouchEntity, twinTouchRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        }
        return twinTouchEntityFromDB;
    }

    @Transactional
    public void deleteTouch(UUID twinId, Touch touchId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID userId = apiUser.getUserId();
        twinTouchRepository.deleteByTwinIdAndTouchIdAndUserId(twinId, touchId, apiUser.getUserId());
        log.info("Touch[{}] perhaps were deleted", StringUtils.join(twinId, ",", touchId, ",", userId));
    }

    @Override
    public CrudRepository<TwinTouchEntity, UUID> entityRepository() {
        return null;
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
