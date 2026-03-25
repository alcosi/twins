package org.twins.core.featurer.scheduler;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Service;
import org.twins.core.dao.permission.PermissionMaterSpaceUserRepository;
import org.twins.core.featurer.FeaturerTwins;

@Service
@RequiredArgsConstructor
@Featurer(
        id = FeaturerTwins.ID_5014,
        name = "SchedulerConsistencyCheckPermissionMaterSpaceUser",
        description = "Scheduler to check permission materialization space user grants count is not less then 0"
)
public class SchedulerConsistencyCheckPermissionMaterSpaceUser extends SchedulerConsistencyCheck {
    private final PermissionMaterSpaceUserRepository permissionMaterSpaceUserRepository;
    @Override
    protected long invalidRecordsCount() {
        return permissionMaterSpaceUserRepository.countInvalidGrantsCount();
    }

    @Override
    protected String consistencyCheckName() {
        return "permission_mater_space_user.grants_count";
    }
}
