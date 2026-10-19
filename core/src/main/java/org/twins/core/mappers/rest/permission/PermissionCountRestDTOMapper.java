package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.permission.PermissionCountDTOv1;
import org.twins.core.enums.sort.PermissionGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionGroupMode;
import org.twins.core.service.permission.PermissionService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class PermissionCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<PermissionEntity, PermissionGroupField>, PermissionCountDTOv1> {
    private final PermissionService permissionService;

    @MapperModePointerBinding(modes = PermissionGroupMode.Permission2PermissionGroupMode.class)
    private final PermissionGroupRestDTOMapper permissionGroupRestDTOMapper;

    @Override
    public void map(CountResult<PermissionEntity, PermissionGroupField> src, PermissionCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setGroupId(entity.getPermissionGroupId())
                .setCount(src.getCount());
        if (needLoad(mapperContext, PermissionGroupMode.Permission2PermissionGroupMode.HIDE, src, PermissionGroupField.groupId)) {
            permissionService.loadPermissionGroup(entity);
            permissionGroupRestDTOMapper.postpone(entity.getPermissionGroup(), mapperContext.forkOnPoint(PermissionGroupMode.Permission2PermissionGroupMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<PermissionEntity, PermissionGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        var entities = srcCollection.stream().map(CountResult::getEntity).toList();
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, PermissionGroupMode.Permission2PermissionGroupMode.HIDE, someCount, PermissionGroupField.groupId)) {
            permissionService.loadPermissionGroup(entities);
        }
    }
}
