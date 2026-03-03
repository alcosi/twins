package org.twins.core.featurer.scheduler;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Service;
import org.twins.core.dao.permission.PermissionMaterGlobalRepository;
import org.twins.core.featurer.FeaturerTwins;

@Service
@RequiredArgsConstructor
@Featurer(
        id = FeaturerTwins.ID_5010,
        name = "SchedulerConsistencyCheckPermissionMaterGlobal",
        description = "Scheduler to check permission materialization global grants count is not less then 0"
)
public class SchedulerConsistencyCheckPermissionMaterGlobal extends SchedulerConsistencyCheck {
    private final PermissionMaterGlobalRepository permissionMaterGlobalRepository;
    @Override
    protected long invalidRecordsCount() {
        return permissionMaterGlobalRepository.countInvalidGrantsCount();
    }

    @Override
    protected String consistencyCheckName() {
        return "permission_mater_global.grants_count";
    }
}
