package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dto.rest.permission.PermissionDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionGroupMode;
import org.twins.core.service.permission.PermissionGroupService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class PermissionRestDTOMapperV2 extends RestSimpleDTOMapper<PermissionEntity, PermissionDTOv2> {

    private final PermissionGroupService permissionService;
    private final PermissionRestDTOMapper permissionRestDTOMapper;
    private final PermissionGroupRestDTOMapper permissionGroupRestDTOMapper;

    @Override
    public void map(PermissionEntity src, PermissionDTOv2 dst, MapperContext mapperContext) throws Exception {
        permissionRestDTOMapper.map(src, dst, mapperContext);
        if (showPermissionGroup(mapperContext)) {
            permissionService.loadPermissionGroup(src);
            dst.setGroupId(src.getPermissionGroupId());
            dst.setGroup(permissionGroupRestDTOMapper.convertOrPostpone(src.getPermissionGroup(), mapperContext));
        }
    }

    private static boolean showPermissionGroup(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(PermissionGroupMode.HIDE);
    }

    @Override
    public void beforeCollectionConversion(Collection<PermissionEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (showPermissionGroup(mapperContext))
            permissionService.loadPermissionGroup(srcCollection);
    }
}
