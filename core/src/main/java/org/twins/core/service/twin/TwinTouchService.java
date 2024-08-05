package org.twins.core.service.twin;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinTouchEntity;
import org.twins.core.dao.twin.TwinTouchRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

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

//    public static Specification<TwinEntity> checkTouchIds(final Collection<TwinTouchEntity.Touch> touchIds, final UUID userId, final boolean exclude) {
//        return (root, query, cb) -> {
//            if (CollectionUtils.isEmpty(touchIds)) return cb.conjunction();
//
//            Join<TwinEntity, TwinTouchEntity> touchJoin = root.join(TwinEntity.Fields.touches, JoinType.LEFT);
//            Predicate touchIdIn = touchJoin.get(TwinTouchEntity.Fields.touchId).in(touchIds);
//            Predicate userIdEqual = cb.equal(touchJoin.get(TwinTouchEntity.Fields.userId), userId);
//            query.distinct(true);
//
//            if (exclude) {
//                Predicate noTouches = cb.isNull(touchJoin.get(TwinTouchEntity.Fields.twinId));
//                return cb.or(noTouches, cb.not(cb.and(touchIdIn, userIdEqual)));
//            } else {
//                return cb.and(cb.isNotNull(touchJoin.get(TwinTouchEntity.Fields.twinId)), touchIdIn, userIdEqual);
//            }
//        };
//    }

    @Transactional
    public void deleteTouch(UUID twinId, TwinTouchEntity.Touch touchId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID userId = apiUser.getUserId();
        twinTouchRepository.deleteByTwinIdAndTouchIdAndUserId(twinId, touchId, userId);
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
