package org.twins.core.featurer.scheduler;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Service;
import org.twins.core.dao.permission.PermissionMaterUserGroupRepository;
import org.twins.core.featurer.FeaturerTwins;

@Service
@RequiredArgsConstructor
@Featurer(
        id = FeaturerTwins.ID_5011,
        name = "SchedulerConsistencyCheckPermissionMaterUserGroup",
        description = "Scheduler to check permission materialization user groups grants count is not less then 0"
)
public class SchedulerConsistencyCheckPermissionMaterUserGroup extends SchedulerConsistencyCheck {
    private final PermissionMaterUserGroupRepository permissionMaterUserGroupRepository;
    @Override
    protected long invalidRecordsCount() {
        return permissionMaterUserGroupRepository.countInvalidGrantsCount();
    }

    @Override
    protected String consistencyCheckName() {
        return "permission_mater_user_group.grants_count";
    }
}
