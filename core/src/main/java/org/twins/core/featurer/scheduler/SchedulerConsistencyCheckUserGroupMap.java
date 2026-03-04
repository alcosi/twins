package org.twins.core.featurer.scheduler;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Service;
import org.twins.core.dao.permission.PermissionMaterUserGroupRepository;
import org.twins.core.dao.usergroup.UserGroupMapRepository;
import org.twins.core.featurer.FeaturerTwins;

@Service
@RequiredArgsConstructor
@Featurer(
        id = FeaturerTwins.ID_5015,
        name = "SchedulerConsistencyCheckUserGroupMap",
        description = "Scheduler to check user group map involves count is not less then 0"
)
public class SchedulerConsistencyCheckUserGroupMap extends SchedulerConsistencyCheck {
    private final UserGroupMapRepository userGroupMapRepository;
    @Override
    protected long invalidRecordsCount() {
        return userGroupMapRepository.countInvalidIvolvesCount();
    }

    @Override
    protected String consistencyCheckName() {
        return "user_group_map.involves_count";
    }
}
