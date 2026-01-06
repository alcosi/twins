package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinTouchEntity;
import org.twins.core.dao.twin.TwinTouchRepository;
import org.twins.core.enums.twin.Touch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinTouchService extends EntitySecureFindServiceImpl<TwinTouchEntity> {
    private final AuthService authService;
    private final TwinService twinService;
    private final TwinTouchRepository twinTouchRepository;
    private final TwinRepository twinRepository;

    @Transactional
    public TwinTouchEntity addTouch(UUID twinId, Touch touch) throws ServiceException {
        List<TwinTouchEntity> list = addTouch(Collections.singleton(twinId), touch);
        if (CollectionUtils.isNotEmpty(list))
            return list.get(0);
        else
            throw new ServiceException(ErrorCodeTwins.TWIN_ID_IS_INCORRECT);
    }

    @Transactional
    public List<TwinTouchEntity> addTouch(Set<UUID> twinIds, Touch touch) throws ServiceException {
        if (CollectionUtils.isEmpty(twinIds))
            return Collections.emptyList();
        UUID userId = authService.getApiUser().getUserId();
        List<TwinTouchEntity> dbEntities = twinTouchRepository.findByTwinIdInAndTouchIdAndUserId(twinIds, touch, userId);
        Kit<TwinTouchEntity, UUID> kit = new Kit<>(dbEntities, TwinTouchEntity::getTwinId);
        List<TwinTouchEntity> ret = new ArrayList<>();
        List<TwinTouchEntity> needSave = new ArrayList<>();
        for (UUID twinId : twinIds) {
            if (kit.containsKey(twinId))
                ret.add(kit.get(twinId));
            else {
                TwinTouchEntity forSave = new TwinTouchEntity()
                        .setTwinId(twinId)
                        .setTouchId(touch)
                        .setUserId(userId);
                needSave.add(forSave);
                ret.add(forSave);
            }
        }
        //todo there will be an error if an incorrect twins is transmitted
        // and check permission
        ret.addAll((List<TwinTouchEntity>) twinTouchRepository.saveAll(needSave));
        return ret;
    }

    @Transactional
    public void deleteAllUsersTouch(UUID twinId, Touch touchId) throws ServiceException {
        twinTouchRepository.deleteByTwinIdAndTouchId(twinId, touchId);
        log.info("Touch[{}] on twin[{}] perhaps was deleted for all users", touchId, twinId);
    }

    @Transactional
    public void deleteCurrentUserTouch(UUID twinId, Touch touchId) throws ServiceException {
        deleteTouch(twinId, touchId, authService.getApiUser().getUserId());
    }

    @Transactional
    public void deleteTouch(UUID twinId, Touch touchId, UUID userId) throws ServiceException {
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
