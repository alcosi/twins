package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dto.rest.permission.PermissionGroupDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionGroupMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = PermissionGroupMode.class)
public class PermissionGroupRestDTOMapper extends RestSimpleDTOMapper<PermissionGroupEntity, PermissionGroupDTOv1> {

    @MapperModePointerBinding(modes = TwinClassMode.PermissionGroup2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Override
    public void map(PermissionGroupEntity src, PermissionGroupDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(PermissionGroupMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setTwinClassId(src.getTwinClassId())
                        .setName(src.getName())
                        .setDescription(src.getDescription());
                if (mapperContext.hasModeButNot(TwinClassMode.PermissionGroup2TwinClassMode.HIDE))
                    dst
                            .setTwinClassId(src.getTwinClassId())
                            .setTwinClass(twinClassRestDTOMapper.convertOrPostpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.PermissionGroup2TwinClassMode.SHORT)));
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey());
                break;
        }
    }
}
