package org.twins.core.featurer.scheduler;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Service;
import org.twins.core.dao.permission.PermissionMaterGlobalRepository;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinService;

@Service
@RequiredArgsConstructor
@Featurer(
        id = FeaturerTwins.ID_5012,
        name = "SchedulerConsistencyCheckPermissionSchemaDetectMismatches",
        description = "Scheduler to check permission schema detect mismatches in twin"
)
public class SchedulerConsistencyCheckPermissionSchemaDetectMismatches extends SchedulerConsistencyCheck {
    private final TwinService twinService;
    @Override
    protected long invalidRecordsCount() {
        return twinService.countPermissionSchemaMismatches();
    }

    @Override
    protected String consistencyCheckName() {
        return "twin.permission_schema_id";
    }
}
