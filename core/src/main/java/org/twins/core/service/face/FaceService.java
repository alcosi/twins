package org.twins.core.service.face;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FacePointedEntity;
import org.twins.core.dao.face.FaceRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceService extends EntitySecureFindServiceImpl<FaceEntity> {
    private final FaceRepository faceRepository;
    @Lazy
    private final AuthService authService;
    private final TwinService twinService;
    @Getter
    private final RequestTwinPointers requestFacePointers;

    @Override
    public CrudRepository<FaceEntity, UUID> entityRepository() {
        return faceRepository;
    }

    @Override
    public Function<FaceEntity, UUID> entityGetIdFunction() {
        return FaceEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getDomainId().equals(authService.getApiUser().getDomainId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allows in " + apiUser.getDomain().logShort());
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(FaceEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadTwin(PointedFace<? extends FacePointedEntity> src) throws ServiceException {
        loadTwin(Collections.singletonList(src));
    }

    public void loadTwin(Collection<PointedFace<? extends FacePointedEntity>> srcCollection) throws ServiceException {
        Set<UUID> needLoad = new HashSet<>();
        for (var config : srcCollection) {
            if (config.getTargetTwin() == null || !config.getTargetTwinId().equals(config.getTargetTwin().getId())) {
                needLoad.add(config.getTargetTwinId());
            }
        }
        if (needLoad.isEmpty())
            return;
        Kit<TwinEntity, UUID> twinsKit = twinService.findEntitiesSafe(needLoad);
        for (var config : srcCollection) {
            if (config.getTargetTwin() == null || !config.getTargetTwinId().equals(config.getTargetTwin().getId())) {
                config.setTargetTwin(twinsKit.get(config.getTargetTwinId()));
            }
        }
    }
}
