package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinTouchEntity;
import org.twins.core.dao.twin.TwinTouchRepository;
import org.twins.core.service.auth.AuthService;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinTouchService extends EntitySecureFindServiceImpl<TwinTouchEntity> {
    private final AuthService authService;
    private final TwinService twinService;
    private final TwinTouchRepository twinTouchRepository;
    private final TwinRepository twinRepository;

    @Transactional
    public TwinTouchEntity addTouch(UUID twinId, String touchId) throws ServiceException {
        List<TwinTouchEntity> list = addTouch(Collections.singleton(twinId), touchId);
        if (CollectionUtils.isNotEmpty(list))
            return list.get(0);
        else
            return new TwinTouchEntity();
    }

    @Transactional
    public List<TwinTouchEntity> addTouch(Set<UUID> twinIds, String touchId) throws ServiceException {
        UUID userId = authService.getApiUser().getUserId();
        List<TwinEntity> dbEntities = twinRepository.findByIdIn(twinIds);
        if (CollectionUtils.isEmpty(dbEntities))
            return Collections.emptyList();
        List<TwinTouchEntity> savedTouches = new ArrayList<>();
        for (TwinEntity twin : dbEntities) {
            Optional<TwinTouchEntity> savedEntity = twinTouchRepository.saveOrGetIfExists(UUID.randomUUID() , twin.getId(), touchId, userId, Instant.now());
            savedEntity.ifPresent(savedTouches::add);
        }
        return savedTouches;
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
