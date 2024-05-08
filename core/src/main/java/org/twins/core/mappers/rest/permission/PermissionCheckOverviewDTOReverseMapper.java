package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.permission.PermissionCheckOverview;
import org.twins.core.dto.rest.permission.PermissionCheckOverviewRqDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
@RequiredArgsConstructor
public class PermissionCheckOverviewDTOReverseMapper extends RestSimpleDTOMapper<PermissionCheckOverviewRqDTOv1, PermissionCheckOverview> {

    @Override
    public void map(PermissionCheckOverviewRqDTOv1 src, PermissionCheckOverview dst, MapperContext mapperContext) throws Exception {
        dst.setUserId(src.userId)
                .setPermissionId(src.permissionId);
    }
}
