package org.twins.core.featurer.scheduler;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Service;
import org.twins.core.dao.permission.PermissionMaterSpaceUserGroupRepository;
import org.twins.core.featurer.FeaturerTwins;

@Service
@RequiredArgsConstructor
@Featurer(
        id = FeaturerTwins.ID_5013,
        name = "SchedulerConsistencyCheckPermissionMaterSpaceUserGroup",
        description = "Scheduler to check permission materialization space user groups grants count is not less then 0"
)
public class SchedulerConsistencyCheckPermissionMaterSpaceUserGroup extends SchedulerConsistencyCheck {
    private final PermissionMaterSpaceUserGroupRepository permissionMaterSpaceUserGroupRepository;
    @Override
    protected long invalidRecordsCount() {
        return permissionMaterSpaceUserGroupRepository.countInvalidGrantsCount();
    }

    @Override
    protected String consistencyCheckName() {
        return "permission_mater_space_user_group.grants_count";
    }
}
