package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.TypedParameterTwins;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.draft.DraftCommitService;
import org.twins.core.service.draft.DraftService;
import org.twins.core.service.permission.PermissionService;

import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@Lazy
@RequiredArgsConstructor
public class TwinEraserService {

    @Lazy
    private final PermissionService permissionService;
    @Lazy
    private final AuthService authService;
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final DraftService draftService;
    private final DraftCommitService draftCommitService;

    public DraftEntity eraseTwin(UUID twinId) throws ServiceException {
        return eraseTwins(twinService.findEntitySafe(twinId));
    }

    public DraftEntity eraseTwinDrafted(UUID twinId) throws ServiceException {
        return eraseTwinDrafted(twinService.findEntitySafe(twinId));
    }

    public DraftEntity eraseTwinDrafted(TwinEntity twinEntity) throws ServiceException {
        checkDeletePermission(twinEntity);
        return draftService.draftErase(twinEntity);
    }

    public DraftEntity eraseTwins(TwinEntity... twinEntityList) throws ServiceException {
        DraftEntity draftEntity = draftService.draftErase(twinEntityList);
        draftCommitService.commitNowOrInQueue(draftEntity);
        return draftEntity;
    }

    public DraftEntity eraseTwins(Set<UUID> twinIds) throws ServiceException {
        return eraseTwins(twinService.findEntitiesSafe(twinIds).getList().toArray(TwinEntity[]::new));
    }


    public void checkDeletePermission(TwinEntity twinEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID updatePermissionId = twinService.detectDeletePermissionId(twinEntity);
        if (null == updatePermissionId)
            return;
        boolean hasPermission = permissionService.hasPermission(twinEntity, updatePermissionId);
        if (!hasPermission)
            throw new ServiceException(ErrorCodeTwins.TWIN_CREATE_ACCESS_DENIED, apiUser.getUser().logShort() + " does not have permission to edit " + twinEntity.logNormal());
    }

}
